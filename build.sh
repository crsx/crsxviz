#!/bin/bash
echo 'Building crsx_parser'
make

echo 'Building crsxviz.jar'
JAVA_HOME=$JAVA_8_HOME
mvn -e clean install