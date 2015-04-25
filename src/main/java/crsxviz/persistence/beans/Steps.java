package crsxviz.persistence.beans;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Steps implements Serializable {

    private int stepNum;
    private int indentation;
    private int activeRuleId;
    private int startAllocs;
    private int startFrees;
    private int completeAllocs;
    private int completeFrees;
    private String startData;
    private String completeData;
    private List<Integer> cookies;
    private boolean startDataDisplayed, completeDataDisplayed;

    public boolean isStartDataDisplayed() {
        return startDataDisplayed;
    }

    public void setStartDataDisplayed(boolean startDataRead) {
        this.startDataDisplayed = startDataRead;
    }

    public boolean isCompleteDataDisplayed() {
        return completeDataDisplayed;
    }

    public void setCompleteDataDisplayed(boolean completeDataRead) {
        this.completeDataDisplayed = completeDataRead;
    }

    public Steps() {
    }

    public int getStepNum() {
        return stepNum;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    public int getIndentation() {
        return indentation;
    }

    public void setIndentation(int indentation) {
        this.indentation = indentation;
    }

    public int getActiveRuleId() {
        return activeRuleId;
    }

    public void setActiveRuleId(int activeRuleId) {
        this.activeRuleId = activeRuleId;
    }

    public int getStartAllocs() {
        return startAllocs;
    }

    public void setStartAllocs(int startAllocs) {
        this.startAllocs = startAllocs;
    }

    public int getStartFrees() {
        return startFrees;
    }

    public void setStartFrees(int startFrees) {
        this.startFrees = startFrees;
    }

    public int getCompleteAllocs() {
        return completeAllocs;
    }

    public void setCompleteAllocs(int completeAllocs) {
        this.completeAllocs = completeAllocs;
    }

    public int getCompleteFrees() {
        return completeFrees;
    }

    public void setCompleteFrees(int completeFrees) {
        this.completeFrees = completeFrees;
    }

    public String getStartData() {
        return startData;
    }

    public void setStartData(String startData) {
        this.startData = startData.replaceAll("\\s", "");
    }

    public String getCompleteData() {
        return completeData;
    }

    public void setCompleteData(String completeData) {
        this.completeData = completeData.replaceAll("\\s", "");
    }

    public List<Integer> getCookies() {
        return this.cookies;
    }

    public void setCookies(byte[] array) {
        if (array == null) {
            return;
        }
        cookies = new ArrayList<>();

        IntBuffer buf = ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        int[] tmp = new int[buf.remaining()];
        buf.get(tmp);
        for (int i : tmp) {
            cookies.add(i);
        }
    }

    public static List<Steps> loadAllSteps(String url) {
        List<Steps> l = new ArrayList<>();
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(url);
            Statement stmt = c.createStatement();
            stmt.execute("SELECT * FROM Steps");
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                l.add(loadStep(rs));
            }
        } catch (SQLException e) {
            System.err.println("Unable to populate list");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Steps.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (c != null) 
                    c.close();
            } catch (SQLException ex) {
                Logger.getLogger(Steps.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return l;
    }

    private static Steps loadStep(ResultSet r) {
        Steps c = new Steps();
        try {
            c.setActiveRuleId(r.getInt("ActiveRuleID"));
            c.setCompleteAllocs(r.getInt("CompleteAllocs"));
            c.setCompleteData(r.getString("CompleteData"));
            c.setCompleteFrees(r.getInt("CompleteFrees"));
            c.setCookies(r.getBytes("Cookies"));
            c.setIndentation(r.getInt("Indentation"));
            c.setStartAllocs(r.getInt("StartAllocs"));
            c.setStartData(r.getString("StartData"));
            c.setStartFrees(r.getInt("StartFrees"));
            c.setStepNum(r.getInt("StepNum"));
        } catch (SQLException e) {
            System.err.println("Error loading Step from ResultSet");
            e.printStackTrace();
            return null;
        }
        return c;
    }
}
