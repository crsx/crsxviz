package crsxviz.application.rules;

import crsxviz.persistence.DataListener;
import crsxviz.persistence.services.DataService;
import crsxviz.persistence.beans.RuleDetails;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.*;
import javafx.scene.paint.Color;

public class RulesPresenter extends AnchorPane implements DataListener {

    @FXML
    private TextField filter_field;
    @FXML
    private ListView<Text> rules_list;
    
    private DataService ts;

    private ObservableList<Text> observableBreakpoints = FXCollections.observableArrayList();
    private ObservableList<Text> observableRules = FXCollections.observableArrayList();
    
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
        
        ts = DataService.getInstance();
        ts.addListener(this);
        
        // Generate Context Menu for Rules
        final ContextMenu cMenu = new ContextMenu();
        MenuItem cmItem = new MenuItem("Set Breakpoint");
        cmItem.setOnAction(
                (event) -> {
                    Text breakpoint = rules_list.getSelectionModel().getSelectedItem();
                    if (breakpoint.getText().contains("\n")) {
                    	breakpoint.setText(breakpoint.getText().substring(0, breakpoint.getText().indexOf("\n")));
                    }
                    observableBreakpoints.add(breakpoint);
                    System.out.println("Breakpoint set on: " + breakpoint);
                }
        );

        cMenu.getItems().add(cmItem);
        rules_list.addEventHandler(MouseEvent.MOUSE_CLICKED,
                (event) -> {
                	this.onEntityClicked();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        cMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
                    }
                }
        );
    }
    
    public void setDbService(DataService service) {
        this.ts = service;
    }

    /**
     * Initializes the Presenter to an initial state where either a
     * database has been opened and thus will display the correct state of
     * buttons along with initial term tree, or where a database has not been 
     * opened.
     */
    public void initiateData() {
        observableRules = ts.allObservableRules();
        observableBreakpoints = ts.allObservableBreakpoints();
        
        rules_list.setItems(observableRules);
        setFilteredRules(new FilteredList<>(observableRules, p -> true));
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
    
    public void onEntityClicked() {
    	String result = "";
    	String selection = rules_list.selectionModelProperty().getValue().getSelectedItem().getText();
    	if (selection.contains("\n")) {
    		result = selection.substring(0, selection.indexOf("\n"));
    	} else {
	    	List<RuleDetails> l = ts.getRuleDetails(selection);
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
    	for ( Text txt : rules_list.getItems()) {
    		if (txt.getText().equals(nextRule)) {
    			txt.setFill(Color.BLUE);
    		}
    	}
    }
}
