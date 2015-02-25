package crsxviz.persistence.beans;

import java.io.Serializable;
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
	@NamedQuery(name = Cookies.getAll, query = "SELECT a FROM Cookies a")
})
public class Cookies implements Serializable {
	public final static String PREFIX = "crsxviz.persistence.beans.Cookies.";
	public final static String getAll = PREFIX + "getAll";
	private IntegerProperty cookieId;
	private StringProperty value;
        private int _cookieId;
        private String _value;
	
	public Cookies() { }

	@Id
	public int getCookieId() {
		return (cookieId == null) ? _cookieId : cookieId.get();
	}

	public void setCookieId(int cookieId) {
            if (this.cookieId == null)
                _cookieId = cookieId;
            else
                this.cookieId.set(cookieId);
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
        
        public IntegerProperty getCookieIdProperty() { 
            if (cookieId == null)
                cookieId = new SimpleIntegerProperty(this, "cookieId");
            return cookieId; 
        }
        public StringProperty getValueProperty() { 
            if (value == null)
                value = new SimpleStringProperty(this, "value");
            return value; 
        }
	
}
