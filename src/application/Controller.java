/**
 * Sample Skeleton for 'CRSXVIZ.fxml' Controller Class
 */

package application;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import persistence.ActiveRulesAccess;
import persistence.BeanAccess;
import persistence.CookiesAccess;
import persistence.Manager;
import persistence.RollbackException;
import persistence.StepsAccess;
import persistence.beans.ActiveRuleBean;
import persistence.beans.CookieBean;
import persistence.beans.StepBean;


public class Controller {
	
	private Manager instance;
	
	private BeanAccess<StepBean> stepAccess;
	private BeanAccess<CookieBean> cookieAccess;
	private BeanAccess<ActiveRuleBean> ruleAccess;
	
	private List<StepBean> steps;
	private List<CookieBean> cookies;
	private List<ActiveRuleBean> rules;
	
	private int totalSteps, totalCookies, totalRules;
	
	private ObservableList<String> observableRules;
	private ObservableList<String> observableBreakpoints;
	
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="resume"
    private Button resume; // Value injected by FXMLLoader

    @FXML // fx:id="step_return"
    private Button step_return; // Value injected by FXMLLoader

    @FXML // fx:id="about"
    private MenuItem about; // Value injected by FXMLLoader

    @FXML // fx:id="pause"
    private Button pause; // Value injected by FXMLLoader

    @FXML // fx:id="step_over"
    private Button step_over; // Value injected by FXMLLoader

    @FXML // fx:id="help"
    private Menu help; // Value injected by FXMLLoader

    @FXML // fx:id="file"
    private Menu file; // Value injected by FXMLLoader

    @FXML // fx:id="trace_label"
    private Label trace_label; // Value injected by FXMLLoader
    
    @FXML // fx:id="terms_tree"
    private TreeView<String> terms_tree; // Value injected by FXMLLoader

    @FXML // fx:id="step_into"
    private Button step_into; // Value injected by FXMLLoader

    @FXML // fx:id="options"
    private Menu options; // Value injected by FXMLLoader

    @FXML // fx:id="playback"
    private MenuItem playback; // Value injected by FXMLLoader

    @FXML // fx:id="terminate"
    private Button terminate; // Value injected by FXMLLoader

    @FXML // fx:id="breakpoint_list"
    private ListView<String> breakpoint_list; // Value injected by FXMLLoader

    @FXML // fx:id="rules_list"
    private ListView<String> rules_list; // Value injected by FXMLLoader

    @FXML // fx:id="close"
    private MenuItem close; // Value injected by FXMLLoader

    @FXML // fx:id="open"
    private MenuItem open; // Value injected by FXMLLoader
    
    @FXML // fx:id="bp_menu"
    private MenuButton bp_menu;

