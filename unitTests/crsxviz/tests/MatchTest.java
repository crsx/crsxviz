package crsxviz.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import persistence.BasicDao;
import persistence.DataAccessException;
import persistence.Manager;
import persistence.Matcher;
import persistence.PrimaryKey;
import persistence.RollbackException;
import persistence.beans.ActiveRuleBean;
import persistence.beans.CookieBean;
import persistence.beans.StepBean;
import persistence.impl.Property;
import persistence.impl.matcharg.BinaryMatcher;
import persistence.impl.matcharg.Leaf;
import persistence.impl.matcharg.UnaryMatcher;
import crsxviz.TestDbUtils;
import crsxviz.TestManager;

public class MatchTest {

	private static Manager instance;
	private BasicDao<StepBean> stepsDao;
	private BasicDao<ActiveRuleBean> activeRulesDao;
	private BasicDao<CookieBean> cookiesDao;
	
	@BeforeClass
	public static void classSetUp() throws Exception {
		instance = TestManager.getTestInstance();
		//TestDbUtils.extractData();    Uncomment when the database schema changes
		//								This requires updating the database that 
		//								data is extracted from in TestDbUtils
	}
	
	@Before
	public void setUp() {
		instance = TestManager.getTestInstance();
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
	public void happyPathMatchContainsAndEquals() throws RollbackException {
		assertEquals(5, stepsDao.match(Matcher.contains("startData", "Multiply[")).length);
		assertEquals(5, stepsDao.match(Matcher.containsIgnoreCase("startData", "MuLTiply[")).length);
		
		StepBean result[] = stepsDao.match(Matcher.equals("stepNum", 1));
		assertEquals(1, result.length);
		assertEquals(1, result[0].getStepNum());
		
		assertEquals(1, activeRulesDao.match(Matcher.equalsIgnoreCase("value", "Multiply[0]")).length);
		assertEquals(20, stepsDao.match(Matcher.notEquals("stepNum", 0)).length);
	}
	
	@Test
	public void happyPathAnd() throws RollbackException {
		assertEquals(1, stepsDao.match(Matcher.and(Matcher.equals("stepNum", 1), Matcher.equals("cookies", ""))).length);
		assertEquals(0, stepsDao.match(Matcher.and(Matcher.equals("stepNum", 1), Matcher.equals("startFrees", 0))).length);
	}
	
	@Test
	public void happyPathEndsWithStartsWith() throws RollbackException {
		assertEquals(2, cookiesDao.match(Matcher.endsWith("value", "Plus")).length);
		assertEquals(2, cookiesDao.match(Matcher.endsWithIgnoreCase("value", "plus")).length);
		
		assertEquals(0, stepsDao.match(Matcher.endsWithIgnoreCase("startData", "cat")).length);
		assertEquals(0, stepsDao.match(Matcher.endsWith("startData", "cat")).length);
		
		assertEquals(6, cookiesDao.match(Matcher.startsWith("value", "Plus")).length);
		assertEquals(6, cookiesDao.match(Matcher.startsWithIgnoreCase("value", "plus")).length);
		assertEquals(0, stepsDao.match(Matcher.startsWithIgnoreCase("startData", "cat")).length);
		assertEquals(0, stepsDao.match(Matcher.startsWith("startData", "cat")).length);
	}
	
	@Test
	public void happyPathRangeValidation() throws RollbackException {
		assertEquals(19, stepsDao.match(Matcher.greaterThan("stepNum", 1)).length);
		assertEquals(20, stepsDao.match(Matcher.greaterThanOrEqualTo("stepNum", 1)).length);
		
		assertEquals(3, stepsDao.match(Matcher.lessThan("stepNum", 4)).length);
		assertEquals(3, stepsDao.match(Matcher.lessThanOrEqualTo("stepNum", 3)).length);
	}
	
	@Test
	public void happyPathMaxMin() throws RollbackException {
		assertEquals(20, stepsDao.match(Matcher.max("stepNum"))[0].getStepNum());
		assertEquals(1, stepsDao.match(Matcher.min("stepNum"))[0].getStepNum());
	}
	
	@Test
	public void happyPathOr() throws RollbackException {
		assertEquals(2, stepsDao.match(Matcher.or(Matcher.equals("stepNum", 1), Matcher.equals("stepNum", 2))).length);
	}
	
	@Test(expected=RollbackException.class)
	public void throwExceptionWhenArgumentGivenIsIncorrect() throws RollbackException {
		assertEquals(1, stepsDao.match(Matcher.max("car")).length);
	}
	
	@Test(expected=NullPointerException.class)
	public void throwExceptionWhenNullConstraintGiven() throws RollbackException {
		stepsDao.match((Matcher[])null);
	}
	
	@Test
	public void returnEmptyArrayForNoResultsFound() throws RollbackException {
		assertEquals(0, stepsDao.match(Matcher.greaterThan("stepNum", 999)).length);
	}
	
	@Test(expected=DataAccessException.class)
	public void improperPropertyTypeInBean() throws RollbackException, DataAccessException {
		Property props[] = Property.findProperties(TestBean.class, true);
		new Leaf(props, (BinaryMatcher) Matcher.greaterThan("id", 999));
	}
	
	@Test(expected=NullPointerException.class)
	public void contraintCanNotBeNull() throws DataAccessException {
		Property props[] = Property.findProperties(StepBean.class, true);
		new Leaf(props, (UnaryMatcher)null);
	}
	
	@PrimaryKey("id")
	protected class TestBean {
		private int id;
		private Object obj;
		
		public TestBean() { }
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public Object getObj() {
			return obj;
		}
		public void setObj(Object obj) {
			this.obj = obj;
		}
		
		
	}
}
