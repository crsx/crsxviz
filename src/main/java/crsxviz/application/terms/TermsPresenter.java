package crsxviz.application.terms;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.DatabaseService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

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
import javafx.scene.text.Text;


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
    private TreeView<Text> terms_tree;
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
    private List<CompiledSteps> cSteps;
    private int totalSteps;
    private Stack<TreeItem<Text>> nodeStack;

    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private ObservableList<String> observableRules = FXCollections.observableArrayList();
    
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
        if(currentStep > 0){
        	String thisStepString = thisStep.toString();
        	TreeItem t = new TreeItem<Text>(new Text("Full Term String"));
            this.terms_tree.setRoot(t);
            nodeStack = new Stack<TreeItem<Text>>();
            nodeStack.push(t);
            boolean newIndent = true;
        	while(!thisStepString.equals("")){
        		int nextSiblingIndex = thisStepString.indexOf(',');
        		int nextChildIndex = thisStepString.indexOf('[');
        		int endIndentIndex = thisStepString.indexOf(']');
        		if(nextSiblingIndex == -1 && nextChildIndex == -1 && endIndentIndex == -1){
        			break;
        		}
        		else if((endIndentIndex < nextChildIndex || nextChildIndex == -1) && (endIndentIndex < nextSiblingIndex || nextSiblingIndex == -1)){
        			// place final term if there is one, then pop and place ]
        			if(endIndentIndex != 0){
        				//There is a preceding term, store first
        				if(newIndent){
        					TreeItem<Text> parentNode = nodeStack.pop();
                			String term = thisStepString.substring(0, endIndentIndex);
                			Text termText = new Text(term.replaceAll("\\s", ""));
                			TreeItem<Text> termNode = new TreeItem<Text>(termText);
                			parentNode.getChildren().add(termNode);
                			nodeStack.push(parentNode);
                			nodeStack.push(termNode);
        					newIndent = false;
        				}
        				else{
        					nodeStack.pop();
        					TreeItem<Text> parentNode = nodeStack.pop();
                			String term = thisStepString.substring(0, endIndentIndex);
                			Text termText = new Text(term.replaceAll("\\s", ""));
                			TreeItem<Text> termNode = new TreeItem<Text>(termText);
                			parentNode.getChildren().add(termNode);
                			nodeStack.push(parentNode);
                			nodeStack.push(termNode);
        				}
        			}
        			nodeStack.pop();
            		TreeItem<Text> parentNode = nodeStack.pop();
            		if(endIndentIndex + 1 >= thisStepString.length()){
        				thisStepString = "";
        			}
        			else{
        				if(thisStepString.charAt(endIndentIndex + 1) == ',' ){
        					TreeItem<Text> endIndentNode = new TreeItem<Text>(new Text("],"));
                    		parentNode.getParent().getChildren().add(endIndentNode);
                    		nodeStack.push(endIndentNode);
                    		thisStepString = thisStepString.substring(endIndentIndex + 2, thisStepString.length());
        				}
        				else{
        					TreeItem<Text> endIndentNode = new TreeItem<Text>(new Text("]"));
                    		parentNode.getParent().getChildren().add(endIndentNode);
                    		nodeStack.push(endIndentNode);
                    		thisStepString = thisStepString.substring(endIndentIndex + 1, thisStepString.length());
        				}
        				
        			}
        		}
        		else if((nextSiblingIndex < nextChildIndex || nextChildIndex == -1) && (nextSiblingIndex < endIndentIndex || endIndentIndex == -1)){
        			if(newIndent){
        				newIndent = false;
            			TreeItem<Text> parentNode = nodeStack.pop();
            			String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
            			Text siblingTermText = new Text(siblingTerm.replaceAll("\\s", ""));
            			TreeItem<Text> siblingNode = new TreeItem<Text>(siblingTermText);
            			parentNode.getChildren().add(siblingNode);
            			nodeStack.push(parentNode);
            			nodeStack.push(siblingNode);
        			}
        			else{
        				nodeStack.pop();
            			TreeItem<Text> parentNode = nodeStack.pop();
            			String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
            			Text siblingTermText = new Text(siblingTerm.replaceAll("\\s", ""));
            			TreeItem<Text> siblingNode = new TreeItem<>(siblingTermText);
            			parentNode.getChildren().add(siblingNode);
            			nodeStack.push(parentNode);
            			nodeStack.push(siblingNode);
        			}
        			if(nextSiblingIndex + 1 >= thisStepString.length()){
        				thisStepString = "";
        			}
        			else{
        				thisStepString = thisStepString.substring(nextSiblingIndex + 1, thisStepString.length());
        			}
        		}
        		else{
        			if(!newIndent){
        				nodeStack.pop();
        			}
        			TreeItem<Text> parentNode = nodeStack.pop();
        			String childTerm = thisStepString.substring(0, nextChildIndex + 1);
        			Text childTermText = new Text(childTerm.replaceAll("\\s", ""));
        			TreeItem<Text> childNode = new TreeItem<Text>(childTermText);
        			parentNode.getChildren().add(childNode);
        			nodeStack.push(parentNode);
        			nodeStack.push(childNode);
        			newIndent = true;
        			if(nextChildIndex + 1 >= thisStepString.length()){
        				thisStepString = "";
        			}
        			else{
        				thisStepString = thisStepString.substring(nextChildIndex + 1, thisStepString.length());
        			}
        		}
        		terms_tree.getRoot().setExpanded(true);
        	}
        	
//        	for(int i = 0; i < diffs.size(); i++){
//        		Diff diff = diffs.get(i);
//        		Text subtext = new Text();
//        		if(diff.operation == Operation.EQUAL){
//        			subtext.setText(diff.text);
//        			subtexts.getChildren().add(subtext);
//        		}
//        		else if(diff.operation == Operation.DELETE){
////        			subtext.setText(diff.text);
////        			subtext.setFill(Color.RED);
////        			subtexts.getChildren().add(subtext);
//        		}
//        		else if(diff.operation == Operation.INSERT){
//        			subtext.setText(diff.text);
//        			subtext.setFont(Font.font("System", FontWeight.BOLD, 12));
//        			subtext.setFill(Color.GREEN);
//        			subtexts.getChildren().add(subtext);
//        		}
//        	}
        }    
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
        if(currentStep < steps.size()){
        	String thisStepString = thisStep.toString();
        	TreeItem t = new TreeItem<Text>(new Text("Full Term String"));
            this.terms_tree.setRoot(t);
            nodeStack = new Stack<TreeItem<Text>>();
            nodeStack.push(t);
            boolean newIndent = true;
            while(!thisStepString.equals("")){
        		int nextSiblingIndex = thisStepString.indexOf(',');
        		int nextChildIndex = thisStepString.indexOf('[');
        		int endIndentIndex = thisStepString.indexOf(']');
        		if(nextSiblingIndex == -1 && nextChildIndex == -1 && endIndentIndex == -1){
        			break;
        		}
        		else if((endIndentIndex < nextChildIndex || nextChildIndex == -1) && (endIndentIndex < nextSiblingIndex || nextSiblingIndex == -1)){
        			// place final term if there is one, then pop and place ]
        			if(endIndentIndex != 0){
        				//There is a preceding term, store first
        				if(newIndent){
        					TreeItem<Text> parentNode = nodeStack.pop();
                			String term = thisStepString.substring(0, endIndentIndex);
                			Text termText = new Text(term.replaceAll("\\s", ""));
                			TreeItem<Text> termNode = new TreeItem<Text>(termText);
                			parentNode.getChildren().add(termNode);
                			nodeStack.push(parentNode);
                			nodeStack.push(termNode);
        					newIndent = false;
        				}
        				else{
        					nodeStack.pop();
        					TreeItem<Text> parentNode = nodeStack.pop();
                			String term = thisStepString.substring(0, endIndentIndex);
                			Text termText = new Text(term.replaceAll("\\s", ""));
                			TreeItem<Text> termNode = new TreeItem<Text>(termText);
                			parentNode.getChildren().add(termNode);
                			nodeStack.push(parentNode);
                			nodeStack.push(termNode);
        				}
        			}
        			nodeStack.pop();
            		TreeItem<Text> parentNode = nodeStack.pop();
            		if(endIndentIndex + 1 >= thisStepString.length()){
        				thisStepString = "";
        			}
        			else{
        				if(thisStepString.charAt(endIndentIndex + 1) == ',' ){
        					TreeItem<Text> endIndentNode = new TreeItem<Text>(new Text("],"));
                    		parentNode.getParent().getChildren().add(endIndentNode);
                    		nodeStack.push(endIndentNode);
                    		thisStepString = thisStepString.substring(endIndentIndex + 2, thisStepString.length());
        				}
        				else{
        					TreeItem<Text> endIndentNode = new TreeItem<Text>(new Text("]"));
                    		parentNode.getParent().getChildren().add(endIndentNode);
                    		nodeStack.push(endIndentNode);
                    		thisStepString = thisStepString.substring(endIndentIndex + 1, thisStepString.length());
        				}
        				
        			}
        		}
        		else if((nextSiblingIndex < nextChildIndex || nextChildIndex == -1) && (nextSiblingIndex < endIndentIndex || endIndentIndex == -1)){
        			if(newIndent){
        				newIndent = false;
            			TreeItem<Text> parentNode = nodeStack.pop();
            			String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
            			Text siblingTermText = new Text(siblingTerm.replaceAll("\\s", ""));
            			TreeItem<Text> siblingNode = new TreeItem<Text>(siblingTermText);
            			parentNode.getChildren().add(siblingNode);
            			nodeStack.push(parentNode);
            			nodeStack.push(siblingNode);
        			}
        			else{
        				nodeStack.pop();
            			TreeItem<Text> parentNode = nodeStack.pop();
            			String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
            			Text siblingTermText = new Text(siblingTerm.replaceAll("\\s", ""));
            			TreeItem<Text> siblingNode = new TreeItem<>(siblingTermText);
            			parentNode.getChildren().add(siblingNode);
            			nodeStack.push(parentNode);
            			nodeStack.push(siblingNode);
        			}
        			if(nextSiblingIndex + 1 >= thisStepString.length()){
        				thisStepString = "";
        			}
        			else{
        				thisStepString = thisStepString.substring(nextSiblingIndex + 1, thisStepString.length());
        			}
        		}
        		else{
        			if(!newIndent){
        				nodeStack.pop();
        			}
        			TreeItem<Text> parentNode = nodeStack.pop();
        			String childTerm = thisStepString.substring(0, nextChildIndex + 1);
        			Text childTermText = new Text(childTerm.replaceAll("\\s", ""));
        			TreeItem<Text> childNode = new TreeItem<Text>(childTermText);
        			parentNode.getChildren().add(childNode);
        			nodeStack.push(parentNode);
        			nodeStack.push(childNode);
        			newIndent = true;
        			if(nextChildIndex + 1 >= thisStepString.length()){
        				thisStepString = "";
        			}
        			else{
        				thisStepString = thisStepString.substring(nextChildIndex + 1, thisStepString.length());
        			}
        		}
        		terms_tree.getRoot().setExpanded(true);
        	}
        }
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
    private void initializeTree(TreeItem<Text> root) {
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
    private void nodeFocus(TreeItem<Text> node) {
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
        cSteps = ts.allCompiledSteps();
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
        onStepInto(null);
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
    
    private void updateTermDisplay(String termString){
    
    }
}
