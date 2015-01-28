package persistence;

import java.sql.Connection;

import persistence.impl.TranImpl;

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
	
	public Connection getTransactionConnection() throws RollbackException {
		// If we're in a transaction, use the transaction's connection
		if (!TranImpl.isActive()) {
			throw new RollbackException("Must be in a transaction");
		}

		return TranImpl.join(this);
	}
	
	public boolean getLowerCaseColumnNames() {
		// Change implementation if use of postgres database is required
		return false;
	}
}
