package crsxviz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import persistence.DbProperties;
import persistence.IDriver;
import persistence.Manager;

public class TestManager extends Manager implements IDriver {
	
	private static Manager testInstance;
	private DbProperties dbInfo;
	
	public static TestManager getTestInstance() {
		if (testInstance == null)
			testInstance = new TestManager();
		return (TestManager)testInstance;
	}
	
	protected TestManager() {
		dbInfo = new DbProperties();
	}
	
	public Connection getConnection() {
		Connection con = null;
		try {
			Class.forName(dbInfo.getJdbcDriver());
			con = DriverManager.getConnection(dbInfo.getJdbcTestUrl());
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("SQL Exception: " + e.getMessage());
		}
		return con;
	}
}
