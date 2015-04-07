package crsxviz.persistence.beans;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cookies implements Serializable {

    private IntegerProperty cookieId;
    private StringProperty value;
    private int _cookieId;
    private String _value;

    public Cookies() {
    }

    public int getCookieId() {
        return (cookieId == null) ? _cookieId : cookieId.get();
    }

    public void setCookieId(int cookieId) {
        if (this.cookieId == null) {
            _cookieId = cookieId;
        } else {
            this.cookieId.set(cookieId);
        }
    }

    public String getValue() {
        return (value == null) ? _value : value.get();
    }

    public void setValue(String value) {
        if (this.value == null) {
            _value = value;
        } else {
            this.value.set(value);
        }
    }

    public IntegerProperty getCookieIdProperty() {
        if (cookieId == null) {
            cookieId = new SimpleIntegerProperty(this, "cookieId");
        }
        return cookieId;
    }

    public StringProperty getValueProperty() {
        if (value == null) {
            value = new SimpleStringProperty(this, "value");
        }
        return value;
    }

    public static List<Cookies> loadAllCookies(String url) {
        List<Cookies> l = new ArrayList<>();
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(url);
            Statement stmt = c.createStatement();
            stmt.execute("SELECT * FROM Cookies;");
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                l.add(loadCookie(rs));
            }
        } catch (SQLException e) {
            System.err.println("Unable to populate list");
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Cookies.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (c != null) 
                    c.close();
            } catch (SQLException ex) {
                Logger.getLogger(Cookies.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return l;
    }

    private static Cookies loadCookie(ResultSet r) {
        Cookies c = new Cookies();
        try {
            c.setCookieId(r.getInt("CookieID"));
            c.setValue(r.getString("Value"));
        } catch (SQLException e) {
            System.err.println("Error loading Cookie from ResultSet");
            return null;
        }
        return c;
    }
}
