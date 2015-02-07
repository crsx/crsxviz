package ui;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {
	
	FileChooser fileChooser;
	Stage primaryStage;
	
    @Override
    public void start(Stage primaryStage) throws Exception {
        Controller controller = new Controller();
    	
        primaryStage.setScene(new Scene(controller));
        primaryStage.setTitle("CRSXVIZ");
        primaryStage.show();
        fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Trace File");  	
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public File open(){
    	 return fileChooser.showOpenDialog(primaryStage);
    }
    
}