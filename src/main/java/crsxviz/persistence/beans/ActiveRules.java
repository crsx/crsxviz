package crsxviz.persistence.beans;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ActiveRules implements Serializable {

    private IntegerProperty activeRuleId;
    private StringProperty value;
    private StringProperty env;
    private StringProperty args;
    private StringProperty resultType;
    private StringProperty usedInTrace;
    private BooleanProperty breakpoint;
    private int _activeRuleId;
    private String _value;
    private String _env;
    private String _args;
    private String _resultType;
    private String _usedInTrace;
    private boolean _breakpoint;

    public ActiveRules() {
    }

    public ActiveRules(int activeRuleId, String value) {
        if (this.activeRuleId == null) {
            _activeRuleId = activeRuleId;
        } else {
            this.activeRuleId.set(activeRuleId);
        }
        
        if (this.value == null) {
            _value = value;
        } else {
            this.value.set(value);
        }
    }
    

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

    public String getEnv() {
        return (env == null) ? _env : env.get();
    }

    public void setEnv(String env) {
        if (this.env == null) {
            _env = env;
        } else {
            this.env.set(env);
        }
    }

    public String getArgs() {
        return (args == null) ? _args : args.get();
    }

    public void setArgs(String args) {
        if (this.args == null) {
            _args = args;
        } else {
            this.args.set(args);
        }
    }

    public String getResultType() {
        return (resultType == null) ? _resultType : resultType.get();
    }

    public void setResultType(String resultType) {
        if (this.resultType == null) {
            _resultType = resultType;
        } else {
            this.resultType.set(resultType);
        }
    }

    public String getUsedInTrace() {
        return (usedInTrace == null) ? _usedInTrace : usedInTrace.get();
    }

    public void setUsedInTrace(String usedInTrace) {
        if (this.usedInTrace == null) {
            _usedInTrace = usedInTrace;
        } else {
            this.usedInTrace.set(usedInTrace);
        }
    }

    public boolean isBreakpoint() {
        return (breakpoint == null) ? _breakpoint : breakpoint.get();
    }

    public void setBreakpoint(boolean breakpoint) {
        if (this.breakpoint == null) {
            _breakpoint = breakpoint;
        } else {
            this.breakpoint.set(breakpoint);
        }
    }

    public IntegerProperty getActiveRuleIdProperty() {
        if (activeRuleId == null) {
            activeRuleId = new SimpleIntegerProperty(this, "activeRuleId");
        }
        return activeRuleId;
    }

    public StringProperty getValueProperty() {
        if (value == null) {
            value = new SimpleStringProperty(this, "value");
        }
        return value;
    }

    public StringProperty getEnvProperty() {
        if (env == null) {
            env = new SimpleStringProperty(this, "env");
        }
        return env;
    }

    public StringProperty getArgsProperty() {
        if (args == null) {
            args = new SimpleStringProperty(this, "args");
        }
        return args;
    }

    public StringProperty getResultTypeProperty() {
        if (resultType == null) {
            resultType = new SimpleStringProperty(this, "resultType");
        }
        return resultType;
    }

    public StringProperty getUsedInTraceProperty() {
        if (usedInTrace == null) {
            usedInTrace = new SimpleStringProperty(this, "usedInTrace");
        }
        return usedInTrace;
    }

    public BooleanProperty getBreakpointProperty() {
        if (breakpoint == null) {
            breakpoint = new SimpleBooleanProperty(this, "breakpoint");
        }
        return breakpoint;
    }
    
    public static List<RuleDetails> getRuleDetails(String ruleName, String url) {
        String sql = "SELECT * FROM ActiveRules as R JOIN DispatchedRules as D ON R.ActiveRuleID=D.ActiveRuleID WHERE R.Value = \"" + ruleName + "\";";
        Statement s;
        Connection fastConn = null;
        try {
            List<RuleDetails> l = new LinkedList<>();
            fastConn = DriverManager.getConnection(url);
            s = fastConn.createStatement();
            s.execute(sql);
            ResultSet rs = s.getResultSet();
            if (!rs.next()) {
                System.err.println("No results returned");
                return l;
            }
            do {
                RuleDetails d = new RuleDetails(rs);
                l.add(d);
            } while (rs.next());
            return l;
        } catch (SQLException e) {
            System.err.println("Error requesting rule details");
            e.printStackTrace();
        } finally {
            try {
                if (fastConn != null) 
                    fastConn.close();
            } catch (SQLException ex) {
                Logger.getLogger(ActiveRules.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static List<ActiveRules> loadAllRules(String url) {
        List<ActiveRules> l = new ArrayList<>();
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(url);
            Statement stmt = c.createStatement();
            stmt.execute("SELECT * FROM ActiveRules;");
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                l.add(loadRule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Unable to populate list");
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ActiveRules.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (c != null) 
                    c.close();
            } catch (SQLException ex) {
                Logger.getLogger(ActiveRules.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return l;
    }

    private static ActiveRules loadRule(ResultSet r) {
        ActiveRules c = new ActiveRules();
        try {
            c.setActiveRuleId(r.getInt("ActiveRuleID"));
            c.setArgs(r.getString("Args"));
            c.setEnv(r.getString("Env"));
            c.setResultType(r.getString("ResultType"));
            c.setUsedInTrace(r.getString("UsedInTrace"));
            c.setValue(r.getString("Value"));
        } catch (SQLException e) {
            System.err.println("Error loading ActiveRule from ResultSet");
            e.printStackTrace();
            return null;
        }
        return c;
    }
}
