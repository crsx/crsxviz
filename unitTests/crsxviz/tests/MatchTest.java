package crsxviz.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import persistence.BasicDao;
import persistence.DataAccessException;
import persistence.Manager;
import persistence.Matcher;
import persistence.PrimaryKey;
import persistence.RollbackException;
import persistence.beans.StepBean;
import persistence.impl.Property;
import persistence.impl.matcharg.BinaryMatcher;
import persistence.impl.matcharg.Leaf;
import persistence.impl.matcharg.UnaryMatcher;
import crsxviz.TestDbUtils;
import crsxviz.TestManager;

public class MatchTest {

	private Manager instance;
	private BasicDao<StepBean> basicDao;
	
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
	public void happyPathMatchContainsAndEquals() throws RollbackException {
		assertEquals(3, basicDao.match(Matcher.contains("activeTerm", "activeTerm")).length);
		assertEquals(3, basicDao.match(Matcher.containsIgnoreCase("activeterm", "activeterm")).length);
		
		StepBean result[] = basicDao.match(Matcher.equals("stepNum", 1));
		assertEquals(1, result.length);
		assertEquals(1, result[0].getStepNum());
		
		assertEquals(3, basicDao.match(Matcher.equalsIgnoreCase("startdata", "startdata")).length);
		assertEquals(3, basicDao.match(Matcher.notEquals("stepNum", 0)).length);
	}
	
	@Test
	public void happyPathAnd() throws RollbackException {
		assertEquals(1, basicDao.match(Matcher.and(Matcher.equals("stepNum", 1), Matcher.equals("activeTerm", "activeTerm"))).length);
		assertEquals(0, basicDao.match(Matcher.and(Matcher.equals("stepNum", 1), Matcher.equals("startFrees", 0))).length);
	}
	
	@Test
	public void happyPathEndsWithStartsWith() throws RollbackException {
		assertEquals(3, basicDao.match(Matcher.endsWith("activeTerm", "Term")).length);
		assertEquals(3, basicDao.match(Matcher.endsWithIgnoreCase("activeterm", "term")).length);
		
		assertEquals(0, basicDao.match(Matcher.endsWithIgnoreCase("activeterm", "cat")).length);
		assertEquals(0, basicDao.match(Matcher.endsWith("activeterm", "cat")).length);
		
		assertEquals(3, basicDao.match(Matcher.startsWith("activeterm", "active")).length);
		assertEquals(3, basicDao.match(Matcher.startsWithIgnoreCase("activeterm", "AcTive")).length);
		assertEquals(0, basicDao.match(Matcher.startsWithIgnoreCase("activeterm", "cat")).length);
		assertEquals(0, basicDao.match(Matcher.startsWith("activeterm", "cat")).length);
	}
	
	@Test
	public void happyPathRangeValidation() throws RollbackException {
		assertEquals(2, basicDao.match(Matcher.greaterThan("stepNum", 1)).length);
		assertEquals(3, basicDao.match(Matcher.greaterThanOrEqualTo("stepNum", 1)).length);
		
		assertEquals(3, basicDao.match(Matcher.lessThan("stepNum", 4)).length);
		assertEquals(3, basicDao.match(Matcher.lessThanOrEqualTo("stepNum", 3)).length);
	}
	
	@Test
	public void happyPathMaxMin() throws RollbackException {
		assertEquals(3, basicDao.match(Matcher.max("stepNum"))[0].getStepNum());
		assertEquals(1, basicDao.match(Matcher.min("stepNum"))[0].getStepNum());
	}
	
	@Test
	public void happyPathOr() throws RollbackException {
		assertEquals(2, basicDao.match(Matcher.or(Matcher.equals("stepNum", 1), Matcher.equals("stepNum", 2))).length);
	}
	
	@Test(expected=RollbackException.class)
	public void throwExceptionWhenArgumentGivenIsIncorrect() throws RollbackException {
		assertEquals(1, basicDao.match(Matcher.max("car")).length);
	}
	
	@Test(expected=NullPointerException.class)
	public void throwExceptionWhenNullConstraintGiven() throws RollbackException {
		basicDao.match((Matcher[])null);
	}
	
	@Test
	public void returnEmptyArrayForNoResultsFound() throws RollbackException {
		assertEquals(0, basicDao.match(Matcher.greaterThan("stepNum", 9)).length);
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
