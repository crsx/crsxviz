#include <vector>
#include <string>
#include <sqlite3.h>

/** Processes step stages from crsxviz output from a crsx compiler into database entries
*/
class Step {
public:
	/** Initialize a new step from a list of lines
	*   @param buf The lines of data read in to build into the step
	*   @throws runtime_error when there is an error parsing the data
	*/
	Step (std::vector<std::string> &buf);

	/** Prints an overview of the step data to stdout
	*/
	void print();
	
	/** Inserts the step into the sqlite3 DB <br />
	*   Clears and populates the entries in the prepared statement then runs the command
	*   @param stmt The prepared statement to populate with data and insert on
	*	@throws runtime_error when there is an error inserting the row
	*/
	void insert(sqlite3_stmt *stmt);
	
	/** Creates a preparedStatement for use by 
	*   @param db the database to use when creating the prepared statement
	*   @return A prepared statement for use in future step processing
	*   @throws invalid_argument The db was not opened or was null
	*   @throws runtime_error There was an error creating the prepared statement
	*/
	static sqlite3_stmt* prepareStatement(sqlite3* db);

	/** The indentation level of the step
	*/
	int indent;
	
	/** Begin or Complete 
	*/
	std::string type; //TODO:add support for FAIL
	
	/** Flag for if the step is a begin or complete
	*/
	bool begin;
	
	/** The step number identifier
	*/
	unsigned long long stepNum;
	
	/** The term being executed this in this step
	*   @note This field is only populated for step begins
	*/
	std::string activeTerm;
	
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
	std::string cookies;
	
private:
	/** The template for the prepared statement used for inserting entries
	*/
	static constexpr const char * statementTemplate = R"(INSERT INTO `Raw`(`StepNum`,`Indentation`,`Begin`,`ActiveTerm`,`Allocs`,`Frees`,`Data`,`Cookies`) VALUES (?,?,?,?,?,?,?,?);)";

	/** Initializes components of the step from the first line of data
	*   @param s The line to parse
	*   @throws runtime_error when the string does not match the expected format
	*/
	void init (std::string s);
};
