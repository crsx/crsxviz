package crsxviz.application.crsxviz;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import crsxviz.application.breakpoints.BreakpointsPresenter;
import crsxviz.application.breakpoints.BreakpointsView;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.application.rules.RulesView;
import crsxviz.application.terms.TermsPresenter;
import crsxviz.application.terms.TermsView;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.TraceService;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.inject.Inject;

public class CrsxvizPresenter implements Initializable {
	
    @FXML AnchorPane terms;
    @FXML AnchorPane rules;
    @FXML AnchorPane breakpoints;
	
    @FXML private MenuItem close; 
    @FXML private MenuItem open;
    @FXML private Menu options;
    @FXML private MenuItem playback; 
    @FXML private MenuItem about; 
    @FXML private Menu help; 
    @FXML private Menu file;
    
    @Inject TraceService ts;

    protected TermsPresenter termsPresenter;
    public RulesPresenter rulesPresenter;
    protected BreakpointsPresenter breakpointsPresenter;
    
    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private ObservableList<String> observableRules;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
            TermsView termsView = new TermsView();
            RulesView rulesView = new RulesView();
            BreakpointsView breakpointsView = new BreakpointsView();
            this.termsPresenter = (TermsPresenter) termsView.getPresenter();
            this.rulesPresenter = (RulesPresenter) rulesView.getPresenter();
            this.breakpointsPresenter = (BreakpointsPresenter) breakpointsView.getPresenter();
            
            breakpointsPresenter.setCrsxMain(this);
            rulesPresenter.setCrsxMain(this);
            termsPresenter.setCrsxMain(this);

            terms.getChildren().add(termsView.getView());
            rules.getChildren().add(rulesView.getView());
            breakpoints.getChildren().add(breakpointsView.getView());
    }
    
    public ObservableList<String> getBreakpoints() {
        return observableBreakpoints;
    }
    
    public ObservableList<String> getObservableRules() {
        observableRules = FXCollections.observableArrayList();
        ts.allRules().stream().forEach(
                (rule) -> observableRules.add(rule.getValue())
        );
        return observableRules;
    }
    
    public List<ActiveRules> getRules() {
        return ts.allRules();
    }
    
    public List<Steps> getSteps() {
        return ts.allSteps();
    }

    @FXML
    void onOpenFile(ActionEvent event){
    	System.out.println("opening file...");
    }
    
    @FXML
    void onCloseFile(ActionEvent event){
    	System.out.println("closing file...");
    }
    
    @FXML
    void onAdjustPlayback(ActionEvent event){
    	System.out.println("adjusting playback...");
    }
    
    @FXML
    void onAbout(ActionEvent event){
    	System.out.println("about...");
    }
}