    private int lastIndent = 0, currentStep = 0;
    Stack<TreeItem<String>> stepNodes = new Stack<TreeItem<String>>();
    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() throws RollbackException {
        assert resume != null : "fx:id=\"resume\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert step_return != null : "fx:id=\"step_return\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert about != null : "fx:id=\"about\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert pause != null : "fx:id=\"pause\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert step_over != null : "fx:id=\"step_over\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert help != null : "fx:id=\"help\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert file != null : "fx:id=\"file\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert trace_label != null : "fx:id=\"trace_label\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert terms_tree != null : "fx:id=\"terms_tree\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert step_into != null : "fx:id=\"step_into\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert options != null : "fx:id=\"options\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert playback != null : "fx:id=\"playback\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert terminate != null : "fx:id=\"terminate\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert breakpoint_list != null : "fx:id=\"breakpoint_list\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert rules_list != null : "fx:id=\"rules_list\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert close != null : "fx:id=\"close\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert open != null : "fx:id=\"open\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
       
    	String dbpath = "out.db";
    	
    	long start = System.nanoTime();
    	instance = Manager.getInstance();
    	long stop = System.nanoTime();
    	
    	System.out.println("Opened database " + dbpath + " successfully in " + ((stop - start) / 1000000.0) + " ms.");
    	
    	trace_label.setText("Debugging " + dbpath);
    	pause.setDisable(true);
    	
    	start = System.nanoTime();
    	cookieAccess = new CookiesAccess(instance);
    	cookies = cookieAccess.getAll();
    	totalCookies = cookies.size();
    	stop = System.nanoTime();
    	
    	System.out.println("Loaded " + totalCookies + " cookies in " + ((stop - start) / 1000000.0) + " ms.");
    	
    	start = System.nanoTime();
    	ruleAccess = new ActiveRulesAccess(instance);
    	rules = ruleAccess.getAll();
    	totalRules = rules.size();
    	stop = System.nanoTime();
    	
    	System.out.println("Loaded " + totalRules + " active rules in " + ((stop - start) / 1000000.0) + " ms.");
    	
    	start = System.nanoTime();
    	stepAccess = new StepsAccess(instance);
    	steps = stepAccess.getAll();
    	totalSteps = steps.size();
    	stop = System.nanoTime();
    	
    	System.out.println("Loaded " + totalSteps + " steps in " + ((stop - start) / 1000000.0) + " ms.");
    	
    	
    	// Initialize Terms Tree View
    	
    	TreeItem<String> root = new TreeItem<String> ("Terms");
    	terms_tree.setRoot(root);
    	root.setExpanded(true);
    	stepNodes.push(root);
    	System.out.println("Term Tree:\n");
    	
    	//Populate Rules List View
    	
        observableRules =FXCollections.observableArrayList ();
        for (ActiveRuleBean bean : rules) {
    		observableRules.add(bean.getValue());
    	}
        rules_list.setItems((ObservableList<String>) observableRules);
        
        // Populate BreakPoint List View
        observableBreakpoints = FXCollections.observableArrayList();
        breakpoint_list.setItems((ObservableList<String>) observableBreakpoints);
        
        // Generate Context Menu for Rules
        final ContextMenu cMenu = new ContextMenu();
        MenuItem cmItem = new MenuItem("Set Breakpoint");
        cmItem.setOnAction(
        		(event) -> {
        			String breakpoint = rules_list.getSelectionModel().getSelectedItem();
        			observableBreakpoints.add(breakpoint);
        			System.out.println("Breakpoint set on: " + breakpoint);
        		}
        );
        
        cMenu.getItems().add(cmItem);
        rules_list.addEventHandler(MouseEvent.MOUSE_CLICKED, 
        		(event) -> {
        			if (event.getButton() == MouseButton.SECONDARY)
        				cMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
        		});
        
        final ContextMenu bp_cmenu = new ContextMenu();
        MenuItem bp_cmItem = new MenuItem("Remove Breakpoint");
        bp_cmItem.setOnAction((event) ->{
        	String breakpoint = breakpoint_list.getSelectionModel().getSelectedItem();
        	observableBreakpoints.remove(breakpoint);
        	System.out.println("Removed breakpoint: " + breakpoint);
        });
        
        bp_cmenu.getItems().add(bp_cmItem);
        breakpoint_list.addEventHandler(MouseEvent.MOUSE_CLICKED, 
        		(event) -> {
        			if (event.getButton() == MouseButton.SECONDARY)
        				bp_cmenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
        		});
        
        // Generate Breakpoint Menu
        MenuItem addBP = new MenuItem("Set New Breakpoint");
        MenuItem removeAll = new MenuItem("Remove All Breakpoints");
        addBP.setOnAction(
        		(event) -> {
        			TextInputDialog dialog = new TextInputDialog();
        	    	dialog.setTitle("Set New Breakpoint by RegEx");
        	    	dialog.setContentText("Enter your RegEx Rule: ");
        	    	dialog.setHeaderText(null);
        	    	Optional<String> result = dialog.showAndWait();
        	    	
        	    	result.ifPresent((exp) -> {
        	    		try {
        	    			Pattern p = Pattern.compile(exp);
        	    			for (ActiveRuleBean rule : rules) 
        	    				if (p.matcher(rule.getValue()).find() && !breakpoint_list.getItems().contains(rule.getValue()))
        	    					observableBreakpoints.add(rule.getValue());
        	    		} catch (PatternSyntaxException e) {
        	    			Alert alert = new Alert(AlertType.WARNING);
        	    			alert.setTitle("Invalid Regular Expression");
        	    			alert.setHeaderText(null);
        	    			alert.setContentText(e.getMessage());
        	    			alert.showAndWait();
        	    		}
        	    	});
        	    	
        		}
        	);
        
        removeAll.setOnAction(
        		(event) -> {
        			observableBreakpoints.clear();
        			System.out.println("All breakpoints removed");
        		});
        
        bp_menu.getItems().addAll(addBP, removeAll);
        
        step_return.setDisable(true);
        step_into.setDisable(true);
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
    
    @FXML
    void onPause(ActionEvent event){
    	System.out.println("pausing debug...");
    }
    
    @FXML
    void onResume(ActionEvent event){
    	System.out.println("resuming debug...");
    }
    
    @FXML
    void onTerminate(ActionEvent event){
    	System.out.println("terminating debug...");
    }
    
    @FXML
    void onStepInto(ActionEvent event){
    	if (currentStep <= totalSteps) {
    		step();
    		System.out.println("stepping into...");
    	}
    }
    
    @FXML
    void onStepOver(ActionEvent event){
    	if (currentStep <= totalSteps) {
    		if(lastIndent == 0){
        		step();
        	}
    		else{
    			StepBean s = steps.get(currentStep);
        		int currentIndent = lastIndent;
        		while(s.getIndentation() > currentIndent){
        			step();
        			s = steps.get(currentStep);
        		}
        		step();
    		}
    		System.out.println("stepping over...");
    	}
    }

    @FXML
    void onStepReturn(ActionEvent event){
    	if (currentStep <= totalSteps) {
    		if(lastIndent == 0){
    			step();
    		}
    		else{
    			StepBean s = steps.get(currentStep);
    			int currentIndent = lastIndent;
    			while(s.getIndentation() >= currentIndent){
    				step();
    				s = steps.get(currentStep);
    			}
    			step();
    		}
    		System.out.println("returning to step...");
    	}
    }
    
    void step(){
    	StepBean s = steps.get(currentStep);
		System.out.println("Indentation level " + s.getIndentation() + ":\nStep " + s.getStepNum() + ":\n");
		
		
		// TODO pull this out
		String currentRule = rules.get(s.getActiveRuleId()).getValue();
		if (breakpoint_list.getItems().contains(currentRule))
			this.onPause(null);
		
		if(s.getIndentation() > lastIndent){
			// Next indentation level
			lastIndent = s.getIndentation();
			TreeItem<String> newNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ":\nStep " + s.getStepNum() + ":\n" + s.getStartData());
			TreeItem<String> currentNode = stepNodes.pop();
			currentNode.getChildren().add(newNode);
			stepNodes.push(currentNode);
			// New stack node is the first child of this indentation level for this node's parent
			stepNodes.push(newNode);
			System.out.println(newNode.getValue());
			terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			terms_tree.requestFocus();
			terms_tree.getSelectionModel().select(newNode);
			terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
			
			//rules_list.requestFocus();
			rules_list.getSelectionModel().select(s.getActiveRuleId());
			rules_list.getFocusModel().focus(s.getActiveRuleId());
			
		}
		else if(s.getIndentation() < lastIndent){
			// Previous indentation level
			lastIndent = s.getIndentation();
			TreeItem<String> newNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ":\nStep " + s.getStepNum() + ":\n" + s.getStartData());
			stepNodes.pop();
			TreeItem<String> currentNode = stepNodes.pop();
			currentNode.getParent().getChildren().add(newNode);
			//stepNodes.push(currentNode);
			// New stack node is the next child of the indentation level for the previous node's parent
			stepNodes.push(newNode);
			System.out.println(newNode.getValue());
			terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			terms_tree.requestFocus();
			terms_tree.getSelectionModel().select(newNode);
			terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
			
			//rules_list.requestFocus();
			rules_list.getSelectionModel().select(s.getActiveRuleId());
			rules_list.getFocusModel().focus(s.getActiveRuleId());
		}
		else{
			// Same indentation level, new child of previous indentation level
			TreeItem<String> newNode = new TreeItem<String>("Indentation level " + s.getIndentation() + ":\nStep " + s.getStepNum() + ":\n" + s.getStartData());
			TreeItem<String> currentNode = stepNodes.pop();
			currentNode.getParent().getChildren().add(newNode);
			// New stack node is the NEXT child for this indentation level's parent, previous child shouldn't have any new children
			stepNodes.push(newNode);
			System.out.println(newNode.getValue());
			terms_tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			terms_tree.requestFocus();
			terms_tree.getSelectionModel().select(newNode);
			terms_tree.getFocusModel().focus(terms_tree.getSelectionModel().getSelectedIndex());
			
			//rules_list.requestFocus();
			rules_list.getSelectionModel().select(s.getActiveRuleId());
			rules_list.getFocusModel().focus(s.getActiveRuleId());
		}
		
		currentStep++;
		System.out.println("---------------------------------------------------");
		if (currentStep < totalSteps) {
			s = steps.get(currentStep);
			if(s.getIndentation() > lastIndent)
				step_into.setDisable(false);
			else
				step_into.setDisable(true);
			if(lastIndent > 1)
				step_return.setDisable(false);
			else
				step_return.setDisable(true);
		} else {
			step_into.setDisable(true);
			step_return.setDisable(true);
			step_over.setDisable(true);
		}
    }
}
