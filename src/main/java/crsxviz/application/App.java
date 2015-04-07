package crsxviz.application;

import com.airhacks.afterburner.injection.Injector;
import crsxviz.application.crsxviz.CrsxvizPresenter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import crsxviz.application.crsxviz.CrsxvizView;

public class App extends Application {
    
    
    private CrsxvizPresenter crsxviz;

    @Override
    public void start(Stage stage) throws Exception {
        CrsxvizView appView = new CrsxvizView();
        crsxviz = (CrsxvizPresenter) appView.getPresenter();
        crsxviz.setStage(stage);
        Scene scene = new Scene(appView.getView());
        stage.setTitle("CRSX Visualizer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        Injector.forgetAll();
    }

    public CrsxvizPresenter getRootPresenter() {
        return crsxviz;
    }
}