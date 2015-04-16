package crsxviz.application.breakpoints;

import static crsxviz.application.crsxrunner.Controller.showError;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.services.DatabaseService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;


public class BreakpointsPresenter extends AnchorPane implements Initializable {

    @FXML
    private ListView<String> breakpoint_list;
    @FXML
    private MenuButton bp_menu;
    
    private DatabaseService ts;

    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private List<ActiveRules> rules;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rules = new ArrayList<>();

        final ContextMenu bp_cmenu = new ContextMenu();
        MenuItem bp_cmItem = new MenuItem("Remove Breakpoint");
        bp_cmItem.setOnAction((event) -> {
            String breakpoint = breakpoint_list.getSelectionModel().getSelectedItem();
            observableBreakpoints.remove(breakpoint);
            System.out.println("Removed breakpoint: " + breakpoint);
        });

        bp_cmenu.getItems().add(bp_cmItem);
        breakpoint_list.addEventHandler(MouseEvent.MOUSE_CLICKED,
                (event) -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        bp_cmenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
                    }
                });

        // Generate Breakpoint Menu
        MenuItem addBP = new MenuItem("Set New Breakpoint");
        MenuItem removeAll = new MenuItem("Remove All Breakpoints");
        addBP.setOnAction((event) -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Set New Breakpoint by RegEx");
            dialog.setContentText("Enter your RegEx Rule: ");
            dialog.setHeaderText(null);
            Optional<String> result = dialog.showAndWait();

            result.ifPresent((exp) -> {
                try {
                    Pattern p = Pattern.compile(exp);
                    for (ActiveRules rule : rules) {
                        if (p.matcher(rule.getValue()).find() && !breakpoint_list.getItems().contains(rule.getValue())) {
                            observableBreakpoints.add(rule.getValue());
                        }
                    }
                } catch (PatternSyntaxException e) {
                    showError("Error", "Invalid Regular Expression");
                }
            });

        }
        );

        removeAll.setOnAction(
                (event) -> {
                    observableBreakpoints.clear();
                    System.out.println("All breakpoints removed");
                });

        bp_menu.getItems().addAll(addBP, removeAll);

    }
    
    public void setDbService(DatabaseService service) {
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

    /**
     * Return the Presenter to its initial state where no database is to be
     * displayed.
     */
    public void clearDisplay() {
        observableBreakpoints = FXCollections.observableArrayList();
        breakpoint_list.setItems(observableBreakpoints);
        rules = null;
    }
}
