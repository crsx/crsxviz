package crsxviz.application.crsxrunner;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main  extends Application {
	
    public static void help() {
        System.out.println("Usage: crsxrunner [<executable> [wrapper] <term> <dbPath>]");
        System.exit(-1);
    }


    public static void main(String[] args) {
        Runner r = new Runner();
        try {
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
        } catch (Exception e) {
        	System.err.println("Error running app.");
        	e.printStackTrace();
        }
    }


    @Override
    public void start(Stage arg0) throws Exception {
        RunnerDialog dialog = new RunnerDialog();
        dialog.doModal();
        System.out.println("Processing Ran: " + dialog.processingRan());
        if (dialog.getOutFile() != null) { 
                System.out.println("Dialog returned file: " + dialog.getOutFile().getAbsolutePath());
        } else {
                System.out.println("Dialog returned with null file");
        }
    }
}
