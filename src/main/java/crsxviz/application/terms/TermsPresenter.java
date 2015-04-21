package crsxviz.application.terms;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.DataService;

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
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TermsPresenter extends AnchorPane implements Initializable, DataListener {

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

	private DataService ts;

	private int lastIndent = 0, currentStep = -1, previousSliderValue = 0;
	
	String precedingLastString = "";
	String precedingTerm = "";
	
	// Controls progress through the trace by providing means to pause
	// in a given location, used primarily to pause on breakpoints
	private boolean proceed;

	private List<Steps> steps;
	private List<ActiveRules> rules;
	private int totalSteps;
	private Stack<TreeItem<Text>> nodeStack;

	private ObservableList<Text> observableBreakpoints = FXCollections.observableArrayList();
	private ObservableList<Text> observableRules = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		trace_label.setText("No trace file opened");
		initialSliderState();
		step_specifier.setText("");
		step_specifier.setEditable(true);
		step_specifier.setFocusTraversable(false);

		ts = DataService.getInstance();
		ts.addListener(this);
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
		currentStep = -1;
		step(1);
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
			step(1);
		}
	}

	@FXML
	void onStepOver(ActionEvent event) {
		proceed = true;
		if (currentStep <= totalSteps) {
			if (lastIndent == 0) {
				step(1);
			} else {
				int stepsToAdvance = 0;
				Steps s = steps.get(currentStep);
				int currentIndent = lastIndent;
				while (s.getIndentation() > currentIndent) {
					stepsToAdvance++;
					s = steps.get(currentStep + stepsToAdvance);
				}
				step(stepsToAdvance + 1);
			}
		}
	}

	@FXML
	void onStepReturn(ActionEvent event) {
		proceed = true;
		if (currentStep <= totalSteps) {
			if (lastIndent == 0) {
				step(1);
			} else {
				int stepsToAdvance = 0;
				Steps s = steps.get(currentStep);
				int currentIndent = lastIndent;
				while (s.getIndentation() >= currentIndent) {
					stepsToAdvance++;
					s = steps.get(currentStep + stepsToAdvance);
				}
				step(stepsToAdvance + 1);
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
			step(currentSliderValue - currentStep);
		} else if (currentSliderValue < tempPreviousSliderValue) {
			stepBack(currentStep - currentSliderValue);
			step(0);
		}
		previousSliderValue = currentSliderValue;
	}

	void step(int stepsToAdvance) {
		currentStep += stepsToAdvance;
		if(currentStep > totalSteps - 1){
			currentStep = totalSteps - 1; 
		}
		if (currentStep < totalSteps && proceed) {
			Steps s = steps.get(currentStep);
			CrsxvizPresenter.getRulesPresenter().highlightActiveRule(s.getActiveRuleId());
			String nextRule = rules.get(steps.get(currentStep+1).getActiveRuleId()).getValue();
			CrsxvizPresenter.getRulesPresenter().setNextRule(nextRule);
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
			if(currentStep > 0){
				s = steps.get(currentStep - 1);
				lastIndent = s.getIndentation();
			}
			else{
				lastIndent = 1;
			}
			if (currentStep > 0) step_back.setDisable(false);
		}
		else{
			currentStep -= stepsToAdvance;
		}
		previousSliderValue = (int) slider.getValue();
		slider.setValue(currentStep);
		step_specifier.setText("" + currentStep);

		CompiledSteps thisStep = ts.getCompiledStep(new Long(currentStep));
		if(currentStep > 0){
			CompiledSteps lastStep = ts.getCompiledStep(new Long(currentStep - 1));
			String thisStepString = thisStep.toString();
			String lastStepString = lastStep.toString();
			thisStepString = thisStepString.replaceAll("\\s", "");
			lastStepString = lastStepString.replaceAll("\\s", "");
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
				if(nextSiblingIndex == -1){
					nextSiblingIndex = Integer.MAX_VALUE;
				}
				if(nextChildIndex == -1){
					nextChildIndex = Integer.MAX_VALUE;
				}
				if(endIndentIndex == -1){
					endIndentIndex = Integer.MAX_VALUE;
				}
				if((endIndentIndex < nextChildIndex) && (endIndentIndex < nextSiblingIndex)){
					// place final term if there is one, then pop and place ]
					if(endIndentIndex != 0){
						//There is a preceding term, store first
						if(newIndent){
							TreeItem<Text> parentNode = nodeStack.pop();
							String term = thisStepString.substring(0, endIndentIndex);
							Text termText = new Text(term);
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
							Text termText = new Text(term);
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
				else if((nextSiblingIndex < nextChildIndex) && (nextSiblingIndex < endIndentIndex)){
					if(newIndent){
						newIndent = false;
						TreeItem<Text> parentNode = nodeStack.pop();
						String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
						Text siblingTermText = new Text(siblingTerm);
						TreeItem<Text> siblingNode = new TreeItem<Text>(siblingTermText);
						parentNode.getChildren().add(siblingNode);
						nodeStack.push(parentNode);
						nodeStack.push(siblingNode);
					}
					else{
						nodeStack.pop();
						TreeItem<Text> parentNode = nodeStack.pop();
						String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
						Text siblingTermText = new Text(siblingTerm);
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
					Text childTermText = new Text(childTerm);
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
			}
			expandChildren(terms_tree.getRoot());
			precedingLastString = "";
			precedingTerm = "";
			checkIfRewrite(lastStepString, terms_tree.getRoot());
		}
	}

	void stepBack(int stepsToGoBack){
		currentStep -= stepsToGoBack;
		if(currentStep < 0){
			currentStep = 0;
		}
		Steps s = steps.get(currentStep + 1);
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
			thisStepString = thisStepString.replaceAll("\\s", "");
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
				if(nextSiblingIndex == -1){
					nextSiblingIndex = Integer.MAX_VALUE;
				}
				if(nextChildIndex == -1){
					nextChildIndex = Integer.MAX_VALUE;
				}
				if(endIndentIndex == -1){
					endIndentIndex = Integer.MAX_VALUE;
				}
				if((endIndentIndex < nextChildIndex) && (endIndentIndex < nextSiblingIndex)){
					// place final term if there is one, then pop and place ]
					if(endIndentIndex != 0){
						//There is a preceding term, store first
						if(newIndent){
							TreeItem<Text> parentNode = nodeStack.pop();
							String term = thisStepString.substring(0, endIndentIndex);
							Text termText = new Text(term);
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
							Text termText = new Text(term);
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
				else if((nextSiblingIndex < nextChildIndex) && (nextSiblingIndex < endIndentIndex)){
					if(newIndent){
						newIndent = false;
						TreeItem<Text> parentNode = nodeStack.pop();
						String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
						Text siblingTermText = new Text(siblingTerm);
						TreeItem<Text> siblingNode = new TreeItem<Text>(siblingTermText);
						parentNode.getChildren().add(siblingNode);
						nodeStack.push(parentNode);
						nodeStack.push(siblingNode);
					}
					else{
						nodeStack.pop();
						TreeItem<Text> parentNode = nodeStack.pop();
						String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
						Text siblingTermText = new Text(siblingTerm);
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
					Text childTermText = new Text(childTerm);
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
			}
			expandChildren(terms_tree.getRoot());
		}
	}

	@FXML
	void onStepBack() {
		stepBack(1);
		step(0);
	}
	/*
    public void setDbService(DataService service) {
        this.ts = service;
    }*/

	/**
	 * Jump to the first breakpoint. If there are no breakpoints set, then the
	 * program will step through each instruction per user input. That is with
	 * no breakpoints set then it is up to the user to step through the trace;
	 * the visualizer will not run through on its own.
	 *
	 */
	void jumpToNextStep() {
		if (!observableBreakpoints.isEmpty()) {
			int stepsToAdvance = 0;
			while (currentStep < totalSteps && proceed) {
				Steps step = steps.get(currentStep + stepsToAdvance);
				String rule = rules.get(step.getActiveRuleId()).getValue();
				stepsToAdvance++;
				proceed = !(observableBreakpoints.contains(rule));
			}
			step(stepsToAdvance);
		}
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
		double majorTick = Math.floor((totalSteps - 1) / 10);
		slider.setDisable(false);
		slider.setMax(totalSteps - 1);
		slider.setMajorTickUnit(majorTick <= 0 ? 1 : majorTick);
		slider.setMinorTickCount((int) Math.floor(slider.getMajorTickUnit()) / 5);
		slider.setValue(0);
	}

	@FXML
	public void onStepSpecify() {
		int specifiedStep = Integer.valueOf(step_specifier.getText());
		if(specifiedStep < 0 || specifiedStep > steps.size()){
			return;
		}
		else if(specifiedStep < currentStep){
			stepBack(currentStep - specifiedStep);
			step(0);
		}
		else if(specifiedStep > currentStep){
			step(specifiedStep - currentStep);
		}
	}

	private void expandChildren(TreeItem<Text> node) {
		node.setExpanded(true);
		for (TreeItem<Text> child : node.getChildren()) {
			expandChildren(child);
		}
	}

	private void checkIfRewrite(String lastStepString, TreeItem<Text> term) {
		boolean functionRewrite = false;
		boolean functionShift = false;
		for (TreeItem<Text> child : term.getChildren()) {
			if (functionRewrite && child.getValue().getText().contains("]")) {
				child.getValue().setFill(Color.BLUE);
				child.getValue().setFont(Font.font("System", FontWeight.BOLD, 12));
				functionRewrite = false;
			} 
			else if(functionShift &&  child.getValue().getText().contains("]")) {
				child.getValue().setFill(Color.GREEN);
				child.getValue().setFont(Font.font("System", FontWeight.BOLD, 12));
				functionShift = false;
			} 
			else if (lastStepString.contains(child.getValue().getText())) {
				int startIndex = lastStepString.indexOf(child.getValue().getText());
				int endIndex = startIndex + child.getValue().getText().length();
				if(!precedingTerm.equals("") && !precedingLastString.equals("") && !precedingTerm.equals(precedingLastString)  && !child.getValue().getText().contains("]")){
					recursiveShiftHighlight(child);
					functionShift = true;
				}
				String firstHalf = lastStepString.substring(0, startIndex);
				precedingLastString = lastStepString.substring(startIndex, endIndex);
				String lastHalf = lastStepString.substring(endIndex, lastStepString.length());
				lastStepString = firstHalf.concat(lastHalf);
			} 
			else {
				if (child.getValue().getText().contains("[")) {
					functionRewrite = true;
				}
				child.getValue().setFill(Color.BLUE);
				child.getValue().setFont(Font.font("System", FontWeight.BOLD, 12));			
			}
			precedingTerm = child.getValue().getText();
			checkIfRewrite(lastStepString, child);
			precedingTerm = child.getValue().getText();
		}
	}
	
	private void recursiveShiftHighlight(TreeItem<Text> term){
		term.getValue().setFill(Color.GREEN);
		term.getValue().setFont(Font.font("System", FontWeight.BOLD, 12));
		for (TreeItem<Text> child : term.getChildren()) {
			recursiveShiftHighlight(child);
		}
	}
	
	@Override
	public void dataClosed() {
		observableBreakpoints = FXCollections.observableArrayList();
		observableRules = FXCollections.observableArrayList();
		steps = null;
		rules = null;
		totalSteps = lastIndent = 0;
		currentStep = -1;
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

	@Override
	public void dataLoaded() {
		steps = ts.allSteps();
		for (Steps step : steps) {
			System.out.println("Step " + step.getStepNum() + " Indentation level " + step.getIndentation() + " : " + step.getStartData());
		}
		rules = ts.allRules();
		totalSteps = steps.size();
		currentStep = -1;
		lastIndent = 0;

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
		onStepInto(null);
		onStepBack();
	}
}
