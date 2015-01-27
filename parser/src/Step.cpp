#include <include/Step.h>
#include <stdexcept>
#include <iostream>

using namespace std;

#define assert(x) if(!(x)) throw runtime_error(string("Assertion failed at ") + string(__FILE__) + string(":") + to_string(__LINE__)); 

sqlite3_stmt* Step::StepsInsertStmt = NULL;
sqlite3_stmt* Step::StepsUpdateStmt = NULL;

/** Removes all content before the last space
* @param s The string to strip
* @return the string without any leading spaces
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
	cout << "\tBegin:       " << begin << endl;
	cout << "\tStep Number: " << stepNum << endl;
	cout << "\tVerb:        " << activeTerm << endl;
	cout << "\tAllocs:      " << allocs << endl;
	cout << "\tFrees:       " << frees << endl;
	cout << "\tData:        " << data << endl;
	cout << "\tCookies:     " << cookies << endl;
}

void Step::stepInsert() {
	assert(StepsInsertStmt);
	int rc;

	rc = sqlite3_reset(StepsInsertStmt);
	if (rc != SQLITE_OK) {
		cout << "Error " << rc << " resetting statement" << endl;
		throw runtime_error("Error resetting statement");
	}

	sqlite3_bind_int64(StepsInsertStmt, 1, stepNum);
	sqlite3_bind_int(StepsInsertStmt, 2, indent);
	if (begin) {
		sqlite3_bind_text(StepsInsertStmt, 3, activeTerm.c_str(), activeTerm.length(), SQLITE_STATIC);
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
		sqlite3_bind_text(StepsInsertStmt, 7, cookies.c_str(), cookies.length(), SQLITE_STATIC);
	}

	assert(sqlite3_step(StepsInsertStmt) == SQLITE_DONE);
}

void Step::stepUpdate() {
	assert(StepsUpdateStmt);
	int rc;

	rc = sqlite3_reset(StepsUpdateStmt);
	if (rc != SQLITE_OK) {
		cout << "Error " << rc << " resetting statement" << endl;
		throw runtime_error("Error resetting statement");
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

void Step::initPreparedStatements(sqlite3* db) {
	if (!db)
		throw invalid_argument("Database cannot be NULL");

	int rc = sqlite3_prepare(db, StepInsertTemplate, -1, &StepsInsertStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating steps insert statement" << endl << StepInsertTemplate;
		throw runtime_error("Error creating steps insert statement");
	}

	rc = sqlite3_prepare(db, StepUpdateTemplate, -1, &StepsUpdateStmt, 0);
	if (rc != SQLITE_OK){
		cout << "Error " << rc << " creating steps update statement" << endl << StepUpdateTemplate;
		throw runtime_error("Error creating steps update statement");
	}
}
