package crsxviz.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import persistence.ActiveRulesAccess;
import persistence.BeanAccess;
import persistence.Manager;
import persistence.RollbackException;
import persistence.beans.ActiveRuleBean;
import crsxviz.TestManager;

public class ActiveRulesAccessTest {

	private static Manager instance;
	private BeanAccess<ActiveRuleBean> ActiveRules;
	
	@Before
	public void setUp() throws Exception {
		instance = TestManager.getTestInstance();
		ActiveRules = new ActiveRulesAccess(instance);
		//TestDbUtils.setUpDb();
	}

	@Test
	public void getCorrectNumberOfActiveRules() throws RollbackException {
		assertEquals(12, ActiveRules.getCount());
	}
	
	@Test
	public void getCorrectActiveRuleById() throws RollbackException {
		ActiveRuleBean bean = ActiveRules.get(1);
		assertNotNull(bean);
		assertEquals(1, bean.getActiveRuleId());
		assertEquals("Compile-Finish[0]", bean.getValue());
	}
	
	@Test
	public void getCorrectActiveRulesByGetAll() throws RollbackException {
		List<ActiveRuleBean> list = ActiveRules.getAll();
		assertEquals(12, list.size());
	}
}
