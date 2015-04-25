#include <include/CrsD.h>
#include <sstream>
#include <fstream>
#include <include/RuntimeError.h>
#include <iostream>
#include <include/ActiveRules.h>

using namespace std;

regex CrsDFile::dRuleStart = regex(R"(^\s*(\{[^\}\n]+\})?\s*([^\[\n]+)?\s*\[([^\]]+)\]\s*::\s*([^;]+?)\s*;\s*$)");
regex CrsDFile::dRuleContent = regex(R"(^\s*\'?(\S+)-(\d+)([^\[\]]+)?\'?\[([\S\s]+?)\]\s*\:\s*([\S\s]+?)?\s*→\s*([\s\S]+?)\s*\;\s*$)");

#define prepare(sql,stmt) { \
	int rc = sqlite3_prepare_v2(db, (sql), -1, &(stmt), 0); \
	if (rc != SQLITE_OK) { \
		cout << "Error " << rc << " creating steps insert statement" << endl << (sql); \
		RuntimeError("Error creating steps insert statement"); \
	} \
}

#define bindit(stmt,pos,val) { \
	sqlite3_bind_text(stmt, pos, val.c_str(), val.length(), SQLITE_STATIC); \
}

void CrsDFile::write(sqlite3* db) {
	if (db == NULL) {
		RuntimeError("CrsD write database cannot be NULL");
	}

	//Update database schema
	vector<string> cmds;
	cmds.push_back("ALTER TABLE `ActiveRules` ADD COLUMN 'Env' TEXT;");
	cmds.push_back("ALTER TABLE `ActiveRules` ADD COLUMN 'Args' TEXT;");
	cmds.push_back("ALTER TABLE `ActiveRules` ADD COLUMN 'ResultType' TEXT;");
	cmds.push_back("ALTER TABLE `ActiveRules` ADD COLUMN 'UsedInTrace' INTEGER;");
	cmds.push_back("UPDATE `ActiveRules` SET UsedInTrace=1;");
	cmds.push_back("CREATE TABLE `DispatchedRules`(ActiveRuleID INTEGER, SrcRuleName TEXT, SrcRuleOffset INTEGER, SrcRuleIdent TEXT, SrcRuleArgs TEXT, StartState TEXT, EndState TEXT);");
	for (string cmd : cmds) {
		char *errMsg = NULL;
		int rc = sqlite3_exec(db, cmd.c_str(), NULL, 0, &errMsg);
		if (rc != SQLITE_OK){
			fprintf(stderr, "SQL error: %s\n", errMsg);
			sqlite3_free(errMsg);
			RuntimeError("Error modifying CompiledStatements autoincrement");
		}
	}

	//Set up to insert data
	sqlite3_stmt * setEnv = NULL;
	sqlite3_stmt * insDispatched = NULL;
	prepare("UPDATE `ActiveRules` SET `Env`=?, `Args`=?, `ResultType`=? WHERE `ActiveRuleID`=?;", setEnv);
	prepare("INSERT INTO `DispatchedRules` VALUES (?,?,?,?,?,?,?);", insDispatched);

	//Write the rule data
	for (Rule &r : rules) {
		int rc = 0;
		
		//cleanup
		rc = sqlite3_reset(setEnv);
		if (rc != SQLITE_OK) {
			RuntimeError("Error resetting statement");
		}
			
		//Do the updates
		int id = ActiveRuleManager::GetActiveRuleID(r.Name);
		bindit(setEnv, 1, r.Env);
		bindit(setEnv, 2, r.Args);
		bindit(setEnv, 3, r.ResultType);
		sqlite3_bind_int(setEnv, 4, id);
		
		assert(sqlite3_step(setEnv) == SQLITE_DONE);

		for (Dispatch d : r.handlers) {
			rc = sqlite3_reset(insDispatched);
			if (rc != SQLITE_OK) {
				RuntimeError("Error resetting statement");
			}

			sqlite3_bind_int(insDispatched, 1, id);
			bindit(insDispatched, 2, d.SrcRuleName);
			bindit(insDispatched, 3, d.SrcRuleOffset);
			bindit(insDispatched, 4, d.SrcRuleIdent);
			bindit(insDispatched, 5, d.SrcRuleArgs);
			bindit(insDispatched, 6, d.StartState);
			bindit(insDispatched, 7, d.EndState);

			assert(sqlite3_step(insDispatched) == SQLITE_DONE);
		}
	}
}

