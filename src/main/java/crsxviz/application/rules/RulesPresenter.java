package crsxviz.application.rules;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class RulesPresenter implements Initializable {

    @FXML
    private TextField filter_field;
    @FXML
    private ListView<String> rules_list;

    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private ObservableList<String> observableRules = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Generate Context Menu for Rules
        final ContextMenu cMenu = new ContextMenu();
        MenuItem cmItem = new MenuItem("Set Breakpoint");
        cmItem.setOnAction(
                (event) -> {
                    String breakpoint = rules_list.getSelectionModel().getSelectedItem();
                    observableBreakpoints.add(breakpoint);
                    System.out.println("Breakpoint set on: " + breakpoint);
                }
        );

        cMenu.getItems().add(cmItem);
        rules_list.addEventHandler(MouseEvent.MOUSE_CLICKED,
                (event) -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        cMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
                    }
                }
        );
        
    }

    /**
     * Initializes the Presenter to an initial state where either a
     * database has been opened and thus will display the correct state of
     * buttons along with initial term tree, or where a database has not been 
     * opened.
     * @param main Instance of the calling presenter
     */
    public void setCrsxMain(CrsxvizPresenter main) {
        observableRules = main.getObservableRules();
        observableBreakpoints = main.getBreakpoints();
        
        rules_list.setItems(observableRules);
        setFilteredRules(new FilteredList<>(observableRules, p -> true));
    }
    
    private void setFilteredRules(FilteredList<String> list) {
        filter_field.textProperty().addListener((observable, oldValue, newValue) -> {
            list.setPredicate(String -> {
                //empty filter
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                return String.toLowerCase().contains(newValue.toLowerCase());
            });
        });

        rules_list.setItems((FilteredList<String>) list);
    }
    
    /**
     * Highlights the given rule in the Rules pane
     * @param ruleId 
     */
    public void highlightActiveRule(int ruleId) {
        rules_list.getSelectionModel().select(ruleId);
        rules_list.getFocusModel().focus(ruleId);
    }
    
    /**
     * Return the Presenter to its initial state where no database is to be 
     * displayed.
     */
    public void clearDisplay() {
        observableBreakpoints = FXCollections.observableArrayList();
        observableRules = FXCollections.observableArrayList();
        rules_list.setItems(observableRules);
        filter_field.clear();
    }
}
