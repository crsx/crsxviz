Requirements:
	libsqlite3-dev (provides -lsqlite3 for g++)
	make
	g++ > 4.9.0
	doxygen (to build docs)

Compiling:
	Run `make`

Generating code documentation:
	Run `make docs`

Usage:
	crsx_parse <database> <CRS source file directory> [inputfile]
	Warning: if database exists it will be overwritten
	CRS source file directory should contain the .crs files that were used with crsx to create the program being run.
	If inputfile is not specified the parser will run against stdin until EOF
	
Sample usage:
	crsx/samples/factorial/fact wrapper=Fact term=75 crsxviz | crsx_parse /tmp/factorial75.db crsx/samples/factorial/fact/

Database Schema:
    TABLE `Steps` (
	`StepNum`        INTEGER NOT NULL PRIMARY KEY UNIQUE,
	`Indentation`    INTEGER,
	`ActiveRuleID`   INTEGER,
	`StartAllocs`    INTEGER,
	`StartFrees`	 INTEGER,
	`CompleteAllocs` INTEGER,
	`CompleteFrees`  INTEGER,
	`StartData`      TEXT,
	`CompleteData`   TEXT,
	`Cookies`        BLOB)
	
	TABLE `CompiledSteps`(
	`id` 			 INTEGER PRIAMRY KEY AUTO INCREMENT,
	`left`			 TEXT,
	`center` 		 TEXT, 
	`right` 		 TEXT)
	
	TABLE `ActiveRules` (
	`ActiveRuleID`   INTEGER NOT NULL PRIMARY KEY UNIQUE,
	`Value`          TEXT,
	`Env` 			 TEXT,
	`Args` 			 TEXT,
	`ResultType` 	 TEXT,
	`UsedInTrace` 	 INTEGER)
	
	TABLE `Cookies` (
	`CookieID`       INTEGER NOT NULL PRIMARY KEY UNIQUE,
	`Value`          TEXT)
	
	TABLE `DispatchedRules` (
	`ActiveRuleID` 	 INTEGER,
	`SrcRuleName` 	 TEXT,
	`SrcRuleOffset`  INTEGER,
	`SrcRuleIdent` 	 TEXT,
	`SrcRuleArgs` 	 TEXT,
	`StartState` 	 TEXT,
	`EndState` 		 TEXT)
	
Cookies BLOB format:
	Array of network byte order (big endian) unsiged 32 bit ints. 
	Each int in the array refers to a CookieID in the Cookies table.

CompiledSteps table:
	Concatenating the left, center, and right columns produces the entire term of that is being processed
	The currently being processed section of the term is held in the `center` column
	
DispatchedRules table:
	This table contains the dispatchified rules as output by the CRSX compiler
	The SrcRule columns refer to the information given in the dispatchified rules for identifying the source rule (in the crs files) used to create the dispatchified rule
	The start state and end states are the values to the left and right of the -> in the dispatchified rule
	
ActiveRules table:
	The `Value` column contains the rule that is being executed
	The `Env` column contains the environment variables that are attached to the rules
	The `Args` column contains the arguments that the dispatchified rule takes to produce its output
	The `ResultType` column is sometimes set with the type of the rule
	`UsedInTrace` Indicates whether or not the rule will be used in the trace