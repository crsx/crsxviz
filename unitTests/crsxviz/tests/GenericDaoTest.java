package crsxviz.tests;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import persistence.DaoException;
import persistence.GenericDao;
import persistence.Manager;
import persistence.RollbackException;
import persistence.beans.StepBean;
import crsxviz.TestDbUtils;
import crsxviz.TestManager;

public class GenericDaoTest {
	
	private Manager instance;
	private GenericDao<StepBean> genericDao;
	private static final String table = "testTable";
	
	@Before
	public void setUp() {
		instance = TestManager.getTestInstance();
		try {
			genericDao = new GenericDao<StepBean>(StepBean.class, "steps", instance);
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
		new GenericDao<StepBean>(StepBean.class, table, instance); 
	}

	@Test
	public void basicHappyPathCrudOperations() {
		try {
			assertEquals(3, genericDao.getCount());
			genericDao.create(new StepBean(4, 4, "activeTerm", 4, 4, 4, 4, "startData", "completeData", "cookies"));
			assertEquals(4, genericDao.getCount());
			
			genericDao.update(new StepBean(4, 5, "activeTerm", 5, 5, 5, 5, "startData", "completeData", "cookies"));
			assertEquals(5, genericDao.read(4).getIndentation());
			
			genericDao.delete(1);
			assertEquals(3, genericDao.getCount());
			
		} catch (RollbackException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected=RollbackException.class)
	public void duplicateBeanPassedToCreate() throws RollbackException {
		genericDao.create(new StepBean(1));
	}

	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToRead() throws RollbackException {
		genericDao.read("hat");
	}
	
	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToDelete() throws RollbackException {
		genericDao.delete("hat");
	}
	
	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToUpdate() throws RollbackException {
		genericDao.update(new StepBean());
	}
	
	@Test(expected=NullPointerException.class)
	public void nullManagerPassedToConstructor() throws DaoException, RollbackException {
		new GenericDao<StepBean>(StepBean.class, "steps", null);
	}
	
	@Test(expected=NullPointerException.class)
	public void nullBeanClassPassedToConstructor() throws DaoException, RollbackException {
		new GenericDao<StepBean>(null, "steps", instance);
	}
}
