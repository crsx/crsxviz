package contentmanager;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.management.RuntimeErrorException;

import contentmanager.beans.StepBean;

public class StepManager {
	private static StepManager _singleton;
	private static Integer stepCount;
	private Connection conn;

	//TODO: Add LRU cached steps
	
	public static void Initialize(Connection c) throws IllegalAccessError {
		if (_singleton != null)
			throw new IllegalAccessError("StepManager already initialized.");
		if (c == null)
			throw new IllegalArgumentException("Connection cannot be null");
		
		_singleton = new StepManager(c);
	}
	
	protected static StepManager instance() throws IllegalAccessError {
		if (_singleton == null)
			throw new IllegalAccessError("StepManager must be initialized first.");
		
		return _singleton;
	}
	
	protected StepManager(Connection c) {
		conn = c;
		loadStepCount();
	}
	
	protected void loadStepCount() {
		try {
			Statement s = conn.createStatement();
			s.closeOnCompletion();
			if (!s.execute("SELECT Count(*) FROM `Steps`;")) {
				System.out.println("Error executing COUNT");
				System.exit(-1);
			} else {
				ResultSet rs = s.getResultSet();
				if (!rs.next()) {
					throw new RuntimeException("Error! 0 rows returned from COUNT `Steps`");
				}
				stepCount = rs.getInt(1);
				rs.close();
			}
		} catch (SQLException e) {
			System.err.println("Error getting row count from `Steps`");
			e.printStackTrace();
			throw new RuntimeException("Error!" + e.getMessage());
		}
	}
	
	public static StepBean getStep(int id) {
		if (id == 0)
			throw new IllegalArgumentException("Steps are 1 indexed. Add 1 to all ID requests.");
		
		return StepManager.instance()._getStep(id);
	}
	
	private StepBean _getStep(int id) {
		StepBean bean = null;
		try {
			Statement s = conn.createStatement();
			if (!s.execute("SELECT * FROM `Steps` where StepNum=" + id +";")) {
				throw new RuntimeException("Error executing SELECT * FROM `Steps` where StepNum=" + id +";");
			} else {
				ResultSet rs = s.getResultSet();
				rs.next();
				if (rs.isFirst()) {
					bean = new StepBean(rs);
					if (rs.next()) {
						System.err.println("WARNING: More than 1 row returned for SELECT step");
					}
				} else {
					throw new RuntimeException("Error SELECT * FROM `Steps` where StepNum=" + id +"; returned 0 rows.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bean;
	}

	public static Integer numSteps() {
		return StepManager.instance().stepCount;
	}
	
}
