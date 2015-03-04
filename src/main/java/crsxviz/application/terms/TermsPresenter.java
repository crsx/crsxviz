package crsxviz.application.terms;

import crsxviz.application.crsxviz.CrsxvizPresenter;
import crsxviz.persistence.beans.ActiveRules;
import crsxviz.persistence.beans.Steps;
import crsxviz.persistence.services.TraceService;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javax.inject.Inject;

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
    private Button terminate;
    @FXML
    private Label trace_label;
    @FXML
    private TreeView<String> terms_tree;

    @Inject
    TraceService ts;

    private int lastIndent = 0, currentStep = 0;
    Stack<TreeItem<String>> nodeStack;
    Stack<Steps> stepStack;

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

    }

    @FXML
    void onRun(ActionEvent event) {
        run.setDisable(true);
        resume.setDisable(false);
        step_over.setDisable(false);
        System.out.println("running debug...");

        nodeStack = initializeTree(new TreeItem<>("Terms"));
    }

    @FXML
    void onResume(ActionEvent event) {
        proceed = true;
        jumpToNextStep();
        System.out.println("resuming debug...");
    }

    @FXML
    void onTerminate(ActionEvent event) {
        run.setDisable(false);
        resume.setDisable(true);
        step_into.setDisable(true);
        step_return.setDisable(true);
        step_over.setDisable(true);
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

    void step() {
        if (currentStep < totalSteps && proceed) {
            Steps s = steps.get(currentStep);

            if (s.getIndentation() > lastIndent) {
                // Next indentation level, should always be a new term/subterm
                lastIndent = s.getIndentation();
                TreeItem<String> newNode = new TreeItem<>("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                s.setStartDataDisplayed(true);
                TreeItem<String> currentNode = nodeStack.pop();
                currentNode.getChildren().add(newNode);
                nodeStack.push(currentNode);
                // New stack node is the first child of this indentation level for this node's parent
                nodeStack.push(newNode);
                stepStack.push(s);

                terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                terms_tree.requestFocus();
                terms_tree.getSelectionModel().select(newNode);
                terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());

                main.rulesPresenter.highlightActiveRule(s.getActiveRuleId());

                if (currentStep < totalSteps - 1) {
                    currentStep++;
                }
            } else if (s.getIndentation() < lastIndent) {
                // Previous indentation level, startData should always be the completeData for the previous node at this indentation level
                lastIndent = s.getIndentation();
                //TreeItem<String> newNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                nodeStack.pop();
                stepStack.pop();
                TreeItem<String> currentNode = nodeStack.pop();
                currentNode.setValue("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                s.setStartDataDisplayed(true);
                currentNode.getChildren().clear();
                nodeStack.push(currentNode);
                stepStack.pop();
                stepStack.push(s);
                // New stack node is the next child of the indentation level for the previous node's parent

                terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                terms_tree.requestFocus();
                terms_tree.getSelectionModel().select(currentNode);
                terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());

                main.rulesPresenter.highlightActiveRule(s.getActiveRuleId());

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
                    currentNode.setValue("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getCompleteData() + "\n");
                    s.setCompleteDataDisplayed(true);
                    currentNode.getChildren().clear();
                    nodeStack.push(currentNode);

                    terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    terms_tree.requestFocus();
                    terms_tree.getSelectionModel().select(currentNode);
                    terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());

                    currentStep++;
                } else {
                    TreeItem<String> currentNode = nodeStack.pop();
                    currentNode.setValue("Indentation level " + s.getIndentation() + ": Step " + s.getStepNum() + ":\n" + s.getStartData() + "\n");
                    nodeStack.push(currentNode);

                    if (currentStep + 1 < totalSteps && steps.get(currentStep + 1).getIndentation() < s.getIndentation() && !s.isStartDataDisplayed() && !s.isCompleteDataDisplayed()) {

                    } else if (stepStack.pop().isStartDataDisplayed() && currentStep < totalSteps - 1) {
                        currentStep++;
                    }

                    s.setStartDataDisplayed(true);
                    stepStack.push(s);

                    terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    terms_tree.requestFocus();
                    terms_tree.getSelectionModel().select(currentNode);
                    terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
                }

                main.rulesPresenter.highlightActiveRule(s.getActiveRuleId());
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
        }
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
        terms_tree.setRoot(root);
        root.setExpanded(true);
        nodeStack.push(root);
        currentStep = 0;
        lastIndent = 0;
        System.out.println("Term Tree:\n");
        return nodeStack;
    }

    /**
     * Initializes the Presenter to an initial state where either a
     * database has been opened and thus will display the correct state of
     * buttons along with initial term tree, or where a database has not been 
     * opened.
     * @param main Instance of the calling presenter
     */
    public void setCrsxMain(CrsxvizPresenter main) {
        this.main = main;
        steps = main.getSteps();
        rules = main.getRules();
        totalSteps = steps.size();
        
        String label = main.getDbName();
        trace_label.setText(label == null ? "No trace file opened" : "Debugging " + label);
        observableRules = main.getObservableRules();
        observableBreakpoints = main.getBreakpoints();
        
        nodeStack = initializeTree(new TreeItem<>("Terms"));
        stepStack = new Stack<>();
        
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
        totalSteps = lastIndent =  currentStep = 0;
        terms_tree.setRoot(null);
    	run.setDisable(true);
    	terminate.setDisable(true);
    	step_into.setDisable(true);
    	step_over.setDisable(true);
    	step_return.setDisable(true);
    	resume.setDisable(true);
    	trace_label.setText("No trace file opened");
    }
    
}
