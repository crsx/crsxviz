#!/usr/bin/python
# coding=UTF-8

import os
import subprocess
import sqlite3
import re
import codecs
import argparse

dRuleStart = re.compile(ur'^\s*(\{[^\}\n]+\})?\s*([^\[\n]+)?\s*\[([^\]]+)\]\s*::\s*([^;]+?)\s*;$', re.M)
dRuleContent = re.compile(ur'^\s*\'?(\S+)-(\d+)([^\[\]]+)?\'?\[([\S\s]+?)\]\s*\:\s*([\S\s]+?)?\s*→\s*([\s\S]+?)\s*\;\s*$', (re.M|re.U))

global args

def Print(s):
    global args
    if args != None and args.v:
        print(unicode(s))


def getLeftSpaces(string):
    count = 0
    for c in string:
        if c.isspace():
            count += 1
        else:
            return count

def groupify(lines):
    grps = list()
    lineNum = 0
    while lineNum < len(lines):
        blob = str()
        fIndent = getLeftSpaces(lines[lineNum])
        blob += lines[lineNum][fIndent:]
        lineNum += 1
        while lineNum < len(lines) and getLeftSpaces(lines[lineNum]) > fIndent:
            blob += lines[lineNum][fIndent:]
            lineNum += 1
        grps.append(blob)
    return grps

        
def doMain():
    global args
    assert(args != None)
    
    dirFiles = os.listdir(args.Path)
    crsFiles = list()
    for f in dirFiles:
        if f.endswith(".crs"):
            crsFiles.append(f)

    Print("Compiling " + str(len(crsFiles)) + " files")

    for f in crsFiles:
        p = subprocess.Popen(['make','-C', args.Path, f + "D"], close_fds=True)
        (progStdOut, progStdErr) = p.communicate()
        if p.returncode != 0:
            Print("Error: Compiling dispatchified rules for " + f + " failed")
            Print("stderr: " + progStdErr)
            Print("stdout: " + progStdOut)
            Print("Aborting compilation")
            raise SystemExit("Dispatch rule generation error")

    dirFiles = os.listdir(args.Path)
    dFiles = list()
    for f in dirFiles:
        if f.endswith(".crsD"):
            dFiles.append(f)

    if len(dFiles) == 0:
        Print("Error: No dispatchified rules found")
        raise SystemExit("No rule files dispatched")

    Print("\nParsing " + str(len(dFiles)) + " dispatched rule files")

    foundRules = dict()
    for f in dFiles:
        Print("Processing dispatch file " + args.Path + f)
        with codecs.open(args.Path + f, "r", encoding="utf-8") as fd:
            lines = fd.readlines()
            activeGroup = 0
            groups = groupify(lines[1:-1])
            Print("Found " + str(len(groups)) + " content groups")
            while activeGroup < len(groups):
#                Print("Group #: " + str(activeGroup)) 
                line = groups[activeGroup]
                activeGroup += 1
                m = dRuleStart.match(line)
                if m:
                    Print("Found dispatch rule: " + m.group(2))
                    foundRules[m.group(2)] = [m,list()]
                    while activeGroup < len(groups):
#                        Print("Group #: " + str(activeGroup))
                        ctx = groups[activeGroup]
                        r = dRuleContent.match(ctx)
                        if r:
                            activeGroup += 1
                            foundRules[m.group(2)][1].append(r)
                            Print("\tRule distpatch loaded")
                        else:
                            #Print("Group did not match dRuleContent\n======================")
                            #Print(line)
                            #Print('======================')
                            break
                    if len(foundRules[m.group(2)][1]) == 0:
                        Print("Warning: no dispatches found for rule name " + m.group(2))
