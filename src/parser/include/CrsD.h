#include <regex>
#include <vector>

extern "C" {
	#include <sqlite3.h>
}

/** Manages the loading and processing of .crsD files containing dispatchified rule data.
*/
class CrsDFile {
public:	
	/** Loads a set of files by name and processes them
	*	@param files The list of files to load in order
	*/
	CrsDFile(std::vector<std::string> files);

	/** Writes the loaded rules to the specified database
	*	@param db The database to update
	*/
	void write(sqlite3* db);

private:
	/** The preprocessed regular expression for the rule start indicator
	*/
	static std::regex dRuleStart;
	
	/** The preprocessed regular expression for the rule bodies 
	*/
	static std::regex dRuleContent;

	/** POD storage for a table row to insert
	*/
	typedef struct Dispatch_s {
		std::string SrcRuleName;
		std::string SrcRuleOffset;
		std::string SrcRuleIdent;
		std::string SrcRuleArgs;
		std::string StartState;
		std::string EndState;
	} Dispatch;

	/** POD storae for the rule header to update the ActiveRules table with
	*/
	typedef struct Rule_s {
		std::string Name;
		std::string Env;
		std::string Args;
		std::string ResultType;
		std::vector<Dispatch> handlers;
	} Rule;

	/** Takes lines of file data and groups them into sections which should contain rule data
	*	@param lines The set of lines that were read in
	*	@return multiline strings of contiguous sections of data
	*/
	std::vector<std::string> groupify(std::vector<std::string> lines);

	/** Gets the indentation of a line, used in groupify processing
	*	@param s The string to process
	*	@return The number of spaces before a non-whitespace char
	*/
	size_t getLeftSpaces(std::string s);

	/** Reads a set of files into a set of lines
	*	@param The list of files to read in
	*	@return The set of lines in the files
	*/
	std::vector<std::string> readfile(std::vector<std::string> files);

	/** The set of currently processed rules
	*/
	std::vector<Rule> rules;
};
