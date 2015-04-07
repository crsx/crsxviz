import crsxviz.application.App;
import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.persistence.services.DatabaseService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

public class UITest extends FxRobot {
    
    private final DatabaseService ts = DatabaseService.getInstance("out.db");
    private static Stage primaryStage;
    private CrsxvizPresenter presenter;
    private App app;
    
    @BeforeClass
    public static void setupSpec() throws Exception {
        primaryStage = FxToolkit.registerPrimaryStage();
        primaryStage.initStyle(StageStyle.DECORATED);
        FxToolkit.setupStage(stage -> stage.show());
    }
    
    @Before
    public void setup() throws Exception {
        try {
            app = (App) FxToolkit.setupApplication(App.class);
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, primaryStage.showingProperty());
        } catch (TimeoutException e) {
            Logger.getLogger(UITest.class.getName()).log(Level.SEVERE, null, e);
        }
        
        presenter = app.getRootPresenter();
    }
    
    @Test
    public void test() throws Exception {
        presenter.setService(ts);
        clickOn("#file");
    }
}
