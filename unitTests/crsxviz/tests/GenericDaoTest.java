package crsxviz.tests;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import persistence.BasicDao;
import persistence.DataAccessException;
import persistence.Manager;
import persistence.PrimaryKey;
import persistence.RollbackException;
import persistence.beans.StepBean;
import crsxviz.TestDbUtils;
import crsxviz.TestManager;

public class GenericDaoTest {
	
	private Manager instance;
	private BasicDao<StepBean> basicDao;
	private static final String table = "testTable";
	
	@Before
	public void setUp() {
		instance = TestManager.getTestInstance();
		try {
			basicDao = new BasicDao<StepBean>(StepBean.class, "steps", instance);
			TestDbUtils.setUpDb();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test 
	public void newTableCreation() throws Exception {
		try {
			TestDbUtils.removeTable(instance.getConnection(), table);
		} catch (SQLException e) {
			// ignore it
		}
		new BasicDao<StepBean>(StepBean.class, table, instance); 
	}

	@Test
	public void basicHappyPathCrudOperations() {
		try {
			assertEquals(3, basicDao.getCount());
			basicDao.create(new StepBean(4, 4, "activeTerm", 4, 4, 4, 4, "startData", "completeData", "cookies"));
			assertEquals(4, basicDao.getCount());
			
			basicDao.update(new StepBean(4, 5, "activeTerm", 5, 5, 5, 5, "startData", "completeData", "cookies"));
			assertEquals(5, basicDao.read(4).getIndentation());
			
			basicDao.delete(1);
			assertEquals(3, basicDao.getCount());
			
		} catch (RollbackException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected=RollbackException.class)
	public void noRowToDeleteWithGivenPrimaryKey() throws RollbackException {
		basicDao.delete(999);
	}
	
	@Test(expected=RollbackException.class)
	public void duplicateBeanPassedToCreate() throws RollbackException {
		basicDao.create(new StepBean(1));
	}

	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToRead() throws RollbackException {
		basicDao.read("hat");
	}
	
	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToDelete() throws RollbackException {
		basicDao.delete("hat");
	}
	
	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToUpdate() throws RollbackException {
		basicDao.update(new StepBean());
	}
	
	@Test(expected=NullPointerException.class)
	public void nullManagerPassedToConstructor() throws DataAccessException, RollbackException {
		new BasicDao<StepBean>(StepBean.class, "steps", null);
	}
	
	@Test(expected=NullPointerException.class)
	public void nullBeanClassPassedToConstructor() throws DataAccessException, RollbackException {
		new BasicDao<StepBean>(null, "steps", instance);
	}
	
	@Test
	public void longPrimaryKey() throws RollbackException, DataAccessException {
		new BasicDao<Tester>(Tester.class, "test", instance);
	}
	
	@PrimaryKey("id")
	protected class Tester {
		private long id;
		private Tester() { } 
		public long getId() { return id; }
		public void setId(long id) { this.id = id; }
	}
}
