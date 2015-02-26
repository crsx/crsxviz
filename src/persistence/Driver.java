package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Driver implements IDriver {

	private DbProperties dbInfo;

	public Driver(String dbpath, String driver) {
		dbInfo = new DbProperties(dbpath, driver);
	}

	public Connection getConnection() throws RollbackException {
		Connection con = null;
		try {
			Class.forName(dbInfo.getJdbcDriver());
			con = DriverManager.getConnection(dbInfo.getJdbcUrl());
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Exception caught: " + e.getMessage());
		}
		return con;
	}
}
