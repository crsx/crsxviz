#include <include/Cookies.h>
#include <stdexcept>
#include <iostream>
#include <cstring>
#include <arpa/inet.h>
#include <include/RuntimeError.h>

using namespace std;

vector<string> CookieManager::CookieList;
sqlite3_stmt* CookieManager::CookieInsertStmt = NULL;

void CookieManager::CreateCookieTable(sqlite3* db) {
#ifndef _NO_COOKIES
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
#endif
}

int CookieManager::GetCookieID(string s) {
#ifndef _NO_COOKIES
	unsigned int i = 0;
	bool matched = false;
	for (i = 0; i < CookieList.size(); i++) {
		if (s.compare(CookieList[i]) == 0) {
			matched = true;
			break;
		}
	}
	if (!matched) {
		int rc = sqlite3_reset(CookieInsertStmt);
		if (rc != SQLITE_OK) {
			cout << "Error " << rc << " resetting statement" << endl;
			RuntimeError("Error resetting statement");
		}
		rc = sqlite3_bind_int(CookieInsertStmt, 1, i);
		if (rc != SQLITE_OK) {
			cout << "Error " << rc << " binding ID" << endl;
			RuntimeError("Error binding ID");
		}
		rc = sqlite3_bind_text(CookieInsertStmt, 2, s.c_str(), s.length(), SQLITE_STATIC);
		if (rc != SQLITE_OK) {
			cout << "Error " << rc << " binding text" << endl;
			RuntimeError("Error binding text");
		}
		rc = sqlite3_step(CookieInsertStmt);
		if(rc != SQLITE_DONE) {
			cout << "Cookies insert prepared statement processing error " << rc << endl;
			RuntimeError("Prepared statement failed");
		}
		CookieList.push_back(s);
	}
	return i;
#else
	return -1;
#endif
}

void CookieManager::initPreparedStatements(sqlite3* db) {
	if (!db)
		throw invalid_argument("Database pointer cannot be NULL");

	int rc = sqlite3_prepare_v2(db, CookieInsertTemplate, -1, &CookieInsertStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating cookie insert statement" << endl << CookieInsertTemplate;
		RuntimeError("Error creating cookie insert statement");
	}
}

void BlobCookie::addCookie(string s) {
#ifndef _NO_COOKIES
	if (s.length() == 0)
		return;
	cookieIDs.push_back(CookieManager::GetCookieID(s));	
#endif
}

BlobCookie::BlobCookie() {
	blob = NULL;
}

BlobCookie::~BlobCookie() {
	if (blob)
		free(blob);
	blob = NULL;
}

size_t BlobCookie::length() {
#ifndef _NO_COOKIES
	return cookieIDs.size() * sizeof(uint32_t);
#else
	return 0;
#endif
}

void * BlobCookie::getBlob() {
#ifndef _NO_COOKIES
	if (blob)
		free(blob);
	blob = NULL;
	
	blob = (char*) malloc(length());
	if (blob == NULL)
		RuntimeError("Error allocating memory for cookie blob");
	
	size_t ofs = 0;
	for (auto i : cookieIDs) {
		uint32_t j = htonl(i);
		memcpy(blob + ofs, &j, sizeof(j)); 
		ofs += sizeof(j);
	}
	return blob;
#else
	return NULL;
#endif
}