package crsxviz.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import persistence.GenericDao;
import persistence.Manager;
import persistence.Matcher;
import persistence.RollbackException;
import persistence.beans.StepBean;
import crsxviz.TestDbUtils;
import crsxviz.TestManager;

public class MatchTest {

	private Manager instance;
	private GenericDao<StepBean> genericDao;
	
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
	public void happyPathMatchContainsAndEquals() throws RollbackException {
		assertEquals(3, genericDao.match(Matcher.contains("activeTerm", "activeTerm")).length);
		assertEquals(3, genericDao.match(Matcher.containsIgnoreCase("activeterm", "activeterm")).length);
		
		StepBean result[] = genericDao.match(Matcher.equals("stepNum", 1));
		assertEquals(1, result.length);
		assertEquals(1, result[0].getStepNum());
		
		assertEquals(3, genericDao.match(Matcher.equalsIgnoreCase("startdata", "startdata")).length);
		assertEquals(3, genericDao.match(Matcher.notEquals("stepNum", 0)).length);
	}
	
	@Test
	public void happyPathAnd() throws RollbackException {
		assertEquals(1, genericDao.match(Matcher.and(Matcher.equals("stepNum", 1), Matcher.equals("activeTerm", "activeTerm"))).length);
		assertEquals(0, genericDao.match(Matcher.and(Matcher.equals("stepNum", 1), Matcher.equals("startFrees", 0))).length);
	}
	
	@Test
	public void happyPathEndsWithStartsWith() throws RollbackException {
		assertEquals(3, genericDao.match(Matcher.endsWith("activeTerm", "Term")).length);
		assertEquals(3, genericDao.match(Matcher.endsWithIgnoreCase("activeterm", "term")).length);
		
		assertEquals(0, genericDao.match(Matcher.endsWithIgnoreCase("activeterm", "cat")).length);
		assertEquals(0, genericDao.match(Matcher.endsWith("activeterm", "cat")).length);
		
		assertEquals(3, genericDao.match(Matcher.startsWith("activeterm", "active")).length);
		assertEquals(3, genericDao.match(Matcher.startsWithIgnoreCase("activeterm", "AcTive")).length);
		assertEquals(0, genericDao.match(Matcher.startsWithIgnoreCase("activeterm", "cat")).length);
		assertEquals(0, genericDao.match(Matcher.startsWith("activeterm", "cat")).length);
	}
	
	@Test
	public void happyPathRangeValidation() throws RollbackException {
		assertEquals(2, genericDao.match(Matcher.greaterThan("stepNum", 1)).length);
		assertEquals(3, genericDao.match(Matcher.greaterThanOrEqualTo("stepNum", 1)).length);
		
		assertEquals(3, genericDao.match(Matcher.lessThan("stepNum", 4)).length);
		assertEquals(3, genericDao.match(Matcher.lessThanOrEqualTo("stepNum", 3)).length);
	}
	
	@Test
	public void happyPathMaxMin() throws RollbackException {
		assertEquals(3, genericDao.match(Matcher.max("stepNum"))[0].getStepNum());
		assertEquals(1, genericDao.match(Matcher.min("stepNum"))[0].getStepNum());
	}
	
	@Test
	public void happyPathOr() throws RollbackException {
		assertEquals(2, genericDao.match(Matcher.or(Matcher.equals("stepNum", 1), Matcher.equals("stepNum", 2))).length);
	}
}
