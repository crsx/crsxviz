package persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public abstract class BeanAccess<T> {
	private Manager manager;
	
	protected abstract String getCountStatement();
	protected abstract String getAllStatement();
	protected abstract String getStatement(int id);
	protected abstract List<T> buildBean(ResultSet rs) throws SQLException;
	protected abstract T buildSingle(ResultSet rs) throws SQLException;
	
	public BeanAccess(Manager manager) {
		if (manager == null) 
			throw new NullPointerException("manager cannot be null");
		
		this.manager = manager;
	}
	
	public List<T> getAll() throws RollbackException {
		Connection c = null;
		try {
			c = manager.getConnection();
			Statement smt = c.createStatement();
			smt.closeOnCompletion();
			
			ResultSet rs = smt.executeQuery(getAllStatement());
			return buildBean(rs);
		} catch (SQLException e) {
			throw new RollbackException("Exception: " + e.getMessage());
		} finally {
			closeConnection(c);
		}
	}
	
	public T get(int id) throws RollbackException {
		Connection c = null;
		try {
			c = manager.getConnection();
			Statement smt = c.createStatement();
			smt.closeOnCompletion();
			
			ResultSet rs = smt.executeQuery(getStatement(id));
			return (rs.next()) ? buildSingle(rs) : null ;
		} catch (SQLException e) {
			throw new RollbackException("Exception: " + e.getMessage());
		} finally {
			closeConnection(c);
		}
	}
	
	public int getCount() throws RollbackException {
		Connection c = null;
		try {
			c = manager.getConnection();
			Statement smt = c.createStatement();
			smt.closeOnCompletion();
			
			ResultSet rs = smt.executeQuery(getCountStatement());
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			throw new RollbackException("Exception: " + e.getMessage());
		} finally {
			closeConnection(c);
		}
		return 0;
	}
	
	private static void closeConnection(Connection c) throws RollbackException {
		try {
			if (c != null) c.close();
		} catch (SQLException e) {
			throw new RollbackException(e);
		}
	}
}
