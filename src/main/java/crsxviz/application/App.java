package crsxviz.application;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.persistence.services.DataService;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class App extends Application {
    
    private static CrsxvizPresenter crsxviz;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();

        Pane mainPane = (Pane) loader.load(
            getClass().getResourceAsStream( "crsxviz/crsxviz.fxml" )
        );
        
        crsxviz = loader.<CrsxvizPresenter>getController();
        crsxviz.setService(new DataService());
        stage.setScene(new Scene(mainPane));
        stage.setTitle("CRSX Visualizer");
        stage.show();
    }

    public static CrsxvizPresenter getRootPresenter() {
        return crsxviz;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}