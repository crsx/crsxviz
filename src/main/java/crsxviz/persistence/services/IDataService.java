package crsxviz.persistence.services;

import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.*;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import java.util.List;

public interface IDataService {

    String getDataName();
    
    void setDataName(String dbpath);

    List<RuleDetails> getRuleDetails(String ruleName);

    List<ActiveRules> allRules();

    List<Cookies> allCookies();

    List<Steps> allSteps();

    List<CompiledSteps> allCompiledSteps();

    List<DispatchedRules> allDispatchedRules();

    CompiledSteps getCompiledStep(Long num);

    ObservableList<Text> allObservableBreakpoints();

    ObservableList<Text> allObservableRules();

    void addListener(DataListener toAdd);

    void dataRequiresReload();

    void dataClosed();
}
