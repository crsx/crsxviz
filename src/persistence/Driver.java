package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import persistence.impl.DbProperties;

public class Driver implements IDriver {

	private DbProperties dbInfo;

	public Driver() {
		dbInfo = new DbProperties();
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
