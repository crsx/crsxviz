#include <regex>
#include <vector>

extern "C" {
	#include <sqlite3.h>
}

class CrsDFile {
public:	
	CrsDFile(std::vector<std::string> files);
	void write(sqlite3* db);

private:
	static std::regex dRuleStart;
	static std::regex dRuleContent;

	typedef struct Dispatch_s {
		std::string SrcRuleName;
		std::string SrcRuleOffset;
		std::string SrcRuleIdent;
		std::string SrcRuleArgs;
		std::string StartState;
		std::string EndState;
	} Dispatch;

	typedef struct Rule_s {
		std::string Name;
		std::string Env;
		std::string Args;
		std::string ResultType;
		std::vector<Dispatch> handlers;
	} Rule;

	std::vector<std::string> groupify(std::vector<std::string> lines);
	size_t getLeftSpaces(std::string s);
	std::vector<std::string> readfile(std::vector<std::string> files);

	std::vector<Rule> rules;
};
