import crsxviz.application.App;
import crsxviz.application.breakpoints.BreakpointsPresenter;
import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.services.DataService;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.matcher.base.NodeMatchers;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import static org.testfx.matcher.base.NodeMatchers.isDisabled;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.util.WaitForAsyncUtils;

@RunWith(MockitoJUnitRunner.class)
public class RulesAndBreakpointsTest {
    
    private static Stage primaryStage;
    
    private DataService dataMock, ts = new DataService();
    private static CrsxvizPresenter crsxviz;
    private static RulesPresenter rules;
    private static BreakpointsPresenter breakpoints;
    
    ObservableList<Text> ruleslist;
    ObservableList<Text> breakpointlist;
    List<ActiveRules> rulesList;
    
    public FxRobot fx = new FxRobot();
    
    @BeforeClass
    public static void setupSpec() throws Exception {
        primaryStage = FxToolkit.registerPrimaryStage();
        primaryStage.initStyle(StageStyle.DECORATED);
        FxToolkit.setupStage(stage -> stage.show());
    }
    
    @Before
    public void setup() throws Exception {
        try {
            FxToolkit.setupApplication(App.class);
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, primaryStage.showingProperty());
        } catch (TimeoutException e) {
            Logger.getLogger(RulesAndBreakpointsTest.class.getName()).log(Level.SEVERE, null, e);
        }
        
        crsxviz = App.getRootPresenter();
        rules = RulesPresenter.getPresenter();
        breakpoints = BreakpointsPresenter.getPresenter();
        
        ts.setDataName("out.db");
        crsxviz = App.getRootPresenter();
        dataMock = mock(DataService.class);
        MockitoAnnotations.initMocks(this);
        
        ruleslist = FXCollections.observableArrayList();
        ruleslist.setAll(new Text("first"), new Text("second"), new Text("third"));
        rulesList = new LinkedList<>();
        rulesList.add(new ActiveRules(1, "first"));
        rulesList.add(new ActiveRules(2, "second"));
        rulesList.add(new ActiveRules(3, "third"));
        
        breakpointlist = FXCollections.observableArrayList();
        
        when(dataMock.allObservableBreakpoints()).thenReturn(breakpointlist);
        when(dataMock.allObservableRules()).thenReturn(ruleslist);
        when(dataMock.allRules()).thenReturn(rulesList);
        breakpoints.setService(dataMock);
        breakpoints.dataLoaded();
        rules.setService(dataMock);
        rules.dataLoaded();
    }
    
    @Test
    public void should_contain_data_lists() {
        // expect:
        verifyThat("#rules_list", NodeMatchers.isNotNull());
        verifyThat("#breakpoint_list", NodeMatchers.isNotNull());
        verifyThat("#terms_tree", NodeMatchers.isNotNull());
    }
    
    @Test
    public void rules_loaded() {
        // expect:
        verify(dataMock, atLeast(1)).allObservableRules();
        verifyThat("#rules_list", (ListView<Text> list) -> {
           Text selectedItem = list.getItems().get(0);
           return Objects.equals(selectedItem.getText(), "first");
        });
    }
    
    @Test
    public void breakpoints_loaded() {
        // expect:
        verify(dataMock, atLeast(1)).allObservableBreakpoints();
        verifyThat("#breakpoint_list", ListViewMatchers.hasItems(0));
    }
    
    @Test
    public void breakpoints_can_be_set() {
        // when:
        fx.rightClickOn( "#rules_list" ).clickOn("Set Breakpoint");
        
        // expect:
        verifyThat("#breakpoint_list", (ListView<Text> list) -> {
            return Objects.equals(list.getItems().size(), 1);
        });
        
        // when:
        fx.rightClickOn("#breakpoint_list").clickOn("Remove Breakpoint");
        
        // expect:
        verifyThat("#breakpoint_list", (ListView<Text> list) -> {
            return Objects.equals(list.getItems().size(), 0);
        });
    }
    
    @Test
    public void breakpoints_can_be_set_by_dialog_and_removed() {
        // when:
        fx.clickOn("#breakpointsButton").clickOn("Set New Breakpoint");
        fx.clickOn("#breakpointText").write(".*").clickOn("OK");
        
        // expect:
        verifyThat("#breakpoint_list", (ListView<Text> list) -> {
            return Objects.equals(list.getItems().size(), 3);
        });
        
        // when:
        fx.clickOn("#breakpointsButton").clickOn("Remove All");
        
        // expect
        verifyThat("#breakpoint_list", (ListView list) -> {
            return Objects.equals(list.getItems().size(), 0);
        });
    }
    
    @Test
    public void initial_state_of_application_ok() throws Exception {
        fx.clickOn("#file").clickOn("#help");
        verifyThat("#trace_label", hasText("No trace file opened"));
        
        verifyThat("#step_over", isDisabled());
        verifyThat("#step_into", isDisabled());
        verifyThat("#step_return", isDisabled());
        verifyThat("#step_back", isDisabled());
        verifyThat("#run", isDisabled());
        verifyThat("#terminate", isDisabled());
        verifyThat("#resume", isDisabled());
    }
}
