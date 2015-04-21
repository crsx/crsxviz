package crsxviz.application.breakpoints;

import static crsxviz.application.crsxrunner.Controller.showError;
import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.services.DataService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.*;

public class BreakpointsPresenter extends AnchorPane implements Initializable, DataListener {

    @FXML
    private ListView<Text> breakpoint_list;
    @FXML
    private MenuButton bp_menu;

    private DataService ts;

    private ObservableList<Text> observableBreakpoints = FXCollections.observableArrayList();
    private List<ActiveRules> rules;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rules = new ArrayList<>();
        ts = DataService.getInstance();
        ts.addListener(this);
    }

    @FXML
    public void setNewBreakpoint() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set New Breakpoint by RegEx");
        dialog.setContentText("Enter your RegEx Rule: ");
        dialog.setHeaderText(null);
        Optional<String> result = dialog.showAndWait();

        result.ifPresent((exp) -> {
            try {
                for (ActiveRules rule : rules) {
                    String currentRule = rule.getValue();
                    if (currentRule.matches(exp) && !breakpoint_list.getItems().contains(currentRule)) {
                        System.out.println("pattern matched " + exp);
                        observableBreakpoints.add(new Text(currentRule));
                    }
                }
            } catch (PatternSyntaxException e) {
                showError("Error", "Invalid Regular Expression");
            }
        });
    }

    @FXML
    public void removeAll() {
        observableBreakpoints.clear();
        System.out.println("All breakpoints removed");
    }

    @FXML
    public void removeBreakpoint(ActionEvent event) {
        Text breakpoint = breakpoint_list.getSelectionModel().getSelectedItem();
        observableBreakpoints.remove(breakpoint);
        System.out.println("Removed breakpoint: " + breakpoint.getText());
    }

    public void setDbService(DataService service) {
        this.ts = service;
    }

    /**
     * Initializes the Presenter to an initial state where either a database has
     * been opened and thus will display the correct state of buttons along with
     * initial term tree, or where a database has not been opened.
     */
    public void initiateData() {
        observableBreakpoints = ts.allObservableBreakpoints();
        breakpoint_list.setItems(observableBreakpoints);
    }

    @Override
    public void dataLoaded() {
        observableBreakpoints = ts.allObservableBreakpoints();
        breakpoint_list.setItems(observableBreakpoints);
        rules = ts.allRules();
    }
    
        @Override
    public void dataClosed() {
        observableBreakpoints = FXCollections.observableArrayList();
        breakpoint_list.setItems(observableBreakpoints);
        rules = null;
    }
}
