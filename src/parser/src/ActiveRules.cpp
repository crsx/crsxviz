#include <include/ActiveRules.h>
#include <stdexcept>
#include <iostream>
#include <cstring>
#include <include/RuntimeError.h>

using namespace std;

vector<string> ActiveRuleManager::ActiveRules;
sqlite3_stmt* ActiveRuleManager::ActiveRuleInsertStmt = NULL;

void ActiveRuleManager::CreateActiveRuleTable(sqlite3* db) {
	if (!db)
		throw invalid_argument("Database pointer cannot be NULL");
	
	char *errMsg = NULL;
	int rc = sqlite3_exec(db, TableSchema, NULL, 0, &errMsg);
	if (rc != SQLITE_OK){
		fprintf(stderr, "SQL error: %s\n", errMsg);
		sqlite3_free(errMsg);
		RuntimeError("Error creating table");
	}
	initPreparedStatements(db);
}

int ActiveRuleManager::GetActiveRuleID(string &s) {
	unsigned int i = 0;
	bool matched = false;
	for (i = 0; i < ActiveRules.size(); i++) {
		if (s.compare(ActiveRules[i]) == 0) {
			matched = true;
			break;
		}
	}
	if (!matched) {
		int rc = sqlite3_reset(ActiveRuleInsertStmt);
		if (rc != SQLITE_OK) {
			cout << "Error " << rc << " resetting statement" << endl;
			RuntimeError("Error resetting statement");
		}
		rc = sqlite3_bind_int(ActiveRuleInsertStmt, 1, i);
		if (rc != SQLITE_OK) {
			cout << "Error " << rc << " binding text" << endl;
			RuntimeError("Error binding text");
		}
		rc = sqlite3_bind_text(ActiveRuleInsertStmt, 2, s.c_str(), s.length(), SQLITE_STATIC);
		if (rc != SQLITE_OK) {
			cout << "Error " << rc << " binding text" << endl;
			RuntimeError("Error binding text");
		}
		rc = sqlite3_step(ActiveRuleInsertStmt);
		if(rc != SQLITE_DONE) {
			cout << "Active rules prepared statement processing error " << rc << endl;
			RuntimeError("Error executing prepared statement");
		}
		ActiveRules.push_back(s);
	}
	return i;
}

void ActiveRuleManager::initPreparedStatements(sqlite3* db) {
	if (!db)
		throw invalid_argument("Database pointer cannot be NULL");

	int rc = sqlite3_prepare_v2(db, ActiveRuleInsertTemplate, -1, &ActiveRuleInsertStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating active term insert statement" << endl << ActiveRuleInsertTemplate;
		RuntimeError("Error creating active rule insert statement");
	}
}