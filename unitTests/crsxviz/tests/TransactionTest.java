package crsxviz.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import persistence.Manager;
import persistence.RollbackException;
import persistence.Transaction;
import persistence.impl.TranImpl;

public class TransactionTest {

	@Test
	public void activeTransactionVerification() throws RollbackException {
		Transaction.begin();
		assertTrue(Transaction.isActive());
		Transaction.rollback();
	}
	
	@Test
	public void activeTransactionCommit() throws RollbackException {
		Transaction.begin();
		assertTrue(Transaction.isActive());
		Transaction.commit();
	}
	
	@Test(expected=RollbackException.class)
	public void cannotOpenTwoTransactions() throws RollbackException {
		Transaction.begin();
		Transaction.begin();
	}
	
	@Test
	public void joinTransactionToTheManager() throws RollbackException {
		Transaction.begin();
		TranImpl.join(Manager.getInstance());
		Transaction.rollback();
	}

	@Test(expected=RollbackException.class)
	public void nullManagerPassedToJoin() throws RollbackException {
		Transaction.begin();
		TranImpl.join(null);
		Transaction.rollback();
	}
}
