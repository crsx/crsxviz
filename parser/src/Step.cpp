#include <include/Step.h>
#include <stdexcept>
#include <iostream>

using namespace std;

#define assert(x) if(!(x)) throw runtime_error(string("Assertion failed at ") + string(__FILE__) + string(":") + to_string(__LINE__)); 

/** Removes all content before the last space
*   @param s The string to strip
*   @return the string without any leading spaces
*/
string lstrip(string &s) {
	return s.substr(s.find_last_of(' ') + 1, s.length());
}

Step::Step(vector<string> &buf) {
	assert(buf.size() > 0);
	init(buf[0]);
	unsigned int ln = 0;
	
	while (++ln < buf.size() && buf[ln].length() != 0){
		data.append(lstrip(buf[ln]));
	}
	while (++ln < buf.size() && buf[ln].length() == 0);
	
	if (ln < buf.size()) {
		if (buf[ln].find("Cookies") != string::npos) {
			while (++ln < buf.size() && buf[ln].length() != 0){
				if (cookies.length())
					cookies.append(",");
				cookies.append(lstrip(buf[ln]));
			}
		}
	}
}

void Step::print() {
	cout << "Step:\n";
	cout << "\tIndent:      " << indent << endl;
	cout << "\tType:        " << type << endl;
	cout << "\tStep Number: " << stepNum << endl;
	cout << "\tVerb:        " << activeTerm << endl;
	cout << "\tAllocs:      " << allocs << endl;
	cout << "\tFrees:       " << frees << endl;
	cout << "\tData:        " << data << endl;
	cout << "\tCookies:     " << cookies << endl;
}

void Step::insert(sqlite3_stmt *stmt) {
	assert(stmt);
	int rc;

	rc = sqlite3_reset(stmt);
	if( rc != SQLITE_OK ) {
		cout << "Error " << rc << " resetting statement" << endl;
		assert(false);
	}
	
	sqlite3_bind_int64(stmt, 1, stepNum);
	sqlite3_bind_int(stmt, 2, indent);
	sqlite3_bind_int(stmt, 3, (int)begin);
	if (begin) {
		sqlite3_bind_text(stmt, 4, activeTerm.c_str(), activeTerm.length(), SQLITE_STATIC);
	} else {
		sqlite3_bind_null(stmt, 4);
	}
	sqlite3_bind_int64(stmt, 5, allocs);
	sqlite3_bind_int64(stmt, 6, frees);
	if (data.length() == 0) {
		sqlite3_bind_null(stmt, 7);
	} else {
		sqlite3_bind_text(stmt, 7, data.c_str(), data.length(), SQLITE_STATIC);
	}
	if (cookies.length() == 0) {
		sqlite3_bind_null(stmt, 8);
	} else {
		sqlite3_bind_text(stmt, 8, cookies.c_str(), cookies.length(), SQLITE_STATIC);
	}

	assert(sqlite3_step(stmt) == SQLITE_DONE);
}

void Step::init (string s) {
	bool complete = false;
	size_t offset = s.find("STEP", 2); //Skip to the location of the beginning of 'STEP'
	assert(offset != string::npos); //Fail if not matching the first match
	indent = offset - 2; //store the number of spaces (offset - len('//'))
	
	if (s[offset += 4] == '(') { //Skip to the char after the 'STEP'
		type = "BEGIN";
	} else {
		complete = true;
		type = "COMPLETE";
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
		activeTerm = s.substr(tmp, offset - tmp); //get the verb (excluding tailing space)
		offset += 2; //jump to the alloc first char
	} else {
		activeTerm = "";
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

sqlite3_stmt* Step::prepareStatement(sqlite3* db) {
	if (!db)
		throw invalid_argument("Database cannot be NULL");
	
	sqlite3_stmt *stmt;
	int rc = sqlite3_prepare(db, statementTemplate, -1, &stmt, 0);
	if ( rc != SQLITE_OK ){
		throw runtime_error("Error creating prepared statement");
	}
	return stmt;
}
