BASE_DIR=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
CRSXVIZ_DIR := $(BASE_DIR)../..

ODIR = $(CRSXVIZ_DIR)/obj
BINDIR = $(CRSXVIZ_DIR)/bin
IDIR = $(BASE_DIR)/include
SRCDIR = $(BASE_DIR)/src

GLOBAL_OPTS = -O3 -I$(IDIR) -I$(BASE_DIR) -Wall
CXXFLAGS = -std=c++0x $(GLOBAL_OPTS)
CXX = g++
LIBS = -lpthread -ldl -lsqlite3

all: crsx_parse

_OBJ = main.o Step.o Cookies.o ActiveRules.o CrsD.o
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