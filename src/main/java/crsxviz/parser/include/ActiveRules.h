#ifndef _ACTIVERULES_H
#define _ACTIVERULES_H

#include <vector>
#include <string>
#include <sqlite3.h>

/** Manager for keeping track of Rules for deduplication and assigning IDs
*/
class ActiveRuleManager {
public:
	/** Initializes the ActiveRules table in the database and sets up the prepared statements for it
	*   @param db The database to use for storing the active rules.
	*/
	static void CreateActiveRuleTable(sqlite3* db);
	
	/** Gets the ActiveRuleID of the string
	*   @return the ActiveRuleID
	*   @param s The string to lookup the ID for
	*/
	static int GetActiveRuleID(std::string &s);

private:
	/** Template for creating a prepared statement to perform inserts into ActiveRules
	*/
	static constexpr const char * ActiveRuleInsertTemplate = R"(INSERT INTO `ActiveRules`
	(`ActiveRuleID`,`Value`) VALUES (?,?);)";

	/** SQL for creating the ActiveRules table in the database
	*/
	static constexpr const char * TableSchema = R"(CREATE TABLE `ActiveRules` (
	`ActiveRuleID`    INTEGER NOT NULL PRIMARY KEY UNIQUE,
	`Value`           TEXT);)";
	
	/** The in memory copy of the ActiveRules table
	*   @note The index of each string corresponds to its ActiveRuleID
	*/
	static std::vector<std::string> ActiveRules;
	
	/** Initializes the prepared statements for the ActiveRules table
	*   @param db The database to use for the ActiveRules table
	*/
	static void initPreparedStatements(sqlite3* db);
	
	/** Static class internal storage of prepared statement for Step table inserts
	*/
	static sqlite3_stmt* ActiveRuleInsertStmt;
};
#endif