size_t CrsDFile::getLeftSpaces(string s) {
	size_t l = s.find_first_not_of(" \n\t");
	if (l == string::npos) {
		return 999;
	}
	return l;
}

vector<string> CrsDFile::groupify(vector<string> lines) {
	vector<string> out;
	
	for (size_t i = 1; i < lines.size() - 1;) {
		vector<string> tmpGrp;
		size_t fIndent = getLeftSpaces(lines[i]);
		tmpGrp.push_back(lines[i]);
		printf("Starting new group at indent %ld with string\n%s\n", fIndent, lines[i].c_str());
		i++;
		while (i < lines.size() && getLeftSpaces(lines[i]) > fIndent) {
			if (getLeftSpaces(lines[i]) == 999) {
				printf("Skipping blank line\n");
			}
			else {
				tmpGrp.push_back(lines[i]);
				printf("Found %ld spaces before:\n%s\n", getLeftSpaces(lines[i]), lines[i].c_str());
				i++;
			}
		}
		printf("Failing to add to group. fIndent=%ld cIndent=%ld\n", fIndent, getLeftSpaces(lines[i]));
		string tmpS = "";
		for (string tmpL : tmpGrp) {
			tmpS.append(tmpL);
			tmpS.append("\n");
		}
		printf("Found group %ld:\n%s\n", out.size(), tmpS.c_str());
		out.push_back(tmpS);
	}
	
	return out;
}

vector<string> CrsDFile::readfile(vector<string> files) {
	vector<string> tmp;
	for (string filename : files) {
		ifstream infile(filename);
		string s;
		while (getline(infile, s)) {
			tmp.push_back(s);
		}
	}
	return tmp;
}

CrsDFile::CrsDFile(vector<string> files) {
	std::locale old;
	std::locale::global(std::locale("en_US.UTF-8"));

	vector<string> groups = groupify(readfile(files));
	size_t i = 0;
	while (i < groups.size()) {
		string line = groups[i].c_str();
		i++;
		smatch m;
		Rule stor;

		if (regex_match(line, m, dRuleStart)) {
			printf("Found dispatch rule %s\n", m[2].str().c_str());
			stor.Name = string((char*)m[2].str().c_str());
			stor.Env = string((char*)m[1].str().c_str());
			stor.Args = string((char*)m[3].str().c_str());
			stor.ResultType = string((char*)m[4].str().c_str());

			while (i < groups.size()) {
				string &ctx = groups[i];
				smatch r;
				if (regex_match(ctx, r, dRuleContent)) {
					i++;
					printf("Loaded rule dispatch handler\n");
					stor.handlers.push_back({
						string((char*)r[1].str().c_str()),
						string((char*)r[2].str().c_str()),
						string((char*)r[3].str().c_str()),
						string((char*)r[4].str().c_str()),
						string((char*)r[5].str().c_str()),
						string((char*)r[6].str().c_str())
					});
				}
				else {
					break;
				}
			}
			if (stor.handlers.size() == 0) {
				printf("Warning: No handlers found for dispatch rule\n");
			}
			else {
				printf("Loaded %ld handlers for %s\n", stor.handlers.size(), stor.Name.c_str());
				rules.push_back(stor);
			}
		}
		else {
			printf("No rule start found in string:\n%s", line.c_str());
		}
	}
	std::locale::global(old);
}
