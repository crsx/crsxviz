package crsxrunner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("ParserRunner.fxml"));
		primaryStage.setTitle("ParserRunner");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	public static void help() {
		System.out.println("Usage: crsxrunner [<executable> [wrapper] <term> <dbPath>]");
		System.exit(-1);
	}
	
	
	public static void main(String[] args) {
		Runner r = new Runner();
		if (args.length == 3) {
			r.run(args[0], null, args[1], args[2]);
		} else if (args.length == 4) {
			r.run(args[0], args[1], args[2], args[3]);
		} else if (args.length == 0) {
			launch(args);
		} else {
			System.out.println("Incorrect number of arguments");
			help();
		}
	}

}