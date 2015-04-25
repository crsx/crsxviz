package crsxviz.persistence.services;

import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Cookies;
import crsxviz.persistence.beans.DispatchedRules;
import crsxviz.persistence.beans.RuleDetails;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.DataListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.*;

public class DataService implements IDataService {
    
    private final List<DataListener> listeners = new ArrayList<>();

    private static ObservableList<Text> breakpoints;

    private String dbName;
    private String url = new String();
    
    @Override
    public String getDataName() {
        return (dbName != null) ? dbName : "";
    }
    
    @Override
    public void setDataName(String dbpath) {
        this.dbName = dbpath;
        breakpoints = FXCollections.observableArrayList();
        url = "jdbc:sqlite:" + dbpath;
        if (!url.endsWith(".db")) {
            url += ".db";
        }
    }

    @Override
    public List<RuleDetails> getRuleDetails(String ruleName) {
        return url.isEmpty() ? new LinkedList<>() : ActiveRules.getRuleDetails(ruleName, url);
    }

    @Override
    public List<ActiveRules> allRules() {
        return url.isEmpty() ? new LinkedList<>() : ActiveRules.loadAllRules(url);
    }

    @Override
    public List<Cookies> allCookies() {
        return url.isEmpty() ? new LinkedList<>() : Cookies.loadAllCookies(url);
    }

    @Override
    public List<Steps> allSteps() {
        return url.isEmpty() ? new LinkedList<>() : Steps.loadAllSteps(url);
    }

    @Override
    public List<CompiledSteps> allCompiledSteps() {
        return url.isEmpty() ? new LinkedList<>() : CompiledSteps.loadAll(url);
    }

    @Override
    public List<DispatchedRules> allDispatchedRules() {
        return url.isEmpty() ? new LinkedList<>() : DispatchedRules.loadAllDispatchtedRules(url);
    }

    @Override
    public CompiledSteps getCompiledStep(Long num) {
        return url.isEmpty() ? new CompiledSteps() : CompiledSteps.loadStep(url, num);
    }

    @Override
    public ObservableList<Text> allObservableBreakpoints() {
        return breakpoints;
    }

    @Override
    public ObservableList<Text> allObservableRules() {
        ObservableList list = FXCollections.observableArrayList();
        for (ActiveRules rule : allRules())
            list.add(new Text(rule.getValue()));
        return list;
    }
    
    @Override
    public void addListener(DataListener toAdd) {
        if (!listeners.contains(toAdd))
            listeners.add(toAdd);
    }
    
    @Override
    public void dataRequiresReload() {
        listeners.stream().forEach(
            (listener) -> listener.dataLoaded()
        );
    }
    
    @Override
    public void dataClosed() {
        listeners.stream().forEach(
            (listener) -> listener.dataClosed()
        );
    }
}
