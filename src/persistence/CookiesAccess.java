package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import persistence.beans.CookieBean;

public class CookiesAccess extends BeanAccess<CookieBean> {

	public CookiesAccess(Manager manager) {
		super(manager);
	}

	@Override
	protected String getCountStatement() {
		return "SELECT Count(*) FROM `Cookies`;";
	}

	@Override
	protected String getAllStatement() {
		return "SELECT * FROM `Cookies`;";
	}
	
	@Override
	protected String getStatement(int id) {
		return "SELECT * FROM `Cookies` where CookieId=" + id + ";";
	}

	@Override
	protected List<CookieBean> buildBean(ResultSet rs) throws SQLException {
		List<CookieBean> list = new ArrayList<CookieBean>();
		while (rs.next()) 
			list.add(buildSingle(rs));
		return list;
	}

	@Override
	protected CookieBean buildSingle(ResultSet rs) throws SQLException {
		CookieBean bean = new CookieBean();
		bean.setCookieId(rs.getInt("CookieId"));
		bean.setValue(rs.getString("value"));
		return bean;
	}
}
