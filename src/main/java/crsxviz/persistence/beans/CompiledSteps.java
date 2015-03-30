package crsxviz.persistence.beans;

import java.io.Serializable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
	@NamedQuery(name = CompiledSteps.getAll, query = "SELECT a from CompiledSteps a")
})
public class CompiledSteps implements Serializable {
	public final static String PREFIX = "crsxviz.persistence.beans.CompiledSteps.";
	public final static String getAll = PREFIX + "getAll";
	
	private IntegerProperty id;
	private StringProperty left;
	private StringProperty center;
	private StringProperty right;
    private Integer _id;
    private String _left;
    private String _center;
    private String _right;
    
	public CompiledSteps() { }
	
	public void setid(Integer id) {
        if (this.id == null)
            _id = id;
        else
            this.id.set(id);
	}
	
	@Id   
	public Integer getid() {
		return (id == null) ? _id : id.get();
	}
	
	public void setleft(String left) {
        if (this.left == null)
            _left = left;
        else
            this.left.set(left);
	}
	  
	public String getleft() {
		return (left == null) ? _left : left.get();
	}
	
	public void setright(String right) {
        if (this.right == null)
            _right = right;
        else
            this.right.set(right);
	}
	  
	public String getright() {
		return (right == null) ? _right : right.get();
	}
	
	public void setcenter(String center) {
        if (this.center == null)
            _center = center;
        else
            this.center.set(center);
	}
	  
	public String getcenter() {
		return (center == null) ? _center : center.get();
	}
	
    public IntegerProperty getidProperty() { 
        if (id == null)
            id = new SimpleIntegerProperty(this, "id");
        return id; 
    }
    
    public StringProperty getleftProperty() {
        if (left == null)
            left = new SimpleStringProperty(this, "left");
        return left; 
    }
    
    public StringProperty getrightProperty() {
        if (right == null)
            right = new SimpleStringProperty(this, "right");
        return right; 
    }
    
    public StringProperty getcenterProperty() {
        if (center == null)
            center = new SimpleStringProperty(this, "center");
        return center; 
    }
	
}
