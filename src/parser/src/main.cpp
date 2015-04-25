#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <exception>
#include <sqlite3.h>
#include <include/Step.h>
#include <errno.h>
#include <include/RuntimeError.h>
#include <include/CrsD.h>
#include <dirent.h>
#include <unistd.h>

using namespace std;

/** Ensures correct PRAGMA settings are set up for db
*/
void optimizeDB(sqlite3* db) {
	char *errMsg = NULL;
	int rc = sqlite3_exec(db, "PRAGMA synchronous=OFF", NULL, NULL, &errMsg);
	if( rc != SQLITE_OK ){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
	}
	rc = sqlite3_exec(db, "PRAGMA count_changes=OFF", NULL, NULL, &errMsg);
	if( rc != SQLITE_OK ){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
	}
	rc = sqlite3_exec(db, "PRAGMA journal_mode=OFF", NULL, NULL, &errMsg);
	if( rc != SQLITE_OK ){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
	}
	rc = sqlite3_exec(db, "PRAGMA temp_store=MEMORY", NULL, NULL, &errMsg);
	if( rc != SQLITE_OK ){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
	}
}

/** Prints program usage and exits
*/
void usage(char* argv0) {
	cout << "Usage: " << argv0 << " <database> <crs file directory> [inputfile]" << endl;
	exit(-1);
}

vector<string> getCrsFiles(string basepath) {
	vector<string> tmp;
	
	DIR *dp;
	struct dirent *dirp;
	if ((dp = opendir(basepath.c_str())) == NULL) {
		RuntimeError("Failed to open CRS file directory")
	}

	while ((dirp = readdir(dp)) != NULL) {
		string fname = string(dirp->d_name);
		if (fname.find(".crs") != string::npos && fname.find(".crsD") == string::npos) {
			tmp.push_back(string(basepath).append("/").append(fname).append("D"));
		}
	}
	closedir(dp);

	return tmp;
}

/** Program entry point
*/
int main(int argc, char* argv[]) {
	vector<string> crsfiles;
	char* dbpath = NULL;
	if (argc >= 3 && argc <= 4) {
		dbpath = argv[1];
		crsfiles = getCrsFiles(string(argv[2]));
		if (crsfiles.size() == 0) {
			RuntimeError("No .crs files found in specified directory")
		}
	} else {
		usage(argv[0]);
	}
	
	int rc = remove(dbpath);
	if (rc != 0 && errno != 2) {
		cout << "Error " << errno << " deleting existing database" << endl;
		exit(rc);
	}
	
	sqlite3 * db = NULL;
	rc = sqlite3_open(dbpath, &db);
	if (rc) {
		cout << "Error opening database: " << sqlite3_errmsg(db) << endl;
		exit(-1);
	}

	cout << "Database " << dbpath << " opened OK" << endl;

	Step::CreateStepTable(db);
	
	optimizeDB(db);

	istream * src;
	if (argc == 4) {
		cout << "Loading from " << argv[3] << endl;
		src = new ifstream(argv[3], ios_base::in);
	} else {
		cout << "Running on stdin" << endl;
		src = &cin;
	}

	if (chdir(argv[2]) != 0) {
		cout << "Error " << errno << " while changing directories to " << argv[2] << endl;
		RuntimeError("Error while changing directories");
	}

	for (string file : crsfiles) {
		string cmd = "make ";
		cmd.append(file);
		if (system(cmd.c_str()) != 0) {
			cout << "Error while compiling " << file.c_str() << endl;
			RuntimeError("Error while compiling crs file");
		}
	}

	vector<string> buf;
	string s;
	string lastLine;
	bool prevBlank = false;
	do {
		getline(*src, s);
		if (src->eof())
			break;
		if (!s.length()) {
			if (!prevBlank)
				buf.push_back(s);
			prevBlank = true;
			continue;
		} else {
			prevBlank = false;
			lastLine = s;
		}

		if (buf.size() > 0 && s.find("STEP", 2) != string::npos) {
                        Step r = Step(buf);
                        r.pushToDB();
                        buf.clear();
		}
		buf.push_back(s);
	} while (true);

	if (buf.size() > 1){
		buf.pop_back(); //remove result line
		Step r = Step(buf);
                r.pushToDB();
                buf.clear();
	}
	cout << "Result: " << lastLine << endl;

	Step::compiledInsert("", lastLine, "");

	CrsDFile dispatch(crsfiles);
	dispatch.write(db);

	sqlite3_close(db);
}
