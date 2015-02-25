package crsxviz.persistence.beans;

import java.io.Serializable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
	@NamedQuery(name = ActiveRules.getAll, query = "SELECT a from ActiveRules a")
})
public class ActiveRules implements Serializable {
	public final static String PREFIX = "crsxviz.persistence.beans.ActiveRules.";
	public final static String getAll = PREFIX + "getAll";
	private IntegerProperty activeRuleId;
	private StringProperty value;
        private BooleanProperty breakpoint;
        private int _activeRuleId;
        private String _value;
        private boolean _breakpoint;
	
	public ActiveRules() { }
	
	@Id
	public int getActiveRuleId() {
		return (activeRuleId == null) ? _activeRuleId : activeRuleId.get();
	}

	public void setActiveRuleId(int activeRuleId) {
            if (this.activeRuleId == null)
                _activeRuleId = activeRuleId;
            else
                this.activeRuleId.set(activeRuleId);
	}
        
	public String getValue() {
		return (value == null) ? _value : value.get();
	}

	public void setValue(String value) {
            if (this.value == null)
                _value = value;
            else
                this.value.set(value);
	}
        
        public boolean isBreakpoint() { 
            return (breakpoint == null) ? _breakpoint : breakpoint.get(); 
        }
        
        public void setBreakpoint(boolean breakpoint) {
            if (this.breakpoint == null)
                _breakpoint = breakpoint;
            else
                this.breakpoint.set(breakpoint);
	}
        
        public IntegerProperty getActiveRuleIdProperty() { 
            if (activeRuleId == null)
                activeRuleId = new SimpleIntegerProperty(this, "activeRuleId");
            return activeRuleId; 
        }
        
        public StringProperty getValueProperty() {
            if (value == null)
                value = new SimpleStringProperty(this, "value");
            return value; 
        }
        
        public BooleanProperty getBreakpointProperty() { 
            if (breakpoint == null)
                breakpoint = new SimpleBooleanProperty(this, "breakpoint");
            return breakpoint; 
        }
}
