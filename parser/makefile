GLOBAL_OPTS = -O3 -I$(IDIR) -I. -Wall
CXXFLAGS = -std=c++0x $(GLOBAL_OPTS)
CXX = g++
ODIR = ./obj
LIBS = -lpthread -ldl -lsqlite3
BINDIR = ./bin
IDIR = ./include
SRCDIR = ./src

all: crsx_parse

_OBJ = main.o Step.o Cookies.o ActiveRules.o
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))

$(ODIR)/%.o: $(SRCDIR)/%.cpp
	mkdir -p $(ODIR)
	$(CXX) -c -o $@ $< $(CXXFLAGS)

crsx_parse: $(OBJ)
	mkdir -p $(BINDIR)
	$(CXX) -o $(BINDIR)/$@ $^ $(CXX_OPTS) $(GLOBAL_OPTS) $(LIBS)

.PHONY: clean

clean:
	rm -rf $(ODIR)
	rm -rf $(BINDIR)
	
docs:
	doxygen

cleandocs:
	rm -rf ./docs