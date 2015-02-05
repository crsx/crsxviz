package crsxviz.tests;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import persistence.BasicDao;
import persistence.DataAccessException;
import persistence.Manager;
import persistence.PrimaryKey;
import persistence.RollbackException;
import persistence.beans.ActiveRuleBean;
import persistence.beans.CookieBean;
import persistence.beans.StepBean;
import crsxviz.TestDbUtils;
import crsxviz.TestManager;

public class BasicDaoTest {
	
	private static Manager instance;
	private BasicDao<StepBean> stepsDao;
	private BasicDao<ActiveRuleBean> activeRulesDao;
	private BasicDao<CookieBean> cookiesDao;
	private static final String table = "testTable";
	
	@BeforeClass
	public static void classSetUp() throws Exception {
		instance = TestManager.getTestInstance();
		//TestDbUtils.extractData("let2.db", "test.xml");  
		//								Uncomment if a new database to extra data from
		//								is added or a new output xml file is needed
		//								This requires updating the database that 
		//								data is extracted from in TestDbUtils
	}
	
	@Before
	public void setUp() {
		try {
			stepsDao = new BasicDao<StepBean>(StepBean.class, "steps", instance);
			activeRulesDao = new BasicDao<ActiveRuleBean>(ActiveRuleBean.class, "activeRules", instance);
			cookiesDao = new BasicDao<CookieBean>(CookieBean.class, "cookies", instance);
			TestDbUtils.setUpDb();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void validCookiesStored() throws Exception {
		CookieBean result = cookiesDao.read(1);
		assertEquals(1, result.getCookieId());
		assertEquals("Multiply[Plus", result.getValue());
	}
	
	@Test
	public void invalidCookieId() throws Exception {
		assertEquals(null, cookiesDao.read(999));
	}
	
	@Test
	public void validActiveRulesStored() throws Exception {
		ActiveRuleBean result = activeRulesDao.read(1);
		assertEquals(1, result.getActiveRuleId());
		assertEquals("Plus[0]", result.getValue());
	}
	
	@Test
	public void invalidActiveId() throws Exception {
		assertEquals(null, activeRulesDao.read(999));
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
			assertEquals(20, stepsDao.getCount());
			stepsDao.create(new StepBean());
			assertEquals(21, stepsDao.getCount());
			
			stepsDao.update(new StepBean(4, 5, 5, 5, 5, 5, 5, "startData", "completeData", "1 2 3 4 5"));
			assertEquals(5, stepsDao.read(4).getIndentation());
			
			stepsDao.delete(1);
			assertEquals(20, stepsDao.getCount());
			
		} catch (RollbackException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected=RollbackException.class)
	public void noRowToDeleteWithGivenPrimaryKey() throws RollbackException {
		stepsDao.delete(999);
	}
	
	@Test(expected=RollbackException.class)
	public void duplicateBeanPassedToCreate() throws RollbackException {
		stepsDao.create(new StepBean(1));
	}

	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToRead() throws RollbackException {
		stepsDao.read("hat");
	}
	
	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToDelete() throws RollbackException {
		stepsDao.delete("hat");
	}
	
	@Test(expected=RollbackException.class)
	public void improperArgumentPassedToUpdate() throws RollbackException {
		stepsDao.update(new StepBean());
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
