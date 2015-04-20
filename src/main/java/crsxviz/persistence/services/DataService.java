package crsxviz.persistence.services;

import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.DispatchedRules;
import crsxviz.persistence.beans.RuleDetails;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.DataListener;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.*;

public enum DataService {

    INSTANCE;

    private static final String DEFAULT_DATABASE = "out.db";
    
    private List<DataListener> listeners = new ArrayList<>();

    private static ObservableList<Text> breakpoints;

    private static String dbName;
    private static String url;

    public static DataService getInstance(String dbName) {
        DataService.init(dbName);
        return INSTANCE;
    }
    
    public static DataService getInstance() {
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

    public List<RuleDetails> getRuleDetails(String ruleName) {
        return ActiveRules.getRuleDetails(ruleName, url);
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

    public ObservableList<Text> allObservableBreakpoints() {
        return breakpoints;
    }

    public ObservableList<Text> allObservableRules() {
        ObservableList list = FXCollections.observableArrayList();
        allRules().stream().forEach(
                (rule) -> list.add(new Text(rule.getValue()))
        );
        return list;
    }
    
    public void addListener(DataListener toAdd) {
        listeners.add(toAdd);
    }
    
    public void dataReloaded() {
        for (DataListener listener: listeners)
            listener.dataLoaded();
    }
    
    public void dataClosed() {
        for (DataListener listener: listeners)
            listener.dataClosed();
    }
}
