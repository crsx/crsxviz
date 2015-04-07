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

public class DispatchedRules implements Serializable {

    public DispatchedRules() {
    }

    private IntegerProperty activeRuleId;
    private int _activeRuleId;
    private IntegerProperty srcRuleOffset;
    private int _srcRuleOffset;

    public int getActiveRuleId() {
        return (activeRuleId == null) ? _activeRuleId : activeRuleId.get();
    }

    public void setActiveRuleId(int activeRuleId) {
        if (this.activeRuleId == null) {
            _activeRuleId = activeRuleId;
        } else {
            this.activeRuleId.set(activeRuleId);
        }
    }

    public IntegerProperty getActiveRuleIdProperty() {
        if (activeRuleId == null) {
            activeRuleId = new SimpleIntegerProperty(this, "activeRuleId");
        }
        return activeRuleId;
    }

    public int getSrcRuleOffset() {
        return (srcRuleOffset == null) ? _srcRuleOffset : srcRuleOffset.get();
    }

    public void setSrcRuleOffset(int srcRuleOffset) {
        if (this.srcRuleOffset == null) {
            _srcRuleOffset = srcRuleOffset;
        } else {
            this.srcRuleOffset.set(srcRuleOffset);
        }
    }

    public IntegerProperty getSrcRuleOffsetProperty() {
        if (srcRuleOffset == null) {
            srcRuleOffset = new SimpleIntegerProperty(this, "srcRuleOffset");
        }
        return srcRuleOffset;
    }

    
        public static List<DispatchedRules> loadAllDispatchtedRules(String url) {
        List<DispatchedRules> l = new ArrayList<>();
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(url);
            Statement stmt = c.createStatement();
            stmt.execute("SELECT * FROM DispatchedRules;");
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                l.add(loadDispatchedRule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Unable to populate list");
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DispatchedRules.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (c != null) 
                    c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DispatchedRules.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return l;
    }

    private static DispatchedRules loadDispatchedRule(ResultSet r) {
        DispatchedRules c = new DispatchedRules();
        try {
            c.setActiveRuleId(r.getInt("ActiveRuleID"));
            c.setSrcRuleOffset(r.getInt("SrcRuleOffset"));
        } catch (SQLException e) {
            System.err.println("Error loading Cookie from ResultSet");
            return null;
        }
        return c;
    }
}
