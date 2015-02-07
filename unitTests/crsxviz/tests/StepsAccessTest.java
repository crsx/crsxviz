package crsxviz.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import persistence.BeanAccess;
import persistence.Manager;
import persistence.RollbackException;
import persistence.StepsAccess;
import persistence.beans.StepBean;
import crsxviz.TestManager;

public class StepsAccessTest {

	private static Manager instance;
	private BeanAccess<StepBean> steps;
	
	@Before
	public void setUp() throws Exception {
		instance = TestManager.getTestInstance();
		steps = new StepsAccess(instance);
		//TestDbUtils.setUpDb();
		//TestDbUtils.extractData("let2.db", "full-dataset.xml");
	}

	@Test
	public void getCorrectNumberOfSteps() throws RollbackException {
		assertEquals(22, steps.getCount());
	}
	
	@Test
	public void getCorrectStepById() throws RollbackException {
		StepBean bean = steps.get(2);
		assertNotNull(bean);
		assertEquals(2, bean.getStepNum());
		assertEquals(1, bean.getActiveRuleId());
		assertEquals(428, bean.getCompleteAllocs());
		assertEquals(214, bean.getCompleteFrees());
		assertEquals(2, bean.getCookies().get(1).intValue());
		assertEquals(1, bean.getIndentation());
		assertEquals(134, bean.getStartAllocs());
		assertEquals(22, bean.getStartFrees());
	}
	
	@Test
	public void getCorrectStepsByGetAll() throws RollbackException {
		List<StepBean> list = steps.getAll();
		assertEquals(22, list.size());
	}
	
	@Test(expected=NullPointerException.class)
	public void npeWhenNullPassedToConstructor() throws RollbackException {
		new StepsAccess(null);
	}
	
	@Test
	public void exceptionsThrownForIncorrectSQL() throws RollbackException {
		EvilClass bad = new EvilClass(instance);
		try {
			bad.getAll();
		} catch (RollbackException e) {
			assertTrue(true);
		}
		
		try {
			bad.get(999);
		} catch (RollbackException e) {
			assertTrue(true);
		}
		
		try {
			bad.getCount();
		} catch (RollbackException e) {
			assertTrue(true);
		}
	}
	
	private class EvilClass extends BeanAccess<StepBean> {

		public EvilClass(Manager manager) {
			super(manager);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected String getCountStatement() {
			return "Evil sql string";
		}

		@Override
		protected String getAllStatement() {
			return "Evil sql string";
		}

		@Override
		protected String getStatement(int id) {
			return "Evil sql string";
		}

		@Override
		protected List<StepBean> buildBean(ResultSet rs) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected StepBean buildSingle(ResultSet rs) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
