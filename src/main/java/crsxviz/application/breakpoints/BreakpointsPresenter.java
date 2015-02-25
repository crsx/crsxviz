package crsxviz.application.breakpoints;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.services.TraceService;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javax.inject.Inject;

public class BreakpointsPresenter implements Initializable {
	
    @FXML private ListView<String> breakpoint_list;
    @FXML private MenuButton bp_menu;

    @Inject private TraceService ts;
    
    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private List<ActiveRules> rules;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rules = ts.allRules();

        final ContextMenu bp_cmenu = new ContextMenu();
        MenuItem bp_cmItem = new MenuItem("Remove Breakpoint");
        bp_cmItem.setOnAction((event) ->{
                String breakpoint = breakpoint_list.getSelectionModel().getSelectedItem();
                observableBreakpoints.remove(breakpoint);
                System.out.println("Removed breakpoint: " + breakpoint);
        });

        bp_cmenu.getItems().add(bp_cmItem);
        breakpoint_list.addEventHandler(MouseEvent.MOUSE_CLICKED, 
            (event) -> {
                    if (event.getButton() == MouseButton.SECONDARY)
                            bp_cmenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
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
                            for (ActiveRules rule : rules) 
                                    if (p.matcher(rule.getValue()).find() && !breakpoint_list.getItems().contains(rule.getValue()))
                                            observableBreakpoints.add(rule.getValue());
                        } catch (PatternSyntaxException e) {
                                Alert alert = new Alert(AlertType.WARNING);
                                alert.setTitle("Invalid Regular Expression");
                                alert.setHeaderText(null);
                                alert.setContentText(e.getMessage());
                                alert.showAndWait();
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
    
    public void setCrsxMain(CrsxvizPresenter main) {
        observableBreakpoints = main.getBreakpoints();
        breakpoint_list.setItems(observableBreakpoints);
    }
}
