#include <vector>
#include <string>
extern "C" {
#include <sqlite3.h>
}
#include <include/Cookies.h>
#include <deque>

/** Processes step stages from crsxviz output from a crsx compiler into database entries
*/
class Step {
public:
	
	/** Initializes the Step table
	*	@param db The database object to use
	*/
	static void CreateStepTable(sqlite3* db);

	/** Initialize a new step from a list of lines
	*   @param buf The lines of data read in to build into the step
	*   @throws runtime_error when there is an error parsing the data
	*/
	Step (std::vector<std::string> &buf);

	/** Creates a garbage step to use for an end of table pad.
	*	@note This is a hack. Fix the processing in the GUI to not require this
	*/
	static void _FinalizeDatabase();
	
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
	size_t indent;

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

	static void compiledInsert(std::string, std::string, std::string);
	
private:
		
	/** The step number identifier
	*/
	static unsigned long long lastStepNum;
	

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

	/** Static class internal storage of prepared statement for completing step stages
	*/
	static sqlite3_stmt* CompiledInsertStmt;

	/** Static class internal storage of prepared statement for completing step stages
	*/
	static sqlite3_stmt* CompiledFirstInsertStmt;

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

	/** The table schema for the CompiledSteps table
	*/
	static constexpr const char * CompiledSchema = R"(CREATE TABLE `CompiledSteps`(
	id INTEGER PRIMARY KEY   AUTOINCREMENT,
	left TEXT,
	center TEXT,
	right TEXT);)";

	/** Prepared statement template for doing inserts into CompiledSteps table
	*/
	static constexpr const char * CompiledInsertTemplate = R"(INSERT INTO `CompiledSteps`
	(`left`, `center`, `right`)
	VALUES (?, ?, ?);)";

	/** Prepared statement template for doing the first step insert into CompiledSteps
	*/
	static constexpr const char * CompiledFirstInsertTemplate = R"(INSERT INTO `CompiledSteps`
	(`id`, `left`, `center`, `right`)
	VALUES (0,?, ?, ?);)";

	/** SQL command for fixing the initial index of the CompiledSteps table to 0
	*/
	static constexpr const char * CompiledTableFixAutoincrement = R"(INSERT INTO `SQLITE_SEQUENCE`
	(seq, name) VALUES ('-1', 'CompiledSteps');)";

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

	/** A tuple for storing the sections of a compiled step term context
	*/
	typedef struct SplitStep_s {
		std::string left;
		std::string center;
		std::string right;
	}SplitStep;

	/** Gets the section of the string enclosed by the outer most []
	*   @param s The string to parse
	*   @return The left, center, and right subsections of the string delimited by []
	*/
	SplitStep getSection(std::string &s, size_t minOffset = 0);

	/** Splits step into the data before and after the currently active rule indicator
	*/
	SplitStep rowSplit();

	/** The indentation of the last step that was processed
	*/
	static size_t lastIndent;

	/** The left side of the current full context term (treated as a stack)
	*/
	static std::deque<std::string> left;

	/** The right side of the current full context term (treated as a reversed stack)
	*/
	static std::deque<std::string> right;

	/** The last step that was processed 
	*/
	static Step::SplitStep prev;
	
	/** Preprocessed snapshot of the flattened version of the left side of the context term stack
	*/
	static std::string left_flat;
	
	/** Preprocessed snapshot of the flattened version of the right side of the context term stack
	*/
	static std::string right_flat;

	/** Flag for determining which type of insert to use for CompiledSteps
	*/
	static bool firstWrite;
};
