package persistence;

import java.sql.Connection;

public class Manager {

	private static Manager instance = null;
	private IDriver driver = null;
	
	public static void globalInit(String dbpath, String driver) {
		instance = new Manager(driver, dbpath);
	}
	
	public static Manager getInstance() {
		if (instance == null)
			throw new IllegalAccessError("Database settings must be setup first. Call globalInit() first!");
		return instance;
	}
	
	protected Manager(String dbpath, String driver) {
		this.driver = new Driver(dbpath, driver);
	}
	
	public Connection getConnection() throws RollbackException {
		return driver.getConnection();
	}
}
