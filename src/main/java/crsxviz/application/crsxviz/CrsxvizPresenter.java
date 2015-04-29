package crsxviz.application.crsxviz;


import crsxviz.application.breakpoints.BreakpointsPresenter;
import crsxviz.application.crsxrunner.RunnerDialog;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.application.terms.TermsPresenter;
import crsxviz.persistence.services.IDataService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static crsxviz.application.crsxrunner.Controller.showError;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CrsxvizPresenter implements Initializable {

    private IDataService ts;

    private StringProperty dbpath;

    private static RulesPresenter rulesPresenter;
    private static TermsPresenter termsPresenter;
    private static BreakpointsPresenter breakpointsPresenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbpath = new SimpleStringProperty(null);
        
        rulesPresenter = RulesPresenter.getPresenter();
        termsPresenter = TermsPresenter.getPresenter();
        breakpointsPresenter = BreakpointsPresenter.getPresenter();
        
        dbpath.addListener((observable, oldValue, newValue) -> {
            if (observable.getValue() == null)
                termsPresenter.offConfiguration();
            else
                termsPresenter.onConfiguration();
        });
    }
    
    @FXML
    void onOpenFile(ActionEvent event) {
        if (dbpath.getValue() != null) {
            showError("Error!", "A file already open.\nPlease close it before opening a new one");
        } else {
            System.out.println("Opening file...");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Trace File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite3 Database Files", "*.db"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All formats", "*"));
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile == null) {
                dbpath.setValue(null);
                System.out.println("Browse aborted");
            } else {
                if (selectedFile.exists() && selectedFile.canRead()) {
                    dbpath.setValue(selectedFile.getAbsolutePath());
                    System.out.println("File opened OK");
                    loadDb(dbpath.getValue());
                } else {
                    showError("Error!", "Specified file is not readable");
                }
            }
        }
    }

    @FXML
    void onRunParser(ActionEvent event) throws IOException {
        if (dbpath.getValue() != null) {
            showError("Error!", "A file already open.\nPlease close it before opening a new one");
        } else {
            System.out.println("Running parser...");
            RunnerDialog d = new RunnerDialog();
            d.doModal();
            if (d.processingRan()) {
                if (d.getOutFile() != null) {
                    System.out.println("Parser dialog returned database file " + d.getOutFile().getAbsolutePath());
                    dbpath.setValue(d.getOutFile().getAbsolutePath());
                    loadDb(dbpath.getValue());
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
        if (dbpath.getValue() == null) 
            showError("Error!", "Cannot close, no files are open");
        dbpath.setValue(null);
        ts.dataClosed();
    }
    
    @FXML
    void onAbout(ActionEvent event) {
        System.out.println("onAbout...");
    }
    
    /**
     * Set the service to be used by the controller to get data.
     * 
     * @param ts The service to be used
     */
    public void setService(IDataService ts) {
        this.ts = ts;
        
        rulesPresenter.setService(ts);
        termsPresenter.setService(ts);
        breakpointsPresenter.setService(ts);
        
        ts.dataRequiresReload();
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
            ts.setDataName(db);
            this.setService(ts);
        }
    }
    
    /**
     * Property to record the current running state of the application.
     * The application is defined to be running if there is a valid
     * data service loaded i.e. a valid database is open and being read.
     * 
     * @return true if there is a valid data service, false otherwise
     */
    public BooleanBinding isVisualizerRunning() {
        return Bindings.isNotNull(dbpath);
    }
}
