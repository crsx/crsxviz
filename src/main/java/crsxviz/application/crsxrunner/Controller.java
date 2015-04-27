package crsxviz.application.crsxrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.print.attribute.standard.MediaSize.Other;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

public class Controller {

    private Stage stage;

    public void setStage(Stage s) {
        this.stage = s;
    }

    String outDb = null;
    
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField txtExecutable;

    @FXML
    private ProgressIndicator statusIndicator;

    @FXML
    private Button btnStart;

    @FXML
    private Accordion acrdnTermPanel;

    @FXML
    private TextArea txtaInTerm;

    @FXML
    private TextField txtInFilePath;

    @FXML
    private TitledPane pnlFileBrowse;

    @FXML
    private TitledPane pnlInputText;

    @FXML
    private Label lblStatus;

    @FXML
    private Button btnBrowseExe;

    @FXML
    private Button btnBrowseCrs;

    @FXML
    private AnchorPane ParserRunnerPane;

    @FXML
    private Button btnBrowseInFile;

    @FXML
    private TextField txtWrapper;

    @FXML
    private TextField txtOutPath;

    @FXML
    private TextField txtCrsPath;

    @FXML
    private Button btnBrowseOutPath;

    private boolean processingRan;

    private String doBrowse(String[][] formats) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        if (formats != null) {
            for (int i = 0; i < formats.length; i++) {
                if (formats[i] == null || formats[i].length < 2) {
                    continue;
                }
                ArrayList<String> l = new ArrayList<String>(formats[i].length - 1);
                for (int j = 1; j < formats[i].length; j++) {
                    l.add(formats[i][j]);
                }
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(formats[i][0], l));
            }
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All formats", "*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return null;
        }
        return selectedFile.getPath();
    }

    private void doExeBtn() {
        String val = doBrowse(null);
        if (val != null) {
            txtExecutable.setText(val);
        }
    }
    
    private File doCrsBrowse() {
    	DirectoryChooser dirPick = new DirectoryChooser();
    	File selectedDir = dirPick.showDialog(stage);
    	if (selectedDir == null || !selectedDir.exists()) {
    		txtCrsPath.setText("");
    	} else {
    		txtCrsPath.setText(selectedDir.getAbsolutePath());
    	}
    	return selectedDir;
    }

    private boolean isActivate(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
            case SPACE:
                return true;
            default:
                return false;
        }
    }

    @FXML
    void onExeBrowseKey(KeyEvent event) {
        if (isActivate(event)) {
            doExeBtn();
        }
    }

    @FXML
    void onClickExeBrowse(MouseEvent event) {
        doExeBtn();
    }
    
    @FXML
    void onCrsBrowseKey(KeyEvent event) {
        if (isActivate(event)) {
            doCrsBrowse();
        }
    }
    
    @FXML
    void onClickCrsBrowse(MouseEvent event) {
    	doCrsBrowse();
    }

    private void doBrowseInFile() {
        String[][] args = {{"Text Files", "*.txt"}};
        String val = doBrowse(args);
        if (val != null) {
            txtInFilePath.setText(val);
        }
    }

    @FXML
    void onBrowseInFileKey(KeyEvent event) {
        if (isActivate(event)) {
            doBrowseInFile();
        }
    }

    @FXML
    void onInFileBrowseClicked(MouseEvent event) {
        doBrowseInFile();
    }

    private void doBrowseOutFile() {
        String[][] args = {{"SQLite Database Files", "*.db"}};
        String val = doBrowse(args);
        if (val != null) {
            txtOutPath.setText(val);
        }
    }

    @FXML
    void onOutFileBrowseClicked(MouseEvent event) {
        doBrowseOutFile();
    }

    @FXML
    void onOutFileBrowseKey(KeyEvent event) {
        if (isActivate(event)) {
            doBrowseOutFile();
        }
    }

    public static void showError(String title, String message) {
        System.out.println(message);
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void doStart() {
        processingRan = false;
        File exeFile = new File(txtExecutable.getText());
        File inFile = new File(txtInFilePath.getText());
        File outFile = new File(txtOutPath.getText());
        File crsDir = new File(txtCrsPath.getText());
        String inTerm = txtaInTerm.getText();
        String wrapper = txtWrapper.getText();
        if (!exeFile.exists() || !exeFile.canExecute()) {
            showError("Configuration Error", "CRSX compiled program to run must exist and be executable");
            return;
        }
        if (!crsDir.exists() || !crsDir.isDirectory() || !crsDir.canRead()) {
        	showError("Configuration Error", "Invalid CRS file directory");
        	return;
        }
        if (outFile.getPath().length() == 0) {
            showError("Configuration Error", "Output file cannot be empty");
            return;
        }
        if (!pnlInputText.isExpanded() && !pnlFileBrowse.isExpanded()) {
            showError("Configuration Error", "Select either \"Import from file\" of \"Input as text\"");
            return;
        }
        if (pnlFileBrowse.isExpanded()) {
            if (inFile.exists() && inFile.canRead()) {
                try {
                    FileReader fr = new FileReader(inFile);
                    BufferedReader br = new BufferedReader(fr);
                    inTerm = br.readLine();
                    String tmp = null;
                    while ((tmp = br.readLine()) != null) {
                        inTerm += tmp;
                    }
                    br.close();
                    fr.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showError("Configuration Error", "Term import file must exist and be readable");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Processing Error", "Error reading term file");
                    return;
                }
            } else {
                showError("Configuration Error", "Term import file must exist and be readable");
                return;
            }
        }
        if (inTerm.length() == 0) {
            showError("Configuration Error", "Term cannot be empty");
            return;
        }

        if (wrapper.length() == 0) {
            wrapper = null;
        }
        
        btnStart.setDisable(true);
        statusIndicator.setVisible(true);
        lblStatus.setText("Running");
        try {
            Runner r = new Runner();
            String output = r.run(exeFile.getAbsolutePath(), crsDir.getAbsolutePath(), wrapper, inTerm, outFile.getAbsolutePath());
            outDb = outFile.getAbsolutePath();
            lblStatus.setText("Completed");
            processingRan = true;
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Processing Completed");
            alert.setHeaderText(null);
            alert.setContentText("Processing completed.");
            alert.showAndWait();
        } catch (Exception e) {
            lblStatus.setText("Error!");
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Processing Error");
            alert.setHeaderText(null);
            alert.setContentText("Processing Error!\n" + e.getMessage());
            alert.showAndWait();
        } finally {
            statusIndicator.setVisible(false);
            btnStart.setDisable(false);
        }

    }

    @FXML
    void onStartKey(KeyEvent event) {
        if (isActivate(event)) {
            doStart();
        }
    }

    @FXML
    void onStartClicked(MouseEvent event) {
        doStart();
    }

    @FXML
    void initialize() {
        assert txtExecutable != null : "fx:id=\"txtExecutable\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert statusIndicator != null : "fx:id=\"statusIndicator\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert btnStart != null : "fx:id=\"btnStart\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert acrdnTermPanel != null : "fx:id=\"acrdnTermPanel\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert txtaInTerm != null : "fx:id=\"txtaInTerm\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert txtInFilePath != null : "fx:id=\"txtInFilePath\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert pnlFileBrowse != null : "fx:id=\"pnlFileBrowse\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert lblStatus != null : "fx:id=\"lblStatus\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert btnBrowseExe != null : "fx:id=\"btnBrowseExe\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert ParserRunnerPane != null : "fx:id=\"ParserRunnerPane\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert btnBrowseInFile != null : "fx:id=\"btnBrowseInFile\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert txtWrapper != null : "fx:id=\"txtWrapper\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert txtOutPath != null : "fx:id=\"txtOutPath\" was not injected: check your FXML file 'ParserRunner.fxml'.";
        assert btnBrowseOutPath != null : "fx:id=\"btnBrowseOutPath\" was not injected: check your FXML file 'ParserRunner.fxml'.";

    }

    public File getOutFile() {
        String path = outDb;
        if (outDb == null || path.length() == 0) {
            return null;
        }
        File outFile = new File(path);
        if (outFile.exists() && outFile.canRead()) {
            return outFile;
        } else {
            return null;
        }
    }

    public boolean processingRan() {
        return processingRan;
    }
}
