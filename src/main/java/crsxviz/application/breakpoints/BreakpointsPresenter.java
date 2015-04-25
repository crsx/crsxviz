package crsxviz.application.breakpoints;

import crsxviz.application.Utilities;
import static crsxviz.application.crsxrunner.Controller.showError;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.services.IDataService;
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
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

public class BreakpointsPresenter extends AnchorPane implements DataListener {

    @FXML
    private ListView<Text> breakpoint_list;

    private IDataService ts;

    private ObservableList<Text> observableBreakpoints = FXCollections.observableArrayList();
    private List<ActiveRules> rules;
    
    private static BreakpointsPresenter presenter;

    public BreakpointsPresenter() {
        initialize();
    }
    
    private void initialize() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(BreakpointsPresenter.class.getResource("breakpoints.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(RulesPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        presenter = loader.<BreakpointsPresenter>getController();
    }
    
    public static BreakpointsPresenter getPresenter() {
        return presenter;
    }

    @FXML
    void setNewBreakpoint() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set New Breakpoint by RegEx");
        dialog.setContentText("Enter your RegEx Rule: ");
        dialog.setHeaderText(null);
        Optional<String> result = dialog.showAndWait();

        result.ifPresent((exp) -> {
            try {
                for (ActiveRules rule : rules) {
                    String currentRule = rule.getValue();
                    if (currentRule.matches(exp) && !Utilities.contains(breakpoint_list.getItems(), currentRule)){
                        observableBreakpoints.add(new Text(currentRule));
                    }
                }
            } catch (PatternSyntaxException e) {
                showError("Error", "Invalid Regular Expression");
            }
        });
    }

    @FXML
    void removeAll() {
        observableBreakpoints.clear();
        System.out.println("All breakpoints removed");
    }

    @FXML
    public void removeBreakpoint(ActionEvent event) {
        Text breakpoint = breakpoint_list.getSelectionModel().getSelectedItem();
        observableBreakpoints.remove(breakpoint);
        System.out.println("Removed breakpoint: " + breakpoint.getText());
    }

    public void setService(IDataService service) {
        this.ts = service;
        ts.addListener(this);
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
