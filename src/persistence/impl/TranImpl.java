package persistence.impl;


import java.sql.Connection;
import java.sql.SQLException;

import persistence.DaoException;
import persistence.Manager;
import persistence.RollbackException;


public class TranImpl {
    private static ThreadLocal<TranImpl> myTran = new ThreadLocal<TranImpl>();
    
    private Connection connection = null;
    private Manager manager = null;

    private TranImpl() {
        /* Private constructor forces use of static factory (TranImpl.begin()) */
    }
    
    public static void begin() throws RollbackException {
        TranImpl t = myTran.get();
        if (t != null) rollbackAndThrow("Cannot begin twice without commit or rollback (i.e., you were already in a transaction)!");
        myTran.set(new TranImpl());
    }

    public static void commit() throws RollbackException {
        TranImpl t = myTran.get();
        if (t == null) rollbackAndThrow("Not in a transaction");
        t.executeCommit();
    }
    

    public static boolean isActive() {
        return myTran.get() != null;
    }

    public static void rollback() {
        TranImpl t = myTran.get();
        if (t == null) throw new AssertionError("Not in a transaction");
        t.executeRollback();
    }
    
    static void rollbackAndThrow(String message) throws RollbackException {
        rollbackAndThrow(new RollbackException(message));
    }

    static void rollbackAndThrow(Exception e) throws RollbackException {
        TranImpl t = myTran.get();
        if (t != null) t.executeRollback();
        if (e instanceof RollbackException) throw (RollbackException) e;
        throw new RollbackException(e);
    }

    static void rollbackAndThrow(String message, Exception e) throws RollbackException {
        TranImpl t = myTran.get();
        if (t != null) t.executeRollback();
        throw new RollbackException(message, e);
    }
    
    static void rollbackAndThrow(Connection con, Exception e) throws RollbackException {
        if (isActive()) {
        	rollbackAndThrow(e);
        }

        try {
        	if (con != null && !con.getAutoCommit()) {
        		con.rollback();
        	} 
        } catch (SQLException e2) {
        	e2.printStackTrace();
        }
        
        try {
        	if (con != null) con.close();
        } catch (SQLException e2) {
        	e2.printStackTrace();
        }
        
        rollbackAndThrow(e);
    }
    
	public static Connection join(Manager manager) throws RollbackException {
		TranImpl t = myTran.get();
		if (t == null) throw new RollbackException("Must be in a transaction.", new RollbackException("transaction null"));
		
		if (t.manager != null && t.manager != manager && manager != null) {
			rollbackAndThrow("Cannot involve two managers in one transaction. Already involved: " + t.manager +
					".  Trying to join: " + manager);
		}
		
		if (t.connection != null) return t.connection;
		
		try {
			if (manager == null) throw new RollbackException(new DaoException("null manager"));
			t.manager = manager;
			t.connection = manager.getConnection();
			t.connection.setAutoCommit(false);
			return t.connection;
		} catch (SQLException | RollbackException e) {
			rollbackAndThrow(e);
			throw new AssertionError("Can't happen (rollbackAndThrow returned).");
		}
	}

	private void executeCommit() throws RollbackException {
		myTran.set(null);

		if (connection != null) {
			try {
				connection.commit();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				try { connection.close(); } catch (SQLException e2) {
					e2.printStackTrace();
				}
				throw new RollbackException(e);
			}
		}
	}

	private void executeRollback() {
		myTran.set(null);

		if (connection != null) {
			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				try { connection.close(); } catch (SQLException e2) {
					e2.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
}
