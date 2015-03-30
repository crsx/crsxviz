package crsxviz.persistence.beans;

import java.io.Serializable;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
	@NamedQuery(name = DispatchedRules.getAll, query = "SELECT a from DispatchedRules a")
})
public class DispatchedRules  implements Serializable {
	public final static String PREFIX = "crsxviz.persistence.beans.DispatchedRules.";
	public final static String getAll = PREFIX + "getAll";
	
	public DispatchedRules() { }
	
	private IntegerProperty activeRuleId;
	private int _activeRuleId;
	
	public int getActiveRuleId() {
		return (activeRuleId == null) ? _activeRuleId : activeRuleId.get();
	}

	public void setActiveRuleId(int activeRuleId) {
        if (this.activeRuleId == null)
            _activeRuleId = activeRuleId;
        else
            this.activeRuleId.set(activeRuleId);
	}
	  
    public IntegerProperty getActiveRuleIdProperty() { 
        if (activeRuleId == null)
            activeRuleId = new SimpleIntegerProperty(this, "activeRuleId");
        return activeRuleId; 
    }
    
	private IntegerProperty srcRuleOffset;
	private int _srcRuleOffset;
	
	public int getSrcRuleOffset() {
		return (srcRuleOffset == null) ? _srcRuleOffset : srcRuleOffset.get();
	}

	public void setSrcRuleOffset(int srcRuleOffset) {
        if (this.srcRuleOffset == null)
            _srcRuleOffset = srcRuleOffset;
        else
            this.srcRuleOffset.set(srcRuleOffset);
	}
	  
    public IntegerProperty getSrcRuleOffsetProperty() { 
        if (srcRuleOffset == null)
            srcRuleOffset = new SimpleIntegerProperty(this, "srcRuleOffset");
        return srcRuleOffset; 
    }
    
    
    
}
