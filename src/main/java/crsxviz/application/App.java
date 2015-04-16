package crsxviz.application;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class App extends Application {
    
    private CrsxvizPresenter crsxviz;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("CRSX Visualizer");
        stage.setScene(
            createScene(
                loadMainPane()
            )
        );

        stage.show();
    }

    /**
     * Creates the main application scene.
     *
     * @param mainPane the main application layout.
     *
     * @return the created scene.
     */
    private Scene createScene(Pane mainPane) {
        Scene scene = new Scene( mainPane );

        scene.getStylesheets().setAll(
            getClass().getResource("app.css").toExternalForm()
        );

        return scene;
    }
    
    /**
     * Loads the main fxml layout.
     *
     * @return the loaded pane.
     * @throws IOException if the pane could not be loaded.
     */
    private Pane loadMainPane() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        Pane mainPane = (Pane) loader.load(
            getClass().getResourceAsStream( "crsxviz/crsxviz.fxml" )
        );
        
        CrsxvizPresenter presenter = new CrsxvizPresenter();

        return mainPane;
    }

    public CrsxvizPresenter getRootPresenter() {
        return crsxviz;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}