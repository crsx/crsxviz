package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import persistence.beans.ActiveRuleBean;

public class ActiveRulesAccess extends BeanAccess<ActiveRuleBean> {

	public ActiveRulesAccess(Manager manager) {
		super(manager);
	}

	@Override
	protected String getCountStatement() {
		return "SELECT Count(*) FROM `ActiveRules`;";
	}

	@Override
	protected String getAllStatement() {
		return "SELECT * FROM `ActiveRules`;";
	}
	
	@Override
	protected String getStatement(int id) {
		return "SELECT * FROM `ActiveRules` where ActiveRuleId=" + id + ";";
	}

	@Override
	protected List<ActiveRuleBean> buildBean(ResultSet rs) throws SQLException {
		List<ActiveRuleBean> list = new ArrayList<ActiveRuleBean>();
		
		while (rs.next())
			list.add(buildSingle(rs));
		return list;
	}

	@Override
	protected ActiveRuleBean buildSingle(ResultSet rs) throws SQLException {
		ActiveRuleBean bean = new ActiveRuleBean();
		bean.setActiveRuleId(rs.getInt("activeRuleId"));
		bean.setValue(rs.getString("value"));
		return bean;
	}
}
