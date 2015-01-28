package persistence;

import java.sql.Connection;

public interface IDriver {
	public Connection getConnection() throws RollbackException;
}