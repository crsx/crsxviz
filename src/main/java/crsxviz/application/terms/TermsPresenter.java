package crsxviz.application.terms;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.TraceService;
import java.net.URL;
import java.util.LinkedList;
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

import javax.inject.Inject;
import jdk.nashorn.internal.runtime.regexp.joni.EncodingHelper;

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
    private TreeView<String> terms_tree;
    @FXML
    private Slider slider;
    @FXML
    private TextField step_specifier;

    @Inject
    TraceService ts;

    private int lastIndent = 0, currentStep = 0, previousSliderValue = 0;
    Stack<TreeItem<String>> nodeStack;
    Stack<Steps> stepStack;
    TreeView<String> complete_tree;
    Stack<TreeItem<String>> completeNodeStack;
    LinkedList<Steps> stepsSoFar;
    LinkedList<TreeItem<String>> nodesSoFar;

    // Controls progress through the trace by providing means to pause
    // in a given location, used primarily to pause on breakpoints
    private boolean proceed;

    private List<Steps> steps;
    private List<ActiveRules> rules;
    private int totalSteps;

    private ObservableList<String> observableBreakpoints = FXCollections.observableArrayList();
    private ObservableList<String> observableRules = FXCollections.observableArrayList();

    private CrsxvizPresenter main;

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
        System.out.println("running debug...");
        step_specifier.setText("");
        step_specifier.setEditable(true);
        step_specifier.setFocusTraversable(false);

        nodeStack = initializeTree(new TreeItem<>("Terms"));
        stepStack = new Stack<>();
        stepsSoFar = new LinkedList<>();
        nodesSoFar = new LinkedList<>();
    }

    @FXML
    void onResume(ActionEvent event) {
        proceed = true;
        jumpToNextStep();
        System.out.println("resuming debug...");
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
        for (int i = 0; i < totalSteps; i++) {
            steps.get(i).setStartDataDisplayed(false);
            steps.get(i).setCompleteDataDisplayed(false);
        }
        System.out.println("terminating debug...");
    }

    @FXML
    void onStepInto(ActionEvent event) {
        proceed = true;
        if (currentStep <= totalSteps) {
            step();
            System.out.println("stepping into...");
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
            System.out.println("stepping over...");
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
            System.out.println("returning to step...");
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

            if (s.getIndentation() > lastIndent) {
                // Next indentation level, should always be a new term/subterm
                lastIndent = s.getIndentation();
                TreeItem<String> newNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                TreeItem<String> newCompleteNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                s.setStartDataDisplayed(true);
                TreeItem<String> currentNode = nodeStack.pop();
                TreeItem<String> currentCompleteNode = completeNodeStack.pop();
                currentNode.getChildren().add(newNode);
                currentCompleteNode.getChildren().add(newCompleteNode);
                nodeStack.push(currentNode);
                completeNodeStack.push(currentCompleteNode);
                // New stack node is the first child of this indentation level for this node's parent
                nodeStack.push(newNode);
                completeNodeStack.push(newCompleteNode);
                nodesSoFar.add(newCompleteNode);
                stepStack.push(s);
                stepsSoFar.add(s);

                nodeFocus(newNode);

                CrsxvizPresenter.getRulesPresenter().highlightActiveRule(s.getActiveRuleId());

                if (currentStep < totalSteps - 1) {
                    currentStep++;
                }
            } else if (s.getIndentation() < lastIndent) {
                // Previous indentation level, startData should always be the completeData for the previous node at this indentation level
                lastIndent = s.getIndentation();
                TreeItem<String> newCompleteNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                nodeStack.pop();
                completeNodeStack.pop();
                stepStack.pop();
                TreeItem<String> currentNode = nodeStack.pop();
                TreeItem<String> currentCompleteNode = completeNodeStack.pop();
                currentNode.setValue("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                s.setStartDataDisplayed(true);
                currentNode.getChildren().clear();
                currentCompleteNode.getParent().getChildren().add(newCompleteNode);
                completeNodeStack.push(newCompleteNode);
                nodesSoFar.add(newCompleteNode);
                nodeStack.push(currentNode);
                stepStack.pop();
                stepStack.push(s);
                stepsSoFar.add(s);
                // New stack node is the next child of the indentation level for the previous node's parent

                nodeFocus(currentNode);

                CrsxvizPresenter.getRulesPresenter().highlightActiveRule(s.getActiveRuleId());

                if (currentStep < totalSteps - 1) {
                    if (currentStep + 1 < totalSteps && steps.get(currentStep + 1).getIndentation() < s.getIndentation() && s.isStartDataDisplayed() && !s.isCompleteDataDisplayed()) {

                    } else {
                        currentStep++;
                    }
                }
            } else {
                // Same indentation level, new child of previous indentation level
                // If the next step is at a previous indentation level (or if this is the final step), display completeData for this step before moving to next step (or finishing the rewrite)
                if (((currentStep == totalSteps - 1) || (currentStep + 1 < totalSteps && steps.get(currentStep + 1).getIndentation() < s.getIndentation())) && s.isStartDataDisplayed() && !s.isCompleteDataDisplayed()) {
                    TreeItem<String> currentNode = nodeStack.pop();
                    TreeItem<String> currentCompleteNode = completeNodeStack.pop();
                    currentNode.setValue("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getCompleteData() + "\n");
                    currentCompleteNode.setValue("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getCompleteData() + "\n");
                    s.setCompleteDataDisplayed(true);
                    currentNode.getChildren().clear();
                    nodeStack.push(currentNode);
                    completeNodeStack.push(currentCompleteNode);
                    nodesSoFar.removeLast();
                    nodesSoFar.add(currentCompleteNode);
                    stepsSoFar.removeLast();
                    stepsSoFar.add(s);
                    stepStack.pop();
                    stepStack.push(s);

                    nodeFocus(currentNode);

                    currentStep++;
                } else {
                    Steps currentStackStep = stepStack.pop();
                    TreeItem<String> currentNode = nodeStack.pop();
                    TreeItem<String> currentCompleteNode = completeNodeStack.pop();
                    TreeItem<String> newCompleteNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                    currentCompleteNode.getParent().getChildren().add(newCompleteNode);
                    currentNode.setValue("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                    nodeStack.push(currentNode);
                    completeNodeStack.push(newCompleteNode);
                    nodesSoFar.add(newCompleteNode);
                    if (currentStep + 1 < totalSteps && steps.get(currentStep + 1).getIndentation() < s.getIndentation() && !s.isStartDataDisplayed() && !s.isCompleteDataDisplayed()) {

                    } else if (currentStackStep.isStartDataDisplayed() && currentStep < totalSteps - 1) {
                        //currentStep++;
                    }
                    currentStep++;
                    s.setStartDataDisplayed(true);
                    stepStack.push(s);
                    stepsSoFar.add(s);

                    nodeFocus(currentNode);
                }

                CrsxvizPresenter.getRulesPresenter().highlightActiveRule(s.getActiveRuleId());
            }

            System.out.println("---------------------------------------------------");
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
            step_back.setDisable(false);
        }
        previousSliderValue = (int) slider.getValue();
        slider.setValue(currentStep);
        step_specifier.setText("" + currentStep);
    }

    @FXML
    void onStepBack() {
        System.out.println("stepping back...");
        if (stepsSoFar.size() == 1) {
            stepsSoFar.clear();
            stepStack.clear();
            nodesSoFar.clear();
            TreeItem<String> currentNode = nodeStack.pop();
            currentNode.getParent().getChildren().clear();
            currentStep--;
            lastIndent = 0;
        } else {
            Steps currentLinkedStep = stepsSoFar.removeLast();
            steps.get(currentStep - 1).setStartDataDisplayed(false);
            if (currentLinkedStep.isCompleteDataDisplayed()) {
                // Previous step is same step with start data displayed
                currentLinkedStep.setCompleteDataDisplayed(false);
                TreeItem<String> currentCompleteNode = nodesSoFar.removeLast();
                currentCompleteNode.setValue("Indentation level " + currentLinkedStep.getIndentation() + ": Step " + currentLinkedStep.getStepNum() + ":\n" + currentLinkedStep.getStartData() + "\n");
                nodesSoFar.add(currentCompleteNode);
                Steps currentStackStep = stepStack.pop();
                currentStackStep.setCompleteDataDisplayed(false);
                stepStack.push(currentStackStep);
                TreeItem<String> currentNode = nodeStack.pop();
                currentNode.setValue("Indentation level " + currentLinkedStep.getIndentation() + ": Step " + currentLinkedStep.getStepNum() + ":\n" + currentLinkedStep.getStartData() + "\n");
                nodeStack.push(currentNode);
                TreeItem<String> currentCompleteStackNode = completeNodeStack.pop();
                currentCompleteStackNode.setValue("Indentation level " + currentLinkedStep.getIndentation() + ": Step " + currentLinkedStep.getStepNum() + ":\n" + currentLinkedStep.getStartData() + "\n");
                completeNodeStack.push(currentCompleteStackNode);
                stepsSoFar.add(currentLinkedStep);
                lastIndent = currentLinkedStep.getIndentation();
                terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                terms_tree.requestFocus();
                terms_tree.getSelectionModel().select(currentNode);
                terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
                CrsxvizPresenter.getRulesPresenter().highlightActiveRule(currentLinkedStep.getActiveRuleId());
            } else if (!currentLinkedStep.isCompleteDataDisplayed()) {
                // Previous step is either this step's parent, the previous sibling, or the last child of the previous sibling
                Steps previousLinkedStep = stepsSoFar.removeLast();
                if (currentLinkedStep.getIndentation() > previousLinkedStep.getIndentation()) {
                    // Previous step is the step's parent
                    nodesSoFar.removeLast();
                    TreeItem<String> previousCompleteNode = nodesSoFar.removeLast();
                    previousCompleteNode.getChildren().clear();
                    nodesSoFar.add(previousCompleteNode);
                    nodeStack.pop();
                    TreeItem<String> currentNode = nodeStack.pop();
                    currentNode.getChildren().clear();
                    nodeStack.push(currentNode);
                    completeNodeStack.pop();
                    stepStack.pop();
                    stepsSoFar.add(previousLinkedStep);
                    lastIndent = previousLinkedStep.getIndentation();
                    terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    terms_tree.requestFocus();
                    terms_tree.getSelectionModel().select(currentNode);
                    terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
                    CrsxvizPresenter.getRulesPresenter().highlightActiveRule(previousLinkedStep.getActiveRuleId());
                    currentStep--;
                } else if (currentLinkedStep.getIndentation() < previousLinkedStep.getIndentation()) {
                    // Previous step is the final child of this step's previous sibling
                    TreeItem<String> currentCompleteNode = nodesSoFar.removeLast();
                    TreeItem<String> previousCompleteNode = nodesSoFar.removeLast();
                    TreeItem<String> previousCompleteParentNode = previousCompleteNode.getParent();
                    TreeItem<String> currentNode = nodeStack.pop();
                    TreeItem<String> root = currentNode.getParent();
                    // Recreate nodes
                    TreeItem<String> newParentNode = new TreeItem<String>(previousCompleteParentNode.getValue());
                    TreeItem<String> newChildNode = new TreeItem<String>(previousCompleteNode.getValue());
                    newParentNode.getChildren().add(newChildNode);
                    TreeItem<String> newCompleteParentNode = new TreeItem<String>(previousCompleteParentNode.getValue());
                    TreeItem<String> newCompleteChildNode = new TreeItem<String>(previousCompleteNode.getValue());
                    newCompleteParentNode.getChildren().add(newCompleteChildNode);
                    root.getChildren().add(newParentNode);
                    currentCompleteNode.getParent().getChildren().remove(currentCompleteNode);
                    root.getChildren().remove(currentNode);
                    completeNodeStack.pop();
                    TreeItem<String> completeRoot = completeNodeStack.pop();

                    completeRoot.getChildren().add(newCompleteParentNode);
                    completeNodeStack.push(completeRoot);
                    completeNodeStack.push(newCompleteParentNode);
                    completeNodeStack.push(newCompleteChildNode);
                    stepStack.pop();
                    // Get parent step and push onto stack
                    Stack<Steps> tempSteps = new Stack<>();
                    Steps nextLinkedStep = stepsSoFar.removeLast();
                    while (nextLinkedStep.getIndentation() >= previousLinkedStep.getIndentation()) {
                        tempSteps.push(nextLinkedStep);
                        nextLinkedStep = stepsSoFar.removeLast();
                    }
                    tempSteps.push(nextLinkedStep);
                    stepStack.push(nextLinkedStep);
                    while (!tempSteps.isEmpty()) {
                        stepsSoFar.add(tempSteps.pop());
                    }
                    stepsSoFar.add(previousLinkedStep);
                    stepStack.push(previousLinkedStep);
                    nodeStack.push(newParentNode);
                    nodeStack.push(newChildNode);
                    lastIndent = previousLinkedStep.getIndentation();
                    nodesSoFar.add(previousCompleteNode);
                    newParentNode.setExpanded(true);
                    terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    terms_tree.requestFocus();
                    terms_tree.getSelectionModel().select(newChildNode);
                    terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
                    CrsxvizPresenter.getRulesPresenter().highlightActiveRule(previousLinkedStep.getActiveRuleId());
                    currentStep--;

                } else {
                    // Previous step is the previous sibling of this step
                    TreeItem<String> currentCompleteNode = nodesSoFar.removeLast();
                    TreeItem<String> previousCompleteNode = nodesSoFar.removeLast();
                    TreeItem<String> currentNode = nodeStack.pop();
                    TreeItem<String> root = currentNode.getParent();
                    // Recreate node and children
                    TreeItem<String> newNode = new TreeItem<>(previousCompleteNode.getValue());
                    for (TreeItem<String> child : previousCompleteNode.getChildren()) {
                        TreeItem<String> newChild = new TreeItem<>(child.getValue());
                        newNode.getChildren().add(newChild);
                    }
                    root.getChildren().add(newNode);

                    currentCompleteNode.getParent().getChildren().remove(currentCompleteNode);
                    root.getChildren().remove(currentNode);
                    completeNodeStack.pop();
                    completeNodeStack.push(previousCompleteNode);
                    stepStack.pop();
                    stepStack.push(previousLinkedStep);
                    nodeStack.push(newNode);
                    stepsSoFar.add(previousLinkedStep);
                    lastIndent = previousLinkedStep.getIndentation();
                    nodesSoFar.add(previousCompleteNode);
                    terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    terms_tree.requestFocus();
                    terms_tree.getSelectionModel().select(newNode);
                    terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
                    CrsxvizPresenter.getRulesPresenter().highlightActiveRule(previousLinkedStep.getActiveRuleId());
                    currentStep--;
                }
            }
        }

        if (currentStep <= 0) {
            step_back.setDisable(true);
        } else {
            step_back.setDisable(false);
        }
        if (currentStep < totalSteps) {
            Steps s = steps.get(currentStep);
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
        previousSliderValue = (int) slider.getValue();
        slider.setValue(currentStep);
        step_specifier.setText("" + currentStep);
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
    private Stack<TreeItem<String>> initializeTree(TreeItem<String> root) {
        nodeStack = new Stack<>();
        completeNodeStack = new Stack<>();
        terms_tree.setRoot(root);
        complete_tree = new TreeView<>(new TreeItem<>(root.getValue()));
        root.setExpanded(true);
        nodeStack.push(root);
        completeNodeStack.push(complete_tree.getRoot());
        currentStep = 0;
        lastIndent = 0;
        System.out.println("Term Tree:\n");
        return nodeStack;
    }

    /**
     * Focus on the given node in the terms_tree
     *
     * @param node Node to focus on
     */
    private void nodeFocus(TreeItem<String> node) {
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
        rules = ts.allRules();
        totalSteps = steps.size();

        String label = ts.getDbName();
        trace_label.setText(label == null ? "No trace file opened" : "Debugging " + label);
        observableRules = ts.allObservableRules();
        observableBreakpoints = ts.allObservableBreakpoints();
        
        nodeStack = initializeTree(new TreeItem<>("Terms"));
        stepStack = new Stack<>();
    	stepsSoFar = new LinkedList<>();
    	nodesSoFar = new LinkedList<>();

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
        slider.setDisable(false);
        slider.setMax(totalSteps);
        slider.setMajorTickUnit(Math.floor(totalSteps / 10));
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
