package crsxviz.persistence.beans;

import java.sql.Statement;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedNativeQueries({
	@NamedNativeQuery(name = CompiledSteps.getAll, query = "SELECT * from CompiledSteps;")
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
    
    public static CompiledSteps loadStep(Connection c, Long stepNum) {
    	try {
			Statement stmt = c.createStatement();
			stmt.execute("SELECT * FROM CompiledSteps WHERE id=" + stepNum + ";");
			ResultSet rs = stmt.getResultSet();
			return loadStep(rs);
		} catch (SQLException e) {
			System.err.println("Unable to request step " + stepNum);
			e.printStackTrace();
			return null;
		}
    }
    
    public static List<CompiledSteps> loadAll(Connection c) {
    	int count = 0;
    	try {
    		Statement stmt = c.createStatement();
			stmt.execute("SELECT COUNT(id) FROM CompiledSteps;");
			ResultSet rs = stmt.getResultSet();
			if(rs.wasNull() || !rs.first()) 
				return null;
			count = rs.getInt(1);
    	} catch (SQLException e) {
    		System.err.println("Unable to get row count for CompiledSteps");
    		e.printStackTrace();
    		return null;
    	}
    	List<CompiledSteps> l = new ArrayList<CompiledSteps>(count);
    	try {
			Statement stmt = c.createStatement();
			stmt.execute("SELECT * FROM CompiledSteps;");
			ResultSet rs = stmt.getResultSet();
			if(rs.wasNull() || !rs.first()) 
				return null;
			for (int i = 0; i < count; i++) {
				l.add(i, loadStep(rs));
				rs.next();
			}
		} catch (SQLException e) {
			System.err.println("Unable to populate list");
			e.printStackTrace();
			return null;
		}
    	return l;
    }
    
    private static CompiledSteps loadStep(ResultSet r) {
    	CompiledSteps c = new CompiledSteps();
    	try {
			c.setid(r.getInt(1));
	    	c.setleft(r.getString(2));
	    	c.setcenter(r.getString(3));
	    	c.setright(r.getString(4));
    	} catch (SQLException e) {
			System.err.println("Error loading CompiledStep from ResultSet");
			e.printStackTrace();
			return null;
		}
    	return c;
    }
    
	public CompiledSteps() { }
	
	public void setid(Integer id) {
        if (this.id == null)
            _id = id;
        else
            this.id.set(id);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(getleft());
		s.append(getcenter());
		s.append(getright());
		return s.toString();
	};
	
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
