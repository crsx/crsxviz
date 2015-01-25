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
	crsx_parse <database> [inputfile]
	Warning: if database exists it will be overwritten
	If inputfile is not specified it will parse stdin until EOF
	
Sample usage:
	crsx/samples/factorial/fact wrapper=Fact term=75 crsxviz | crsx_parse /tmp/factorial75.db

Database format:
    TABLE `Steps` (
	`StepNum`        INTEGER NOT NULL PRIMARY KEY UNIQUE,
	`Indentation`    INTEGER,
	`ActiveTerm`     TEXT,
	`StartAllocs`    INTEGER,
	`StartFrees`	 INTEGER,
	`CompleteAllocs` INTEGER,
	`CompleteFrees`  INTEGER,
	`StartData`      TEXT,
	`CompleteData`   TEXT,
	`Cookies`        TEXT)
