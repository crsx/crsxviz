package crsxviz.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import persistence.BeanAccess;
import persistence.CookiesAccess;
import persistence.Manager;
import persistence.RollbackException;
import persistence.beans.CookieBean;
import crsxviz.TestManager;

public class CookiesAccessTest {

	private static Manager instance;
	private BeanAccess<CookieBean> Cookies;
	
	@Before
	public void setUp() throws Exception {
		instance = TestManager.getTestInstance();
		Cookies = new CookiesAccess(instance);
		//TestDbUtils.setUpDb();
	}

	@Test
	public void getCorrectNumberOfCookies() throws RollbackException {
		assertEquals(18, Cookies.getCount());
	}
	
	@Test
	public void getCorrectCookieById() throws RollbackException {
		CookieBean bean = Cookies.get(1);
		assertNotNull(bean);
		assertEquals(1, bean.getCookieId());
		assertEquals("Compile-Helper[Let", bean.getValue());
	}
	
	@Test
	public void getCorrectCookiesByGetAll() throws RollbackException {
		List<CookieBean> list = Cookies.getAll();
		assertEquals(18, list.size());
	}
}
