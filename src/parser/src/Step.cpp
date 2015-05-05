#include <include/Step.h>
#include <stdexcept>
#include <iostream>
#include <include/Cookies.h>
#include <include/ActiveRules.h>
#include <include/RuntimeError.h>

using namespace std;

sqlite3_stmt* Step::StepsInsertStmt = NULL;
sqlite3_stmt* Step::StepsUpdateStmt = NULL;
sqlite3_stmt* Step::CompiledInsertStmt = NULL;
sqlite3_stmt* Step::CompiledFirstInsertStmt = NULL;

unsigned long long Step::lastStepNum = 0;

bool Step::firstWrite = true;

std::string Step::left_flat = "";
std::string Step::right_flat = "";

size_t Step::lastIndent = 0;
std::deque<std::string> Step::left;
std::deque<std::string> Step::right;
Step::SplitStep Step::prev = {"","",""};

/** Removes all content before the last space
*   @param s The string to strip
*   @return the string without any leading spaces
*/
string lstrip(string &s) {
	return s.substr(s.find_last_of(' ') + 1, s.length());
}

Step::Step(vector<string> &buf) {
	assert(buf.size() > 0);
	
	parseStepHeader(buf[0]);
	unsigned int ln = 0;
	
	while (++ln < buf.size() && buf[ln].length() != 0){
		data.append(buf[ln].append("\n"));
	}
	while (++ln < buf.size() && buf[ln].length() == 0);
	
	if (ln < buf.size()) {
		if (buf[ln].find("Cookies") != string::npos) {
			while (++ln < buf.size() && buf[ln].length() != 0){
				cookies.addCookie(lstrip(buf[ln]));
			}
		}
	}
	
	lastStepNum = stepNum;
	
	if (this->begin) { //if it's a start do the compile

		if (lastIndent < indent) {
			SplitStep s = prev;
			left.push_front(s.left);
			right.push_back(s.right);
			
			left_flat = "";
			for (auto substr : left) {
				left_flat += substr;
			}
			right_flat = "";
			for (auto substr : right) {
				right_flat += substr;
			}
		}
		else if (lastIndent > indent) {
			left.pop_front();
			right.pop_back();

			left_flat = "";
			for (auto substr : left) {
				left_flat += substr;
			}
			right_flat = "";
			for (auto substr : right) {
				right_flat += substr;
			}
		}

		compiledInsert(left_flat, data, right_flat);

		prev = rowSplit();
		lastIndent = indent;
	}
}

void Step::_FinalizeDatabase() {
	assert(StepsInsertStmt);
	int rc;

	rc = sqlite3_reset(StepsInsertStmt);
	if (rc != SQLITE_OK) {
		cout << "Error " << rc << " resetting statement" << endl;
		RuntimeError("Error resetting statement");
	}

	string dat = " ";
	
	sqlite3_bind_int64(StepsInsertStmt, 1, lastStepNum + 1);
	sqlite3_bind_int(StepsInsertStmt, 2, 1);
	sqlite3_bind_int(StepsInsertStmt, 3, 1);
	sqlite3_bind_int64(StepsInsertStmt, 4, 0);
	sqlite3_bind_int64(StepsInsertStmt, 5, 0);
	sqlite3_bind_text(StepsInsertStmt, 6, dat.c_str(), dat.length(), SQLITE_STATIC);
	sqlite3_bind_text(StepsInsertStmt, 7, dat.c_str(), dat.length(), SQLITE_STATIC);
	
	assert(sqlite3_step(StepsInsertStmt) == SQLITE_DONE);
	
	assert(StepsUpdateStmt);

	rc = sqlite3_reset(StepsUpdateStmt);
	if (rc != SQLITE_OK) {
		cout << "Error " << rc << " resetting statement" << endl;
		RuntimeError("Error resetting statement");
	}

	sqlite3_bind_int64(StepsUpdateStmt, 1, 0);
	sqlite3_bind_int64(StepsUpdateStmt, 2, 0);
	sqlite3_bind_text(StepsUpdateStmt, 3, dat.c_str(), dat.length(), SQLITE_STATIC);
	sqlite3_bind_int64(StepsUpdateStmt, 4, lastStepNum + 1);
	
	assert(sqlite3_step(StepsUpdateStmt) == SQLITE_DONE);
}

