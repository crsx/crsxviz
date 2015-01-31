#include <vector>
#include <string>
#include <sqlite3.h>
#include <include/Cookies.h>

/** Processes step stages from crsxviz output from a crsx compiler into database entries
*/
class Step {
public:
	
	static void CreateStepTable(sqlite3* db);

	/** Initialize a new step from a list of lines
	*   @param buf The lines of data read in to build into the step
	*   @throws runtime_error when there is an error parsing the data
	*/
	Step (std::vector<std::string> &buf);

	/** Chooses the proper statement to run and executes it
	*   @throws runtime_error There was an error inserting the DB into the database
	*/
	inline void pushToDB() {
		if (begin) {
			stepInsert();
		}
		else {
			stepUpdate();
		}
	}

	/** The indentation level of the step
	*/
	int indent;

	/** Flag for if the step is a begin or complete
	*/
	bool begin;
	
	/** The step number identifier
	*/
	unsigned long long stepNum;
	
	/** The rule being executed this in this step
	*   @note This field is only populated for step begins
	*/
	std::string activeRule;
	
	/** The allocations done by the compiler at this step
	*/
	unsigned long long allocs;
	
	/** The allocations that have been freed at this step
	*/
	unsigned long long frees;
	
	/** The contents of the full term processed by the step
	*/
	std::string data;
	
	/** The cookies that were detected by the compiler
	*/
	BlobCookie cookies;
	
private:
	/** Initializes Step static prepared statements for use with the DB
	*   @param db the database to use when creating the prepared statements
	*   @throws invalid_argument The db was not opened or was null
	*   @throws runtime_error There was an error creating the prepared statement
	*/
	static void initPreparedStatements(sqlite3* db);
	
	/** Static class internal storage of prepared statement for Step table inserts
	*/
	static sqlite3_stmt* StepsInsertStmt;
	
	/** Static class internal storage of prepared statement for completing step stages
	*/
	static sqlite3_stmt* StepsUpdateStmt;

	static constexpr const char * TableSchema = R"(CREATE TABLE `Steps` (
	`StepNum`        INTEGER NOT NULL PRIMARY KEY UNIQUE,
	`Indentation`    INTEGER,
	`ActiveRuleID`   INTEGER,
	`StartAllocs`    INTEGER,
	`StartFrees`	 INTEGER,
	`CompleteAllocs` INTEGER,
	`CompleteFrees`  INTEGER,
	`StartData`      TEXT,
	`CompleteData`   TEXT,
	`Cookies`        BLOB
	);)";
	
	/** Prepared statement template for inserting a start into the correlated table
	*/
	static constexpr const char * StepInsertTemplate = R"(INSERT INTO `Steps`(
	`StepNum`,
	`Indentation`,
	`ActiveRuleID`,
	`StartAllocs`,
	`StartFrees`,
	`StartData`,
	`Cookies`) VALUES (?,?,?,?,?,?,?);)";

	/** Prepared statement template for adding the Step Complete information to the correlated table
	*/
	static constexpr const char * StepUpdateTemplate = R"(UPDATE Steps SET 
	CompleteAllocs = ?, CompleteFrees = ?, CompleteData = ?
	WHERE StepNum = ?)";

	/** Uses StepsInsertStmt to add the Step to the database
	*   @throws runtime_error There was an error inserting into the database
	*/
	void stepInsert();

	/** Uses StepsUpdateStmt to add the Step complete entry to the database
	*   @throws runtime_error There was an error inserting into the database
	*/
	void stepUpdate();

	/** Initializes components of the step from the first line of data
	*   @param s The line to parse
	*   @throws runtime_error when the string does not match the expected format
	*/
	void parseStepHeader (std::string &s);
};
