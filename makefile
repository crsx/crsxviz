BASE_DIR = $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

CRSXVIZ_DIR = $(BASE_DIR)
PARSER_SRC_DIR = $(BASE_DIR)/src/parser
DEB_DIR = $(BASE_DIR)/deb_build

DPKG_FOUND := $(shell which dpkg > /dev/null; echo $$?)
DPKGDEB_FOUND := $(shell which dpkg-deb > /dev/null; echo $$?)

INST_BIN_DIR = /usr/bin

.PHONY: parser
parser:
	@echo "building parser at $(PARSER_SRC_DIR)/makefile"
	+make all -f $(PARSER_SRC_DIR)/makefile CRSXVIZ_DIR=$(CRSXVIZ_DIR)

viz:
	mvn clean package
	
jar:
	mvn clean package
	
install: jar parser
	chmod a+x $(DEB_DIR) -R
	cp $(BASE_DIR)/crsxviz.jar $(INST_BIN_DIR)/
	cp $(DEB_DIR)/$(INST_BIN_DIR)/crsxviz $(INST_BIN_DIR)/
	cp $(BASE_DIR)/bin/crsx_parse $(INST_BIN_DIR)/

remove:
	rm -f $(INST_BIN_DIR)/crsx_parse
	rm -f $(INST_BIN_DIR)/crsxviz
	rm -f $(INST_BIN_DIR)/crsxviz.jar

isDebPlatform:
ifeq ($(DPKG_FOUND),1)
	@echo "Not a Debian based platform. Cannot build .deb package"
	exit 1
endif
ifeq ($(DPKGDEB_FOUND),1)
	@echo "dpkg-deb not found. Cannot build .deb package"
	exit 1
endif
	
deb: isDebPlatform jar parser
	mkdir -p $(DEB_DIR)/$(INST_BIN_DIR)/
	mkdir -p $(DEB_DIR)/$(INST_BIN_DIR)/
	cp $(BASE_DIR)/crsxviz.jar $(DEB_DIR)/$(INST_BIN_DIR)/
	cp $(BASE_DIR)/bin/crsx_parse $(DEB_DIR)/$(INST_BIN_DIR)/
	chmod 755 $(DEB_DIR)/DEBIAN -R
	chmod a+x $(DEB_DIR)/usr -R
	dpkg-deb --build $(DEB_DIR)
	mv $(BASE_DIR)/deb_build.deb $(BASE_DIR)/crsxviz.deb
	
.PHONY: clean	
clean:
	rm -rf $(BASE_DIR)/bin/
	rm -f $(BASE_DIR)/crsxviz.jar
	rm -f $(BASE_DIR)/crsxviz.deb
	+make clean -f $(PARSER_SRC_DIR)/makefile CRSXVIZ_DIR=$(CRSXVIZ_DIR)
