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
import crsxviz.persistence.services.DatabaseService;
import java.io.File;
import java.io.IOException;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    private DatabaseService ts = DatabaseService.getInstance("out.db");

    private String dbpath;

    private static RulesPresenter rulesPresenter;
    private static TermsPresenter termsPresenter;
    private static BreakpointsPresenter breakpointsPresenter;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TermsView termsView = new TermsView();
        RulesView rulesView = new RulesView();
        BreakpointsView breakpointsView = new BreakpointsView();
        CrsxvizPresenter.termsPresenter = (TermsPresenter) termsView.getPresenter();
        CrsxvizPresenter.rulesPresenter = (RulesPresenter) rulesView.getPresenter();
        CrsxvizPresenter.breakpointsPresenter = (BreakpointsPresenter) breakpointsView.getPresenter();

        CrsxvizPresenter.breakpointsPresenter.setDbService(ts);
        CrsxvizPresenter.termsPresenter.setDbService(ts);
        CrsxvizPresenter.rulesPresenter.setDbService(ts);
        
        addAll(terms, termsView);
        addAll(rules, rulesView);
        addAll(breakpoints, breakpointsView);
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
    
    public void setService(DatabaseService ts) {
        this.ts = ts;
        DatabaseService.init(this.ts.getDbName());
        reloadPresenters();
    }
    
    /**
     * Load the database pointed to by db. 
     * After loading the database, this method will force
     * the Presenters to reload, effectively updating them with
     * the loaded database
     * 
     * @param db absolute path to the database
     */
    private void loadDb(String db) {
        if (db != null)
            DatabaseService.init(db);
        reloadPresenters();
    }
    
    /**
     * Forces a reload of Presenters following a new database being opened.
     * 
     * This effectively shows the new data accessed by the new database.
     */
    private void reloadPresenters() {
        breakpointsPresenter.initiateData();
        rulesPresenter.initiateData();
        termsPresenter.initiateData();
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
    
    /**
     * Used to initialize the application with a test database.
     * testInitialize requires the TraceService testInstance
     * @param dbname Name of test database
     */
    public void testInitialize(String dbname) {
        loadDb(dbname);
    }
    
    /**
     * Retrieve the presenter of the rule view. 
     * 
     * @return Retrieve the presenter for the rule view.
     */
    public static RulesPresenter getRulesPresenter() {
        return rulesPresenter;
    }
}
