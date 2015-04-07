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

    @Id
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
            c.setBreakpoint(r.getBoolean("Breakpoint"));
            c.setEnv(r.getString("Env"));
            c.setResultType(r.getString("ResultType"));
            c.setUsedInTrace(r.getString("UsedInTrace"));
            c.setValue(r.getString("Value"));
        } catch (SQLException e) {
            System.err.println("Error loading ActiveRule from ResultSet");
            return null;
        }
        return c;
    }
}
