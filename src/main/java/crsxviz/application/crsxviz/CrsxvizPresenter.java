package crsxviz.application.crsxviz;


import crsxviz.application.breakpoints.BreakpointsPresenter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import static crsxviz.application.crsxrunner.Controller.showError;
import crsxviz.application.crsxrunner.RunnerDialog;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.application.terms.TermsPresenter;
import crsxviz.persistence.services.IDataService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CrsxvizPresenter implements Initializable {

    private IDataService ts;

    private String dbpath;

    private static RulesPresenter rulesPresenter;
    private static TermsPresenter termsPresenter;
    private static BreakpointsPresenter breakpointsPresenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rulesPresenter = RulesPresenter.getPresenter();
        termsPresenter = TermsPresenter.getPresenter();
        breakpointsPresenter = BreakpointsPresenter.getPresenter();
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
            File selectedFile = fileChooser.showOpenDialog(new Stage());
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
}
