#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <exception>
#include <sqlite3.h>
#include <include/Step.h>
#include <errno.h>
#include <include/RuntimeError.h>

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
	cout << "Usage: " << argv0 << " <database> [inputfile]" << endl;
	exit(-1);
}

/** Program entry point
*/
int main(int argc, char* argv[]) {
	char* dbpath = NULL;
	if (argc >= 2 && argc <= 3) {
		dbpath = argv[1];
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
	if (argc == 3) {
		cout << "Loading from " << argv[2] << endl;
		src = new ifstream(argv[2], ios_base::in);
	} else {
		cout << "Running on stdin" << endl;
		src = &cin;
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

	sqlite3_close(db);
}