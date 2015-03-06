package crsxviz.application.crsxviz;

import com.airhacks.afterburner.views.FXMLView;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import crsxviz.application.breakpoints.BreakpointsPresenter;
import crsxviz.application.breakpoints.BreakpointsView;
import static crsxviz.application.crsxrunner.Controller.showError;
import crsxviz.application.crsxrunner.RunnerDialog;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.application.rules.RulesView;
import crsxviz.application.terms.TermsPresenter;
import crsxviz.application.terms.TermsView;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.TraceService;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.inject.Inject;

public class CrsxvizPresenter implements Initializable {

    @FXML
    AnchorPane terms;
    @FXML
    AnchorPane rules;
    @FXML
    AnchorPane breakpoints;

    @FXML
    private Parent view;
    @FXML
    private MenuItem close;
    @FXML
    private MenuItem open;
    @FXML
    private Menu options;
    @FXML
    private MenuItem playback;
    @FXML
    private MenuItem about;
    @FXML
    private Menu help;
    @FXML
    private Menu file;

    @Inject
    TraceService ts;

    @Inject
    private String dbpath;

    public RulesPresenter rulesPresenter;
    protected TermsPresenter termsPresenter;
    protected BreakpointsPresenter breakpointsPresenter;

    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private ObservableList<String> observableRules;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TermsView termsView = new TermsView();
        RulesView rulesView = new RulesView();
        BreakpointsView breakpointsView = new BreakpointsView();
        this.termsPresenter = (TermsPresenter) termsView.getPresenter();
        this.rulesPresenter = (RulesPresenter) rulesView.getPresenter();
        this.breakpointsPresenter = (BreakpointsPresenter) breakpointsView.getPresenter();

        addAll(terms, termsView);
        addAll(rules, rulesView);
        addAll(breakpoints, breakpointsView);
    }

    public ObservableList<String> getBreakpoints() {
        return observableBreakpoints;
    }

    public ObservableList<String> getObservableRules() {
        observableRules = FXCollections.observableArrayList();
        getRules().stream().forEach(
                (rule) -> observableRules.add(rule.getValue())
        );
        return observableRules;
    }

    public List<ActiveRules> getRules() {
        return ts.allRules();
    }

    public List<Steps> getSteps() {
        return ts.allSteps();
    }

    @FXML
    void onOpenFile(ActionEvent event) {
        if (dbpath != null) {
            showError("Error!", "A file already open.\nPlease close it before opening a new one");
        } else {
            System.out.println("Opening file...");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Trace File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite3 Database Files", "*.db"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All formats", "*"));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile == null) {
                dbpath = null;
                System.out.println("Browse aborted");
            } else {
                if (selectedFile.exists() && selectedFile.canRead()) {
                    dbpath = selectedFile.getAbsolutePath();
                    System.out.println("File opened OK");
                    loadDb(dbpath);
                } else {
                    showError("Error!", "Specified file is not readable");
                }
            }
        }
    }

    @FXML
    void onRunParser(ActionEvent event) throws IOException {
        if (dbpath != null) {
            showError("Error!", "A file already open.\nPlease close it before opening a new one");
        } else {
            System.out.println("Running parser...");
            RunnerDialog d = new RunnerDialog();
            d.doModal();
            if (d.processingRan()) {
                if (d.getOutFile() != null) {
                    System.out.println("Parser dialog returned database file " + d.getOutFile().getAbsolutePath());
                    dbpath = d.getOutFile().getAbsolutePath();
                    loadDb(dbpath);
                } else {
                    showError("Error!", "RunnerDialog claims processingRan, but no file was returned.");
                }
            } else {
                System.out.println("Processing was not run by RunnerDialog");
            }
        }
    }

    @FXML
    void onCloseFile(ActionEvent event) {
        System.out.println("Closing file...");
        if (dbpath == null) 
            showError("Error!", "Cannot close, no files are open");
        dbpath = null;
        this.clearControls();
    }

    @FXML
    void onAdjustPlayback(ActionEvent event) {
        System.out.println("adjusting playback...");
    }

    @FXML
    void onAbout(ActionEvent event) {
        System.out.println("about...");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    private void loadDb(String db) {
        if (db != null)
            ts.init(db);
        reloadPresenters();
    }
    
    /**
     * Forces a reload of Presenters following a new database being opened.
     * 
     * This effectively shows the new data accessed by the new database.
     */
    private void reloadPresenters() {
        breakpointsPresenter.setCrsxMain(this);
        rulesPresenter.setCrsxMain(this);
        termsPresenter.setCrsxMain(this);
    }
    
    /**
     * Clears the data shown by the presenters. This sets all buttons
     * to their initial states along with erasing data of all lists.
     */
    public void clearControls() {
        termsPresenter.clearDisplay();
        breakpointsPresenter.clearDisplay();
        rulesPresenter.clearDisplay();
    }
    
    /**
     * Returns the name of the database that is currently open.
     * @return Path to the database
     */
    public String getDbName() {
        return dbpath;
    }
    
    /**
     * Add all view nodes to the given pane.
     * 
     * This is a workaround to side step the double loading of
     * AnchorPane which prevents resizing from happening correctly.
     * @param pane 
     * @param view 
     */
    private void addAll(Pane pane, FXMLView view) {
        Node node = view.getViewWithoutRootContainer();
        while (node != null) {
            pane.getChildren().add(node);
            node = view.getViewWithoutRootContainer();
        }
    }
}
