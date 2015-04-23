package crsxviz.application.breakpoints;

import crsxviz.application.Utilities;
import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.services.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import static crsxviz.application.crsxrunner.Controller.showError;

public class BreakpointsPresenter extends AnchorPane implements DataListener {

    @FXML
    private ListView<Text> breakpoint_list;

    private DataService ts;

    private ObservableList<Text> observableBreakpoints = FXCollections.observableArrayList();
    private List<ActiveRules> rules;

    public BreakpointsPresenter() {
        initialize();
    }

    private void initialize() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("breakpoints.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

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
                    if (currentRule.matches(exp) && !Utilities.contains(breakpoint_list.getItems(), currentRule)) {
                        //if (currentRule.matches(exp) && !breakpoint_list.getItems().contains(currentRule)) {
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
