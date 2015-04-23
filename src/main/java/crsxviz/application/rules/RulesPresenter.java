package crsxviz.application.rules;

import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.RuleDetails;
import crsxviz.persistence.services.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RulesPresenter extends AnchorPane implements DataListener {

    @FXML
    private TextField filter_field;
    @FXML
    private ListView<Text> rules_list;
    
    private DataService ts;

    private ObservableList<Text> observableBreakpoints;
    private ObservableList<Text> observableRules;
    
    public RulesPresenter() {
        initialize();
    }
    
    private void initialize() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("rules.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        ts = DataService.getInstance();
        ts.addListener(this);

        rules_list.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> onEntityClicked());
    }

    @FXML
    public void setBreakpoint() {
        Text breakpoint = new Text(rules_list.getSelectionModel().getSelectedItem().getText());
        if (breakpoint.getText().contains("\n")) {
            breakpoint.setText(breakpoint.getText().substring(0, breakpoint.getText().indexOf("\n")));
        }
        observableBreakpoints.add(breakpoint);
        System.out.println("Breakpoint set on: " + breakpoint);
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

        rules_list.setItems(list);
    }

    private void onEntityClicked() {
        String result = "";
        Text selection = rules_list.selectionModelProperty().getValue().getSelectedItem();
        if (selection.getText().contains("\n")) {
            result = selection.getText().substring(0, selection.getText().indexOf("\n"));
        } else {
            List<RuleDetails> l = ts.getRuleDetails(selection.getText());
            result = RuleDetails.toString(l);
    	}
    	for (int i = 0; i < observableRules.size(); i++) {
    		if (observableRules.get(i).equals(selection)) {
    			observableRules.set(i,  new Text(result));
    		}
    	}
    }
    
    /**
     * Highlights the given rule in the Rules pane
     * @param ruleId id of the rule to be highlighted
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
        

    public void setNextRule(int nextRule) {
     	rules_list.getItems().get(nextRule).setFill(Color.BLUE);
       	rules_list.getItems().get(nextRule).setFont(Font.font("System", FontWeight.BOLD, 12));
    }
}
