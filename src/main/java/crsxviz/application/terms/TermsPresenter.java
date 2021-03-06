package crsxviz.application.terms;

import crsxviz.application.Utilities;
import crsxviz.application.rules.RulesPresenter;
import crsxviz.persistence.DataListener;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.CompiledSteps;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.IDataService;
import java.io.IOException;

import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class TermsPresenter extends AnchorPane implements DataListener {

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

    private IDataService ts;

    private int lastIndent = 0, currentStep = -1, previousSliderValue = 0;

    private String precedingLastString = "";
    private String precedingTerm = "";

    private List<Steps> steps;
    private List<ActiveRules> rules;
    private int totalSteps;
    private Stack<TreeItem<Text>> nodeStack;

    private ObservableList<Text> observableBreakpoints;
    
    public static TermsPresenter presenter;

    public TermsPresenter() {
        initialize();
    }
    
    private void initialize() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(TermsPresenter.class.getResource("terms.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(TermsPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        presenter = loader.<TermsPresenter>getController();
        
        this.offConfiguration();
    }
    
    /**
     * Used to attain a static reference to this presenter
     * 
     * @return Presenter object representing this presenter
     */
    public static TermsPresenter getPresenter() {
        return presenter;
    }

    @FXML
    void onRun(ActionEvent event) {
        this.onConfiguration();
        run.setDisable(true);
        currentStep = -1;
        step(1);
    }

    @FXML
    void onResume(ActionEvent event) {
        jumpToNextStep();
    }

    @FXML
    void onTerminate(ActionEvent event) {
        this.offConfiguration();
        run.setDisable(false);
    }

    @FXML
    void onStepInto(ActionEvent event) {
        if (currentStep <= totalSteps) {
            step(1);
        }
    }

    @FXML
    void onStepOver(ActionEvent event) {
        if (currentStep <= totalSteps) {
            if (lastIndent == 0) {
                step(1);
            } else {
                int stepsToAdvance = 0;
                Steps s = null;
                if(currentStep <= 0){
                	s = steps.get(currentStep);
                }
                else{
                	s = steps.get(currentStep - 1);
                }
                int currentIndent = s.getIndentation();
                s = steps.get(currentStep);
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
        if (currentStep > totalSteps - 1) {
            currentStep = totalSteps - 1;
        }
        if (currentStep < totalSteps) {
            Steps s = steps.get(currentStep);
            RulesPresenter.getPresenter().setNextRule(s.getActiveRuleId(), Color.GREEN);
            if (currentStep < steps.size() - 1)
            {
            	int nextRule = steps.get(currentStep + 1).getActiveRuleId();
            	RulesPresenter.getPresenter().setNextRule(nextRule, Color.BLUE);
            }
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
            if (currentStep > 0) {
                lastIndent = s.getIndentation();
            } else {
                lastIndent = 1;
            }
            if (currentStep > 0) {
                step_back.setDisable(false);
            }
        } else {
            currentStep -= stepsToAdvance;
        }
        previousSliderValue = (int) slider.getValue();
        slider.setValue(currentStep);
        step_specifier.setText("" + currentStep);

        CompiledSteps thisStep = ts.getCompiledStep(new Long(currentStep));
        if (currentStep > 0) {
            CompiledSteps lastStep = ts.getCompiledStep(new Long(currentStep - 1));
            String thisStepString = thisStep.toString();
            String lastStepString = lastStep.toString();
            thisStepString = thisStepString.replaceAll("\\s", "");
            lastStepString = lastStepString.replaceAll("\\s", "");
            TreeItem t = new TreeItem<>(new Text("Full Term String"));
            this.terms_tree.setRoot(t);
            nodeStack = new Stack<>();
            nodeStack.push(t);
            boolean newIndent = true;
            while (!thisStepString.equals("")) {
                int nextSiblingIndex = thisStepString.indexOf(',');
                int nextChildIndex = thisStepString.indexOf('[');
                int endIndentIndex = thisStepString.indexOf(']');
                if (nextSiblingIndex == -1 && nextChildIndex == -1 && endIndentIndex == -1) {
                    break;
                }
                if (nextSiblingIndex == -1) {
                    nextSiblingIndex = Integer.MAX_VALUE;
                }
                if (nextChildIndex == -1) {
                    nextChildIndex = Integer.MAX_VALUE;
                }
                if (endIndentIndex == -1) {
                    endIndentIndex = Integer.MAX_VALUE;
                }
                if ((endIndentIndex < nextChildIndex) && (endIndentIndex < nextSiblingIndex)) {
                    // place final term if there is one, then pop and place ]
                    if (endIndentIndex != 0) {
                        //There is a preceding term, store first
                        if (newIndent) {
                            TreeItem<Text> parentNode = nodeStack.pop();
                            String term = thisStepString.substring(0, endIndentIndex);
                            Text termText = new Text(term);
                            TreeItem<Text> termNode = new TreeItem<>(termText);
                            parentNode.getChildren().add(termNode);
                            nodeStack.push(parentNode);
                            nodeStack.push(termNode);
                            newIndent = false;
                        } else {
                            nodeStack.pop();
                            TreeItem<Text> parentNode = nodeStack.pop();
                            String term = thisStepString.substring(0, endIndentIndex);
                            Text termText = new Text(term);
                            TreeItem<Text> termNode = new TreeItem<>(termText);
                            parentNode.getChildren().add(termNode);
                            nodeStack.push(parentNode);
                            nodeStack.push(termNode);
                        }
                    }
                    nodeStack.pop();
                    TreeItem<Text> parentNode = nodeStack.pop();
                    if (endIndentIndex + 1 >= thisStepString.length()) {
                        thisStepString = "";
                    } else {
                        if (thisStepString.charAt(endIndentIndex + 1) == ',') {
                            TreeItem<Text> endIndentNode = new TreeItem<>(new Text("],"));
                            parentNode.getParent().getChildren().add(endIndentNode);
                            nodeStack.push(endIndentNode);
                            thisStepString = thisStepString.substring(endIndentIndex + 2, thisStepString.length());
                        } else {
                            TreeItem<Text> endIndentNode = new TreeItem<>(new Text("]"));
                            parentNode.getParent().getChildren().add(endIndentNode);
                            nodeStack.push(endIndentNode);
                            thisStepString = thisStepString.substring(endIndentIndex + 1, thisStepString.length());
                        }

                    }
                } else if ((nextSiblingIndex < nextChildIndex) && (nextSiblingIndex < endIndentIndex)) {
                    if (newIndent) {
                        newIndent = false;
                        TreeItem<Text> parentNode = nodeStack.pop();
                        String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
                        Text siblingTermText = new Text(siblingTerm);
                        TreeItem<Text> siblingNode = new TreeItem<>(siblingTermText);
                        parentNode.getChildren().add(siblingNode);
                        nodeStack.push(parentNode);
                        nodeStack.push(siblingNode);
                    } else {
                        nodeStack.pop();
                        TreeItem<Text> parentNode = nodeStack.pop();
                        String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
                        Text siblingTermText = new Text(siblingTerm);
                        TreeItem<Text> siblingNode = new TreeItem<>(siblingTermText);
                        parentNode.getChildren().add(siblingNode);
                        nodeStack.push(parentNode);
                        nodeStack.push(siblingNode);
                    }
                    if (nextSiblingIndex + 1 >= thisStepString.length()) {
                        thisStepString = "";
                    } else {
                        thisStepString = thisStepString.substring(nextSiblingIndex + 1, thisStepString.length());
                    }
                } else {
                    if (!newIndent) {
                        nodeStack.pop();
                    }
                    TreeItem<Text> parentNode = nodeStack.pop();
                    String childTerm = thisStepString.substring(0, nextChildIndex + 1);
                    Text childTermText = new Text(childTerm);
                    TreeItem<Text> childNode = new TreeItem<>(childTermText);
                    parentNode.getChildren().add(childNode);
                    nodeStack.push(parentNode);
                    nodeStack.push(childNode);
                    newIndent = true;
                    if (nextChildIndex + 1 >= thisStepString.length()) {
                        thisStepString = "";
                    } else {
                        thisStepString = thisStepString.substring(nextChildIndex + 1, thisStepString.length());
                    }
                }
            }
            terms_tree.getRoot().getChildren().add(new TreeItem<>(new Text("]")));
            expandChildren(terms_tree.getRoot());
            precedingLastString = "";
            precedingTerm = "";
            checkIfRewrite(lastStepString, terms_tree.getRoot());
        }
    }

    void stepBack(int stepsToGoBack) {
        currentStep -= stepsToGoBack;
        if (currentStep < 0) {
            currentStep = 0;
        }
        Steps s = steps.get(currentStep + 1);
        RulesPresenter.getPresenter().highlightActiveRule(s.getActiveRuleId());
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
        if (currentStep < steps.size()) {
            String thisStepString = thisStep.toString();
            thisStepString = thisStepString.replaceAll("\\s", "");
            TreeItem t = new TreeItem<>(new Text("Full Term String"));
            this.terms_tree.setRoot(t);
            nodeStack = new Stack<>();
            nodeStack.push(t);
            boolean newIndent = true;
            while (!thisStepString.equals("")) {
                int nextSiblingIndex = thisStepString.indexOf(',');
                int nextChildIndex = thisStepString.indexOf('[');
                int endIndentIndex = thisStepString.indexOf(']');
                if (nextSiblingIndex == -1 && nextChildIndex == -1 && endIndentIndex == -1) {
                    break;
                }
                if (nextSiblingIndex == -1) {
                    nextSiblingIndex = Integer.MAX_VALUE;
                }
                if (nextChildIndex == -1) {
                    nextChildIndex = Integer.MAX_VALUE;
                }
                if (endIndentIndex == -1) {
                    endIndentIndex = Integer.MAX_VALUE;
                }
                if ((endIndentIndex < nextChildIndex) && (endIndentIndex < nextSiblingIndex)) {
                    // place final term if there is one, then pop and place ]
                    if (endIndentIndex != 0) {
                        //There is a preceding term, store first
                        if (newIndent) {
                            TreeItem<Text> parentNode = nodeStack.pop();
                            String term = thisStepString.substring(0, endIndentIndex);
                            Text termText = new Text(term);
                            TreeItem<Text> termNode = new TreeItem<>(termText);
                            parentNode.getChildren().add(termNode);
                            nodeStack.push(parentNode);
                            nodeStack.push(termNode);
                            newIndent = false;
                        } else {
                            nodeStack.pop();
                            TreeItem<Text> parentNode = nodeStack.pop();
                            String term = thisStepString.substring(0, endIndentIndex);
                            Text termText = new Text(term);
                            TreeItem<Text> termNode = new TreeItem<>(termText);
                            parentNode.getChildren().add(termNode);
                            nodeStack.push(parentNode);
                            nodeStack.push(termNode);
                        }
                    }
                    nodeStack.pop();
                    TreeItem<Text> parentNode = nodeStack.pop();
                    if (endIndentIndex + 1 >= thisStepString.length()) {
                        thisStepString = "";
                    } else {
                        if (thisStepString.charAt(endIndentIndex + 1) == ',') {
                            TreeItem<Text> endIndentNode = new TreeItem<>(new Text("],"));
                            parentNode.getParent().getChildren().add(endIndentNode);
                            nodeStack.push(endIndentNode);
                            thisStepString = thisStepString.substring(endIndentIndex + 2, thisStepString.length());
                        } else {
                            TreeItem<Text> endIndentNode = new TreeItem<>(new Text("]"));
                            parentNode.getParent().getChildren().add(endIndentNode);
                            nodeStack.push(endIndentNode);
                            thisStepString = thisStepString.substring(endIndentIndex + 1, thisStepString.length());
                        }
                    }
                } else if ((nextSiblingIndex < nextChildIndex) && (nextSiblingIndex < endIndentIndex)) {
                    if (newIndent) {
                        newIndent = false;
                        TreeItem<Text> parentNode = nodeStack.pop();
                        String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
                        Text siblingTermText = new Text(siblingTerm);
                        TreeItem<Text> siblingNode = new TreeItem<>(siblingTermText);
                        parentNode.getChildren().add(siblingNode);
                        nodeStack.push(parentNode);
                        nodeStack.push(siblingNode);
                    } else {
                        nodeStack.pop();
                        TreeItem<Text> parentNode = nodeStack.pop();
                        String siblingTerm = thisStepString.substring(0, nextSiblingIndex + 1);
                        Text siblingTermText = new Text(siblingTerm);
                        TreeItem<Text> siblingNode = new TreeItem<>(siblingTermText);
                        parentNode.getChildren().add(siblingNode);
                        nodeStack.push(parentNode);
                        nodeStack.push(siblingNode);
                    }
                    if (nextSiblingIndex + 1 >= thisStepString.length()) {
                        thisStepString = "";
                    } else {
                        thisStepString = thisStepString.substring(nextSiblingIndex + 1, thisStepString.length());
                    }
                } else {
                    if (!newIndent) {
                        nodeStack.pop();
                    }
                    TreeItem<Text> parentNode = nodeStack.pop();
                    String childTerm = thisStepString.substring(0, nextChildIndex + 1);
                    Text childTermText = new Text(childTerm);
                    TreeItem<Text> childNode = new TreeItem<>(childTermText);
                    parentNode.getChildren().add(childNode);
                    nodeStack.push(parentNode);
                    nodeStack.push(childNode);
                    newIndent = true;
                    if (nextChildIndex + 1 >= thisStepString.length()) {
                        thisStepString = "";
                    } else {
                        thisStepString = thisStepString.substring(nextChildIndex + 1, thisStepString.length());
                    }
                }
            }
            terms_tree.getRoot().getChildren().add(new TreeItem<>(new Text("]")));
            expandChildren(terms_tree.getRoot());
        }
    }

    @FXML
    void onStepBack() {
        stepBack(1);
        step(0);
    }
    
    public void setService(IDataService service) {
        this.ts = service;
        ts.addListener(this);
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
            int stepsToAdvance = 0;
            while (currentStep < totalSteps - 1 && currentStep + stepsToAdvance < totalSteps - 1) {
                Steps step = steps.get(currentStep + stepsToAdvance);
                String rule = rules.get(step.getActiveRuleId()).getValue();
                stepsToAdvance++;
                if ( Utilities.contains(observableBreakpoints, rule) )
                    break;
            }
            step(stepsToAdvance);
        }
    }
    
    public void offConfiguration() {
        observableBreakpoints = FXCollections.observableArrayList();
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
        step_back.setDisable(true);
        resume.setDisable(true);
        trace_label.setText("No trace file opened");
        step_specifier.setText("");
        step_specifier.setEditable(false);
        step_specifier.setFocusTraversable(false);
        terms_tree.setRoot(null);
        initialSliderState();
    }
    
    public void onConfiguration() {
        steps = ts.allSteps();
        rules = ts.allRules();
        totalSteps = steps.size();
        for (Steps step : steps) {
            System.out.println("Step " + step.getStepNum() + " Indentation level " + step.getIndentation() + " : " + step.getStartData());
        }
        currentStep = -1;
        lastIndent = 0;

        String label = ts.getDataName();
        if (!label.isEmpty()) {
            trace_label.setText("Debugging " + label);
            observableBreakpoints = ts.allObservableBreakpoints();

            sliderOn();
            step_specifier.setText("");
            step_specifier.setEditable(true);
            step_specifier.setFocusTraversable(false);
            step_return.setDisable(false);
            step_into.setDisable(false);
            step_over.setDisable(false);
            run.setDisable(false);
            resume.setDisable(false);
            terminate.setDisable(false);
            onStepInto(null);
            onStepInto(null);
            onStepBack();
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
        if (specifiedStep < currentStep) {
            stepBack(currentStep - specifiedStep);
            step(0);
        } else if (specifiedStep > currentStep) {
            step(specifiedStep - currentStep);
        }
    }

    private void expandChildren(TreeItem<Text> node) {
        node.setExpanded(true);
        node.getChildren().stream().forEach(this::expandChildren);
    }

    private void checkIfRewrite(String lastStepString, TreeItem<Text> term) {
        boolean functionRewrite = false;
        boolean functionShift = false;
        for (TreeItem<Text> child : term.getChildren()) {
            if (functionRewrite && child.getValue().getText().contains("]")) {
                child.getValue().setFill(Color.BLUE);
                child.getValue().setFont(Font.font("System", FontWeight.BOLD, 12));
                functionRewrite = false;
            } else if (functionShift && child.getValue().getText().contains("]")) {
                child.getValue().setFill(Color.GREEN);
                child.getValue().setFont(Font.font("System", FontWeight.BOLD, 12));
                functionShift = false;
            } else if (lastStepString.contains(child.getValue().getText())) {
                int startIndex = lastStepString.indexOf(child.getValue().getText());
                int endIndex = startIndex + child.getValue().getText().length();
                if (!precedingTerm.equals("") && !precedingLastString.equals("") && !precedingTerm.equals(precedingLastString) && !child.getValue().getText().contains("]")) {
                    recursiveShiftHighlight(child);
                    functionShift = true;
                }
                String firstHalf = lastStepString.substring(0, startIndex);
                precedingLastString = lastStepString.substring(startIndex, endIndex);
                String lastHalf = lastStepString.substring(endIndex, lastStepString.length());
                lastStepString = firstHalf.concat(lastHalf);
            } else {
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

    private void recursiveShiftHighlight(TreeItem<Text> term) {
        term.getValue().setFill(Color.GREEN);
        term.getValue().setFont(Font.font("System", FontWeight.BOLD, 12));
        term.getChildren().stream().forEach(this::recursiveShiftHighlight);
    }

    @Override
    public void dataClosed() {
        this.offConfiguration();
    }

    @Override
    public void dataLoaded() {
        this.onConfiguration();
    }
}
