package crsxviz.application.terms;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.application.terms.diff_match_patch.Diff;
import crsxviz.application.terms.diff_match_patch.Operation;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.DatabaseService;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;



public class TermsPresenter implements Initializable {

    @FXML
    private Button step_into;
    @FXML
    private Button resume;
    @FXML
    private Button step_return;
    @FXML
    private Button run;
    @FXML
    private Button step_over;
    @FXML
    private Button step_back;
    @FXML
    private Button terminate;
    @FXML
    private Label trace_label;
    @FXML
    private TreeView<TextFlow> terms_tree;
    @FXML
    private Slider slider;
    @FXML
    private TextField step_specifier;

    private DatabaseService ts;

    private int lastIndent = 0, currentStep = 0, previousSliderValue = 0;

    // Controls progress through the trace by providing means to pause
    // in a given location, used primarily to pause on breakpoints
    private boolean proceed;

    private List<Steps> steps;
    private List<ActiveRules> rules;
    private int totalSteps;

    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private ObservableList<String> observableRules = FXCollections.observableArrayList();
    
    private diff_match_patch differ = new diff_match_patch();
    private LinkedList<Diff> diffs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trace_label.setText("No trace file opened");
        initialSliderState();
        step_specifier.setText("");
        step_specifier.setEditable(true);
        step_specifier.setFocusTraversable(false);
    }

    @FXML
    void onRun(ActionEvent event) {
        sliderOn();
        run.setDisable(true);
        resume.setDisable(false);
        terminate.setDisable(false);
        step_over.setDisable(false);
        step_specifier.setText("");
        step_specifier.setEditable(true);
        step_specifier.setFocusTraversable(false);
    }

    @FXML
    void onResume(ActionEvent event) {
        proceed = true;
        jumpToNextStep();
    }

    @FXML
    void onTerminate(ActionEvent event) {
        terms_tree.setRoot(null);
        run.setDisable(false);
        resume.setDisable(true);
        terminate.setDisable(true);
        step_into.setDisable(true);
        step_return.setDisable(true);
        step_over.setDisable(true);
        step_back.setDisable(true);
        slider.setDisable(true);
        step_specifier.setText("");
        step_specifier.setEditable(false);
        step_specifier.setFocusTraversable(false);
    }

    @FXML
    void onStepInto(ActionEvent event) {
        proceed = true;
        if (currentStep <= totalSteps) {
            step();
        }
    }

    @FXML
    void onStepOver(ActionEvent event) {
        proceed = true;
        if (currentStep <= totalSteps) {
            if (lastIndent == 0) {
                step();
            } else {
                Steps s = steps.get(currentStep);
                int currentIndent = lastIndent;
                while (s.getIndentation() > currentIndent) {
                    step();
                    s = steps.get(currentStep);
                }
                step();
            }
        }
    }

    @FXML
    void onStepReturn(ActionEvent event) {
        proceed = true;
        if (currentStep <= totalSteps) {
            if (lastIndent == 0) {
                step();
            } else {
                Steps s = steps.get(currentStep);
                int currentIndent = lastIndent;
                while (s.getIndentation() >= currentIndent) {
                    step();
                    s = steps.get(currentStep);
                }
                step();
            }
        }
    }

    @FXML
    void onSliderClick() {
        proceed = true;
        slider.setValue(Math.round(slider.getValue()));
        int currentSliderValue = (int) slider.getValue();
        int tempPreviousSliderValue = previousSliderValue;
        if (currentSliderValue > tempPreviousSliderValue) {
            while (currentStep < currentSliderValue) {
                step();
            }
        } else if (currentSliderValue < tempPreviousSliderValue) {
            while (currentStep > currentSliderValue) {
                onStepBack();
            }
        }
        previousSliderValue = currentSliderValue;
    }

    void step() {
        if (currentStep < totalSteps && proceed) {
        	Steps s = steps.get(currentStep);
        	currentStep++;
            CrsxvizPresenter.getRulesPresenter().highlightActiveRule(s.getActiveRuleId());
            if (currentStep < totalSteps) {
                s = steps.get(currentStep);
                step_over.setDisable(false);
                if (s.getIndentation() > lastIndent) {
                    step_into.setDisable(false);
                } else {
                    step_into.setDisable(true);
                }
                if (lastIndent > 1) {
                    step_return.setDisable(false);
                } else {
                    step_return.setDisable(true);
                }
            } else {
                step_into.setDisable(true);
                step_return.setDisable(true);
                step_over.setDisable(true);
            }
            lastIndent = s.getIndentation();
            step_back.setDisable(false);
        }
        
        previousSliderValue = (int) slider.getValue();
        slider.setValue(currentStep);
        step_specifier.setText("" + currentStep);
        
        CompiledSteps thisStep = ts.getCompiledStep(new Long(currentStep));
        TextFlow subtexts = null;
        if(currentStep > 0){
        	String thisStepString = thisStep.toString();
        	diffs = differ.diff_main(ts.getCompiledStep(new Long(currentStep - 1)).toString(), thisStepString);
        	subtexts = new TextFlow(); 
        	for(int i = 0; i < diffs.size(); i++){
        		Diff diff = diffs.get(i);
        		Text subtext = new Text();
        		if(diff.operation == Operation.EQUAL){
        			subtext.setText(diff.text);
        			subtexts.getChildren().add(subtext);
        		}
        		else if(diff.operation == Operation.DELETE){
//        			subtext.setText(diff.text);
//        			subtext.setFill(Color.RED);
//        			subtexts.getChildren().add(subtext);
        		}
        		else if(diff.operation == Operation.INSERT){
        			subtext.setText(diff.text);
        			subtext.setFont(Font.font("System", FontWeight.BOLD, 12));
        			subtext.setFill(Color.GREEN);
        			subtexts.getChildren().add(subtext);
        		}
        	}
        }
        else{
        	subtexts = new TextFlow();
        	subtexts.getChildren().add(new Text(thisStep.toString()));
        }
        TreeItem t = new TreeItem<TextFlow>(subtexts);
        this.terms_tree.setRoot(t);
    }

    @FXML
    void onStepBack() {
    	currentStep--;
    	 Steps s = steps.get(currentStep);
    	CrsxvizPresenter.getRulesPresenter().highlightActiveRule(s.getActiveRuleId());
        if (currentStep <= 0) {
            step_back.setDisable(true);
        } else {
            step_back.setDisable(false);
        }
        if (currentStep < totalSteps) {    
            step_over.setDisable(false);
            if (s.getIndentation() > lastIndent) {
                step_into.setDisable(false);
            } else {
                step_into.setDisable(true);
            }
            if (lastIndent > 1) {
                step_return.setDisable(false);
            } else {
                step_return.setDisable(true);
            }
        } else {
            step_into.setDisable(true);
            step_return.setDisable(true);
            step_over.setDisable(true);
        }
        lastIndent = s.getIndentation();
        previousSliderValue = (int) slider.getValue();
        slider.setValue(currentStep);
        step_specifier.setText("" + currentStep);
        
        CompiledSteps thisStep = ts.getCompiledStep(new Long(currentStep));
        TextFlow subtexts = null;
        if(currentStep < steps.size()){
        	String thisStepString = thisStep.toString();
        	diffs = differ.diff_main(ts.getCompiledStep(new Long(currentStep + 1)).toString(), thisStepString);
        	subtexts = new TextFlow(); 
        	for(int i = 0; i < diffs.size(); i++){
        		Diff diff = diffs.get(i);
        		Text subtext = new Text();
        		if(diff.operation == Operation.EQUAL){
        			subtext.setText(diff.text);
        			subtexts.getChildren().add(subtext);
        		}
        		else if(diff.operation == Operation.DELETE){
//        			subtext.setText(diff.text);
//        			subtext.setFill(Color.RED);
//        			subtexts.getChildren().add(subtext);
        		}
        		else if(diff.operation == Operation.INSERT){
        			subtext.setText(diff.text);
        			//subtext.setFont(Font.font("System", FontWeight.BOLD, 12));
        			//subtext.setFill(Color.GREEN);
        			subtexts.getChildren().add(subtext);
        		}
        	}
        }
        TreeItem t = new TreeItem<TextFlow>(subtexts);
        this.terms_tree.setRoot(t);
    }

    public void setDbService(DatabaseService service) {
        this.ts = service;
    }
    
    /**
     * Jump to the first breakpoint. If there are no breakpoints set, then the
     * program will step through each instruction per user input. That is with
     * no breakpoints set then it is up to the user to step through the trace;
     * the visualizer will not run through on its own.
     *
     */
    void jumpToNextStep() {
        if (!observableBreakpoints.isEmpty()) {
            while (currentStep < totalSteps && proceed) {
                Steps step = steps.get(currentStep);
                String rule = rules.get(step.getActiveRuleId()).getValue();

                step();
                proceed = !(observableBreakpoints.contains(rule));
            }
        }
    }

    /**
     * Create the term tree from a given root node. This root item serves to be
     * a subheading within the terms list window.
     *
     * @param root Root node to build the term tree from
     * @return
     */
    private void initializeTree(TreeItem<TextFlow> root) {
        terms_tree.setRoot(root);
        root.setExpanded(true);
        currentStep = 0;
        lastIndent = 0;
    }

    /**
     * Focus on the given node in the terms_tree
     *
     * @param node Node to focus on
     */
    private void nodeFocus(TreeItem<TextFlow> node) {
        terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        terms_tree.requestFocus();
        terms_tree.getSelectionModel().select(node);
        terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
    }
   
    /**
     * Initializes the Presenter to an initial state where either a database has
     * been opened and thus will display the correct state of buttons along with
     * initial term tree, or where a database has not been opened.
     *
     */
    public void initiateData() {
        steps = ts.allSteps();
        for(Steps step : steps){
        	System.out.println("Step " + step.getStepNum() + " Indentation level " + step.getIndentation() + " : " + step.getStartData());
        }
        rules = ts.allRules();
        totalSteps = steps.size();

        String label = ts.getDbName();
        trace_label.setText(label == null ? "No trace file opened" : "Debugging " + label);
        observableRules = ts.allObservableRules();
        observableBreakpoints = ts.allObservableBreakpoints();

        sliderOn();
        step_return.setDisable(false);
        step_into.setDisable(false);
        step_over.setDisable(false);
        run.setDisable(false);
        resume.setDisable(false);
        terminate.setDisable(false);
        proceed = true;
        //onStepInto(null);
    }

    /**
     * Return the Presenter to its initial state where no database is to be
     * displayed.
     */
    public void clearDisplay() {
        observableBreakpoints = FXCollections.observableArrayList();
        observableRules = FXCollections.observableArrayList();
        steps = null;
        rules = null;
        totalSteps = lastIndent = currentStep = 0;
        terms_tree.setRoot(null);
        run.setDisable(true);
        terminate.setDisable(true);
        step_into.setDisable(true);
        step_over.setDisable(true);
        step_return.setDisable(true);
        resume.setDisable(true);
        trace_label.setText("No trace file opened");
        initialSliderState();
    }

    /**
     * Sets the slider to the initial state which is disabled
     */
    private void initialSliderState() {
        slider.setDisable(true);
        slider.setMin(0);
        slider.setMax(0);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setValue(0);
    }
    
    /**
     * Enable the slider
     */
    private void sliderOn() {
        double majorTick = Math.floor(totalSteps / 10);
        slider.setDisable(false);
        slider.setMax(totalSteps);
        slider.setMajorTickUnit(majorTick <= 0 ? 1 : majorTick);
        slider.setMinorTickCount((int) Math.floor(slider.getMajorTickUnit()) / 5);
        slider.setValue(0);
    }
    
    @FXML
    public void onStepSpecify(){
        int specifiedStep = Integer.valueOf(step_specifier.getText());
        if(specifiedStep < 0 || specifiedStep > steps.size()){
            return;
        }
        else if(specifiedStep < currentStep){
            while(currentStep > specifiedStep){
                onStepBack();
            }
        }
        else if(specifiedStep > currentStep){
            while(currentStep < specifiedStep){
                step();
            }
        }
    }
}
