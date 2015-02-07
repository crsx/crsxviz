/**
 * Sample Skeleton for 'CRSXVIZ.fxml' Controller Class
 */

package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Stack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import persistence.ActiveRulesAccess;
import persistence.BeanAccess;
import persistence.CookiesAccess;
import persistence.Manager;
import persistence.RollbackException;
import persistence.StepsAccess;
import persistence.beans.ActiveRuleBean;
import persistence.beans.CookieBean;
import persistence.beans.StepBean;

public class Controller extends AnchorPane {
	
	public Controller() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("CRSXVIZ.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

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
    private ListView<?> breakpoint_list; // Value injected by FXMLLoader

    @FXML // fx:id="rules_list"
    private ListView<String> rules_list; // Value injected by FXMLLoader

    @FXML // fx:id="close"
    private MenuItem close; // Value injected by FXMLLoader

    @FXML // fx:id="open"
    private MenuItem open; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() throws RollbackException {
        assert resume != null : "fx:id=\"resume\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert step_return != null : "fx:id=\"step_return\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert about != null : "fx:id=\"about\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert pause != null : "fx:id=\"pause\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert step_over != null : "fx:id=\"step_over\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert help != null : "fx:id=\"help\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert file != null : "fx:id=\"file\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert terms_tree != null : "fx:id=\"terms_tree\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert step_into != null : "fx:id=\"step_into\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert options != null : "fx:id=\"options\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert playback != null : "fx:id=\"playback\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert terminate != null : "fx:id=\"terminate\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert breakpoint_list != null : "fx:id=\"breakpoint_list\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert rules_list != null : "fx:id=\"rules_list\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert close != null : "fx:id=\"close\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        assert open != null : "fx:id=\"open\" was not injected: check your FXML file 'CRSXVIZ.fxml'.";
        
        Manager instance = Manager.getInstance();
        BeanAccess<StepBean> steps = null;
        BeanAccess<CookieBean> cookies = null; 
        BeanAccess<ActiveRuleBean> activeRules = null;
        
        long start, stop;
    	
    	start = System.nanoTime();
    	cookies = new CookiesAccess(instance);
    	stop = System.nanoTime();
    	
    	System.out.println("Loaded " + cookies.getCount() + " cookies in " + ((stop - start) / 1000000.0) + " ms.");
    	
    	start = System.nanoTime();
    	activeRules = new ActiveRulesAccess(instance);
    	stop = System.nanoTime();
    	
    	System.out.println("Loaded " + activeRules.getCount() + " active rules in " + ((stop - start) / 1000000.0) + " ms.");
    	
    	start = System.nanoTime();
    	steps = new StepsAccess(instance);
    	stop = System.nanoTime();
    	
    	System.out.println("Loaded StepManager with " + steps.getCount() + " steps in table in " + ((stop - start) / 1000000.0) + " ms.");
    	
    	System.out.println("--Cookies------------------------------------------");
    	for (CookieBean bean : cookies.getAll())
    		System.out.printf("| %4d | %40s |\n", bean.getCookieId(), bean.getValue());
    	System.out.println("---------------------------------------------------");
    	
    	System.out.println("--ActiveRules--------------------------------------");
    	for (ActiveRuleBean bean : activeRules.getAll())
    		System.out.printf("| %4d | %40s |\n", bean.getActiveRuleId(), bean.getValue());
    	System.out.println("---------------------------------------------------");
    	
    	System.out.println("--Steps--------------------------------------------");
    	for (StepBean bean : steps.getAll()) {
    		System.out.printf("| %4d | %4d | %4d | %4d | %4d | %4d | %4d | Cookies: ",
    				bean.getStepNum(),
    				bean.getIndentation(),
    				bean.getActiveRuleId(),
    				bean.getStartAllocs(),
    				bean.getStartFrees(),
    				bean.getCompleteAllocs(),
    				bean.getCompleteFrees()
    			);
    		
    		for (int j : bean.getCookies()) {
    			System.out.print(cookies.get(j).getValue() + ", ");
    		}
    		System.out.println();
    	}
    	System.out.println("---------------------------------------------------");
    	
    	// Populate Terms Tree View
    	
    	TreeItem<String> root = new TreeItem<String> ("Terms");
    	terms_tree.setRoot(root);
    	Stack<TreeItem<String>> stepNodes = new Stack<TreeItem<String>>();
    	stepNodes.push(root);
    	int lastIndent = 0;
    	System.out.println("Term Tree:\n");
    	
    	for (StepBean bean : steps.getAll()) {
    		System.out.println("Indentation level " + bean.getIndentation() + ":\n");
    		
    		
    		if(bean.getIndentation() > lastIndent){
    			// Next indentation level
    			lastIndent = bean.getIndentation();
    			TreeItem<String> newNode = new TreeItem<String>("Indentation level " + bean.getIndentation() + ":\n" + bean.getStartData());
    			TreeItem<String> currentNode = stepNodes.pop();
    			currentNode.getChildren().add(newNode);
    			stepNodes.push(currentNode);
    			// New stack node is the first child of this indentation level for this node's parent
    			stepNodes.push(newNode);
    			System.out.println(newNode.getValue());
    			
    		}
    		else if(bean.getIndentation() < lastIndent){
    			// Previous indentation level
    			lastIndent = bean.getIndentation();
    			TreeItem<String> newNode = new TreeItem<String>("Indentation level " + bean.getIndentation() + ":\n" + bean.getStartData());
    			stepNodes.pop();
    			TreeItem<String> currentNode = stepNodes.pop();
    			currentNode.getParent().getChildren().add(newNode);
    			//stepNodes.push(currentNode);
    			// New stack node is the next child of the indentation level for the previous node's parent
    			stepNodes.push(newNode);
    			System.out.println(newNode.getValue());
    		}
    		else{
    			// Same indentation level, new child of previous indentation level
    			TreeItem<String> newNode = new TreeItem<String>("Indentation level " + bean.getIndentation() + ":\n" + bean.getStartData());
    			TreeItem<String> currentNode = stepNodes.pop();
    			currentNode.getParent().getChildren().add(newNode);
    			// New stack node is the NEXT child for this indentation level's parent, previous child shouldn't have any new children
    			stepNodes.push(newNode);
    			System.out.println(newNode.getValue());
    		}
    		System.out.println("---------------------------------------------------");
    	}
    	
    	//Populate Rules List View
    	
        ObservableList<String> rules = FXCollections.observableArrayList ();
        for (ActiveRuleBean bean : activeRules.getAll()) 
        	rules.add(bean.getValue());
        rules_list.setItems((ObservableList<String>) rules);
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
    	System.out.println("stepping into...");
    }
    
    @FXML
    void onStepOver(ActionEvent event){
    	System.out.println("stepping over...");
    }
    
    @FXML
    void onStepReturn(ActionEvent event){
    	System.out.println("returning to step...");
    }
}
