package persistence;

import java.sql.Connection;

public class Manager {

	private static Manager instance = null;
	private IDriver driver = null;
	
	public static Manager getInstance() {
		instance = new Manager();
		return instance;
	}
	
	protected Manager() {
		this.driver = new Driver();
	}
	
	public Connection getConnection() throws RollbackException {
		return driver.getConnection();
	}
}
