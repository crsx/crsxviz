package crsxrunner;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RunnerDialog{

	private Controller myController = null;
	private Scene scene = null;
	
	public Scene getScene() throws IOException {
		FXMLLoader ldr = new FXMLLoader(RunnerDialog.class.getResource("ParserRunner.fxml"));
		Parent root = ldr.load();
		myController = ldr.getController();
		Scene scene = new Scene(root);  
		return scene;
	}
	
	public void doModal() throws Exception {
		Stage stage = new Stage();
		stage.setTitle("ParserRunner");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
	}
	
	public boolean processingRan() {
		return myController.processingRan();
	}
	
	public File getOutFile() {
		return myController.getOutFile();
	}
	
	public RunnerDialog() throws IOException {
		scene = getScene();
	}
}