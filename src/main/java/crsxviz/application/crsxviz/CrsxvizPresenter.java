package crsxviz.application.crsxviz;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import crsxviz.application.breakpoints.BreakpointsPresenter;
import static crsxviz.application.crsxrunner.Controller.showError;
import crsxviz.application.crsxrunner.RunnerDialog;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.application.terms.TermsPresenter;
import crsxviz.persistence.services.DataService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CrsxvizPresenter implements Initializable {

    private static final String RULES = "../rules/rules.fxml";
    private static final String BREAKPOINTS = "../breakpoints/breakpoints.fxml";
    private static final String TERMS = "../terms/terms.fxml";
    
    @FXML
    AnchorPane terms;
    @FXML
    AnchorPane rules;
    @FXML
    AnchorPane breakpoints;

    private DataService ts = DataService.getInstance("out.db");

    private String dbpath;

    private static RulesPresenter rulesPresenter;

    private Stage stage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rules.getChildren().setAll(loadPane(RULES));
        terms.getChildren().setAll(loadPane(TERMS));
        breakpoints.getChildren().setAll(loadPane(BREAKPOINTS));
    }

    private Node loadPane(String fxml) {
        Node node = null;
        try {
            node = (Node) FXMLLoader.load( getClass().getResource(fxml) );
        } catch (IOException ex) {
            Logger.getLogger(CrsxvizPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return node;
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
        ts.dataClosed();
    }
    
    @FXML
    void onAbout(ActionEvent event) {
        System.out.println("onAbout...");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setService(DataService ts) {
        this.ts = ts;
        DataService.init(this.ts.getDbName());
        ts.dataReloaded();
        //reloadPresenters();
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
        if (db != null) {
            DataService.init(db);
            ts.dataReloaded();
        }
        //reloadPresenters();
    }
    
    /**
     * Forces a reload of Presenters following a new database being opened.
     * 
     * This effectively shows the new data accessed by the new database.
     *
    private void reloadPresenters() {
        breakpointsPresenter.initiateData();
        rulesPresenter.initiateData();
        termsPresenter.initiateData();
    }
    
    /**
     * Clears the data shown by the presenters. This sets all buttons
     * to their initial states along with erasing data of all lists.
     *
    public void clearControls() {
        termsPresenter.clearDisplay();
        breakpointsPresenter.clearDisplay();
        rulesPresenter.clearDisplay();
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
