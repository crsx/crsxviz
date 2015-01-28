package crsxviz;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

public class TestDbUtils {
	
	private static Connection jdbcConnection = null;

	@SuppressWarnings("deprecation")
	public static void setUpDb() throws Exception {
		Class.forName("org.sqlite.JDBC");
		jdbcConnection = DriverManager.getConnection("jdbc:sqlite:test.db");
		
		IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
		IDataSet data = new FlatXmlDataSet(new File("dataset.xml"));
		
		try {
			DatabaseOperation.CLEAN_INSERT.execute(connection, data);
		} finally {
			connection.close();
		}
	}
	
	public static void removeTable(Connection con, String table) throws Exception {
		try {
			Statement s = con.createStatement();
			try {
				s.execute("DROP TABLE " + table);
			} finally {
				s.close();
			}
		} finally {
			con.close();
		}
	}
	
}