void Step::stepInsert() {
	assert(StepsInsertStmt);
	int rc;

	rc = sqlite3_reset(StepsInsertStmt);
	if (rc != SQLITE_OK) {
		cout << "Error " << rc << " resetting statement" << endl;
		RuntimeError("Error resetting statement");
	}

	sqlite3_bind_int64(StepsInsertStmt, 1, stepNum);
	sqlite3_bind_int(StepsInsertStmt, 2, indent);
	if (begin) {
		sqlite3_bind_int(StepsInsertStmt, 3, ActiveRuleManager::GetActiveRuleID(activeRule));
	}
	else {
		sqlite3_bind_null(StepsInsertStmt, 3);
	}
	sqlite3_bind_int64(StepsInsertStmt, 4, allocs);
	sqlite3_bind_int64(StepsInsertStmt, 5, frees);
	if (data.length() == 0) {
		sqlite3_bind_null(StepsInsertStmt, 6);
	}
	else {
		sqlite3_bind_text(StepsInsertStmt, 6, data.c_str(), data.length(), SQLITE_STATIC);
	}
	if (cookies.length() == 0) {
		sqlite3_bind_null(StepsInsertStmt, 7);
	}
	else {
		sqlite3_bind_blob(StepsInsertStmt, 7, cookies.getBlob(), cookies.length(), SQLITE_TRANSIENT);
	}

	assert(sqlite3_step(StepsInsertStmt) == SQLITE_DONE);
}

void Step::stepUpdate() {
	assert(StepsUpdateStmt);
	int rc;

	rc = sqlite3_reset(StepsUpdateStmt);
	if (rc != SQLITE_OK) {
		cout << "Error " << rc << " resetting statement" << endl;
		RuntimeError("Error resetting statement");
	}

	sqlite3_bind_int64(StepsUpdateStmt, 1, allocs);
	sqlite3_bind_int64(StepsUpdateStmt, 2, frees);

	if (data.length() == 0) {
		sqlite3_bind_null(StepsUpdateStmt, 3);
	}
	else {
		sqlite3_bind_text(StepsUpdateStmt, 3, data.c_str(), data.length(), SQLITE_STATIC);
	}

	sqlite3_bind_int64(StepsUpdateStmt, 4, stepNum);
	
	assert(sqlite3_step(StepsUpdateStmt) == SQLITE_DONE);
}

void Step::parseStepHeader(string &s) {
	bool complete = false;
	size_t offset = s.find("STEP", 2); //Skip to the location of the beginning of 'STEP'
	if (offset == string::npos) {
		cout << "Failed to parse header " << s << endl;
	}
	assert(offset != string::npos); //Fail if not matching the first match
	indent = offset - 2; //store the number of spaces (offset - len('//'))
	
	if (s[offset += 4] == '(') { //Skip to the char after the 'STEP'
		complete = false;
	} else {
		complete = true;
		offset += 3; //Skip the '-OK'
	}
	offset++;

	size_t tmp = offset;
	offset = s.find_first_of(')', offset);
	assert(offset != string::npos);
	string tmpstr = s.substr(tmp, offset - tmp);
	stepNum = stoull(tmpstr);
	
	if (!complete) { //if it is a step begin get the verb
		tmp = offset += 3; //skip the leading space on the verb
		offset = s.find_first_of(' ', tmp);  //assume verbs cannot have spaces
		assert(offset != string::npos);
		activeRule = s.substr(tmp, offset - tmp); //get the verb (excluding tailing space)
		offset += 2; //jump to the alloc first char
	} else {
		activeRule = "";
		offset += 4; //jump to alloc first char
	}

	//Get the allocs count
	tmp = offset; 
	offset = s.find_first_of(',', tmp);
	assert(offset != string::npos);
	tmpstr = s.substr(tmp, offset - tmp);
	allocs = stoull(tmpstr);
	offset += 1;

	//Get the frees count
	tmp = offset;
	offset = s.find_first_of(')', tmp); 
	assert(offset != string::npos);
	frees = stoull(s.substr(tmp, offset - tmp));

	begin = !complete;
}

