package crsxviz.application.rules;

import crsxviz.application.Utilities;
import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.RuleDetails;
import crsxviz.persistence.services.IDataService;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.*;
import javafx.scene.paint.Color;

public class RulesPresenter extends AnchorPane implements DataListener {

    @FXML
    private TextField filter_field;
    @FXML
    private ListView<Text> rules_list;

    private IDataService ts;

    private ObservableList<Text> observableBreakpoints;
    private ObservableList<Text> observableRules;
    
    private static RulesPresenter presenter;

    public RulesPresenter() {
        initialize();
    }

    private void initialize() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(RulesPresenter.class.getResource("rules.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(RulesPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        presenter = loader.<RulesPresenter>getController();
        
        rules_list.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> onEntityClicked());
    }
    
    public static RulesPresenter getPresenter() {
        return presenter;
    }

    @FXML
    void setBreakpoint() {
        Text breakpoint = new Text(rules_list.getSelectionModel().getSelectedItem().getText());
        if (breakpoint.getText().contains("\n")) {
            breakpoint.setText(breakpoint.getText().substring(0, breakpoint.getText().indexOf("\n")));
        }
        if (!Utilities.contains(observableBreakpoints, breakpoint.getText())) { 
            observableBreakpoints.add(breakpoint);
            System.out.println("Breakpoint set on: " + breakpoint.getText());
        } else 
            System.out.println("Breakpoint " + breakpoint.getText() + " already set.");
    }

    public void setService(IDataService service) {
        this.ts = service;
        ts.addListener(this);
    }

    private void setFilteredRules(FilteredList<Text> list) {
        filter_field.textProperty().addListener((observable, oldValue, newValue) -> {
            list.setPredicate(String -> {
                //empty filter
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                return String.getText().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        rules_list.setItems((FilteredList<Text>) list);
    }

    private void onEntityClicked() {
        Text result = new Text("");
        Text selection = rules_list.selectionModelProperty().getValue().getSelectedItem();
        if (selection.getText().contains("\n")) {
            result.setText(selection.getText().substring(0, selection.getText().indexOf("\n")));
        } else {
            List<RuleDetails> l = ts.getRuleDetails(selection.getText());
            result.setText(RuleDetails.toString(l));
        }
        for (int i = 0; i < observableRules.size(); i++) {
            if (observableRules.get(i).equals(selection)) {
                observableRules.set(i, result);
            }
        }
    }

    /**
     * Highlights the given rule in the Rules pane
     *
     * @param ruleId
     */
    public void highlightActiveRule(int ruleId) {
        rules_list.getSelectionModel().select(ruleId);
        rules_list.getFocusModel().focus(ruleId);
    }

    @Override
    public void dataLoaded() {
        observableRules = ts.allObservableRules();
        observableBreakpoints = ts.allObservableBreakpoints();

        rules_list.setItems(observableRules);
        setFilteredRules(new FilteredList<>(observableRules, p -> true));
    }

    @Override
    public void dataClosed() {
        observableBreakpoints = FXCollections.observableArrayList();
        observableRules = FXCollections.observableArrayList();
        rules_list.setItems(observableRules);
        filter_field.clear();
    }

    public void setNextRule(String nextRule) {
        for (Text txt : rules_list.getItems()) {
            if (txt.getText().equals(nextRule)) {
                txt.setFill(Color.BLUE);
            }
        }
    }
}
