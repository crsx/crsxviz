package persistence;

import persistence.impl.TranImpl;

/**
 * This class is used to begin and end transactions.
 */
public class Transaction {

	/**
	 * Private constructor to prevent instantiation
	 */
	private Transaction() { }

	/**
	 * Begins a new transaction for this thread.
	 * @throws RollbackException if there is some reason the transaction could not be started.
	 * One reason is if you are already in a transaction.
	 */
	public static void begin() throws RollbackException {
		TranImpl.begin();
	}

	/**
	 * Commits the work performed by the currently running transaction.
	 * @throws RollbackException if there is some reason the transaction could not be committed.
	 */
	public static void commit() throws RollbackException {
        TranImpl.commit();
	}
	
	/**
	 * Tests whether a transaction is currently running for this thread.
	 * @return true if this thread is in a transaction.
	 */
	public static boolean isActive() {
        return TranImpl.isActive();
	}

	/**
	 * Causes the work performed by the currently running transaction to be undone.
	 * @throws AssertionError if not in a transaction.
	 */
	public static void rollback() {
        TranImpl.rollback();
	}
}
