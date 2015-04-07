package crsxviz.persistence.services;

import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.DispatchedRules;
import crsxviz.persistence.beans.Steps;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public enum DatabaseService {
    INSTANCE;
    
    private static final String DEFAULT_DATABASE = "out.db";

    private static ObservableList<String> breakpoints;

    private static String dbName;
    private static String url;
    
    public static DatabaseService getInstance(String dbName) {
        DatabaseService.init(dbName);
        return INSTANCE;
    }
    
    public static void init() {
        init(DEFAULT_DATABASE);
    }

    public static void init(String dbpath) {
        dbName = dbpath;
        breakpoints = FXCollections.observableArrayList();
        url = "jdbc:sqlite:" + dbpath;
        if (!url.endsWith(".db")) {
            url += ".db";
        }
    }

    public String getDbName() {
        return (dbName != null) ? dbName : "";
    }

    public List<ActiveRules> allRules() {
        return ActiveRules.loadAllRules(url);
    }

    public List<Cookies> allCookies() {
        return Cookies.loadAllCookies(url);
    }

    public List<Steps> allSteps() {
        return Steps.loadAllSteps(url);
    }

    public List<CompiledSteps> allCompiledSteps() {
        return CompiledSteps.loadAll(url);
    }
    
    public List<DispatchedRules> allDispatchedRules() {
        return DispatchedRules.loadAllDispatchtedRules(url);
    }

    public CompiledSteps getCompiledStep(Long num) {
        return CompiledSteps.loadStep(url, num);
    }

    public ObservableList<String> allObservableBreakpoints() {
        return breakpoints;
    }

    public ObservableList<String> allObservableRules() {
        ObservableList list = FXCollections.observableArrayList();
        allRules().stream().forEach(
                (rule) -> list.add(rule.getValue())
        );
        return list;
    }
}