void Step::initPreparedStatements(sqlite3* db) {
	if (!db)
		throw invalid_argument("Database pointer cannot be NULL");

	int rc = sqlite3_prepare_v2(db, StepInsertTemplate, -1, &StepsInsertStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating steps insert statement" << endl << StepInsertTemplate;
		RuntimeError("Error creating steps insert statement");
	}

	rc = sqlite3_prepare_v2(db, StepUpdateTemplate, -1, &StepsUpdateStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating steps update statement" << endl << StepUpdateTemplate;
		RuntimeError("Error creating steps update statement");
	}

	rc = sqlite3_prepare_v2(db, CompiledInsertTemplate, -1, &CompiledInsertStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating compiled steps insert statement" << endl << CompiledInsertTemplate;
		RuntimeError("Error creating compiled steps insert statement");
	}

	rc = sqlite3_prepare_v2(db, CompiledFirstInsertTemplate, -1, &CompiledFirstInsertStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating compiled steps first insert statement" << endl << CompiledFirstInsertTemplate;
		RuntimeError("Error creating compiled steps first insert statement");
	}
}

void Step::compiledInsert(std::string l, std::string c, std::string r) {
	assert(CompiledInsertStmt);
	assert(CompiledFirstInsertStmt);
	int rc;

	sqlite3_stmt* stmt;
	if (firstWrite) {
		firstWrite = false;
		stmt = CompiledFirstInsertStmt;
	}
	else {
		stmt = CompiledInsertStmt;
	}

	rc = sqlite3_reset(stmt);
	if (rc != SQLITE_OK) {
		cout << "Error " << rc << " resetting statement" << endl;
		RuntimeError("Error resetting statement");
	}

	if (l.length() == 0) {
		sqlite3_bind_text(stmt, 1, "", 0, SQLITE_STATIC);
	}
	else {
		sqlite3_bind_text(stmt, 1, l.c_str(), l.length(), SQLITE_STATIC);
	}

	if (c.length() == 0) {
		sqlite3_bind_text(stmt, 2, "", 0, SQLITE_STATIC);
	}
	else {
		sqlite3_bind_text(stmt, 2, c.c_str(), c.length(), SQLITE_STATIC);
	}

	if (r.length() == 0) {
		sqlite3_bind_text(stmt, 3, "", 0, SQLITE_STATIC);
	}
	else {
		sqlite3_bind_text(stmt, 3, r.c_str(), r.length(), SQLITE_STATIC);
	}

	assert(sqlite3_step(stmt) == SQLITE_DONE);
}

void Step::CreateStepTable(sqlite3* db) {
	if (!db)
		throw invalid_argument("Database pointer cannot be NULL");
	
	char *errMsg = NULL;
	int rc = sqlite3_exec(db, TableSchema, NULL, 0, &errMsg);
	if (rc != SQLITE_OK){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
		RuntimeError("Error creating table");
	}

	rc = sqlite3_exec(db, CompiledSchema, NULL, 0, &errMsg);
	if (rc != SQLITE_OK){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
		RuntimeError("Error creating table");
	}

	rc = sqlite3_exec(db, CompiledTableFixAutoincrement, NULL, 0, &errMsg);
	if (rc != SQLITE_OK){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
		RuntimeError("Error modifying CompiledStatements autoincrement");
	}

	initPreparedStatements(db);
	
	ActiveRuleManager::CreateActiveRuleTable(db);
	CookieManager::CreateCookieTable(db);
}

Step::SplitStep Step::getSection(std::string &s, size_t minOffset) {
	size_t count = -1;
	size_t start = 0;
	size_t stop = s.length();
	bool found = false;
	for (size_t offset = minOffset - 1; offset < s.length(); offset++) {
		if (s[offset] == '[') {
			if (!found) {
				start = offset + 1;
			}
			found = true;
			count++;
		}
		else if (s[offset] == ']') {
			if (!found) {
				fprintf(stderr, "Warning: found closing brace before opening brace at %ld (started at %ld) in %s\n", offset, minOffset, s.c_str());
				return{ "", s, "" };
			}
			count--;
			if (count == 0) {
				stop = offset + 1;
				break;
			}
		}
	}
	return {s.substr(0, start), s.substr(start, stop - start), s.substr(stop)};
}

Step::SplitStep Step::rowSplit() {
	std::string cookie = cookies.cookieList[cookies.cookieList.size() - 1];
	cookie = cookie.substr(0, cookie.find('['));
	size_t offset = data.find_first_of(cookie);
	return getSection(data, offset);
}