#                else:
#                    if "::" in line:
#                        Print("Group did not match dRuleStart\n======================")
#                        Print(line)
#                        Print('======================')
#                    if u"→" in unicode(line):
#                        Print(u"→ found at start matcher")
                    
    if len(foundRules) == 0:
        Print("\nNo dispatch rules found!")
        raise SystemExit("No dispatch rules found!")

    Print("\nAll rules loaded")
    Print("Updating database file " + args.TraceFile)
    
    conn = sqlite3.connect(args.TraceFile)
    sql = conn.cursor()
    
    sql.execute("PRAGMA table_info('ActiveRules');")
    tableCols = len(sql.fetchall())
    if tableCols == 2:
        Print("Database is missing RuleBody column. Adding it")
        sql.execute("ALTER TABLE `ActiveRules` ADD COLUMN 'Env' TEXT;")
        sql.execute("ALTER TABLE `ActiveRules` ADD COLUMN 'Args' TEXT;")
        sql.execute("ALTER TABLE `ActiveRules` ADD COLUMN 'ResultType' TEXT;")
        sql.execute("ALTER TABLE `ActiveRules` ADD COLUMN 'UsedInTrace' INTEGER;")
        sql.execute("UPDATE `ActiveRules` SET UsedInTrace=1;")
        sql.execute("CREATE TABLE `DispatchedRules`(ActiveRuleID INTEGER, SrcRuleName TEXT, SrcRuleOffset INTEGER, SrcRuleIdent TEXT, SrcRuleArgs TEXT, StartState TEXT, EndState TEXT);")
    elif tableCols == 5:
        Print("\nDatabase already has RuleBody column.\nNotice: all existing rule body lines will be overwritten")
    else:
        Print("Unsupported existing column count in ActiveRules")
        SystemExit("Unsupported existing column count in ActiveRules")
    
    Print(r"Removing [\d+] from rule names")
    existingRows = list()
    sql.execute("SELECT `Value`, `ActiveRuleID` FROM `ActiveRules`;")
    for tpl in sql.fetchall():
        tmpM = re.match(r'([^\[]+)\[\d+\]', tpl[0])
        if tmpM == None:
            Print("Failed to match expected Rule name format for " + tpl[0])
            continue
        minName = tmpM.group(1)
        sql.execute("UPDATE `ActiveRules` SET `Value`=? WHERE ActiveRuleID=" + str(tpl[1]) + ";", (minName,))
        Print("Changed " + tpl[0] + " to " + minName + " at index " + str(tpl[1]))
        existingRows.append(minName)
    
    Print("\nAdding rule dispatch data to trace")
    for name in foundRules:
        if name not in existingRows:
            sql.execute("INSERT INTO `ActiveRules` (`Value`) VALUES (?);", (name,))
            Print("Adding non-trace rule " + name)
        sql.execute('UPDATE `ActiveRules` SET `Env`=? WHERE `Value`=?;', (foundRules[name][0].group(1), name))
        sql.execute('UPDATE `ActiveRules` SET `Args`=? WHERE `Value`=?;', (foundRules[name][0].group(3), name))
        sql.execute('UPDATE `ActiveRules` SET `ResultType`=? WHERE `Value`=?;', (foundRules[name][0].group(4), name))
        Print("Updated active rule " + name)
        for r in foundRules[name][1]:
            Print("\tAdding dispatched rule data")
            sql.execute('INSERT INTO `DispatchedRules` VALUES ((SELECT ActiveRuleID FROM `ActiveRules` WHERE `Value`=?),?,?,?,?,?,?);', (name, r.group(1), r.group(2), r.group(3), r.group(4), r.group(5), r.group(6)))
    
    conn.commit()    
    Print("\nTrace update completed")
    conn.close()
    
if __name__ == "__main__":
    global args

    parser = argparse.ArgumentParser(description='A proof of concept parser for loading crsx rules into a crsxviz debug trace')
    parser.add_argument("Path", help="Path to the source directory for the crsx program", default="./")
    parser.add_argument("TraceFile", help="The trace file to store the rules in")
    parser.add_argument("-q", "--quiet",  default=True, action="store_false", help="Disables status information being written", dest="v")
    args = parser.parse_args()
    
    doMain()