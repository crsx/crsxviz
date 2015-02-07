package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import persistence.beans.StepBean;

public class StepsAccess extends BeanAccess<StepBean> {

	public StepsAccess(Manager manager) {
		super(manager);
	}

	@Override
	protected String getCountStatement() {
		return "SELECT Count(*) FROM `Steps`;";
	}

	@Override
	protected String getAllStatement() {
		return "SELECT * FROM `Steps`;";
	}
	
	@Override
	protected String getStatement(int id) {
		return "SELECT * FROM `Steps` where StepNum=" + id + ";";
	}

	@Override
	protected List<StepBean> buildBean(ResultSet rs) throws SQLException {
		List<StepBean> list = new ArrayList<StepBean>();
		while (rs.next()) 
			list.add(buildSingle(rs));
		return list;
	}

	@Override
	protected StepBean buildSingle(ResultSet rs) throws SQLException {
		StepBean bean = new StepBean();
		bean.setStepNum(rs.getInt("stepNum"));
		bean.setIndentation(rs.getInt("indentation"));
		bean.setActiveRuleId(rs.getInt("activeRuleId"));
		bean.setStartAllocs(rs.getInt("startAllocs"));
		bean.setStartData(rs.getString("startData"));
		bean.setCompleteAllocs(rs.getInt("completeAllocs"));
		bean.setCompleteData(rs.getString("completeData"));
		bean.setStartFrees(rs.getInt("startFrees"));
		bean.setCookies(rs.getBytes("cookies"));
		bean.setCompleteFrees(rs.getInt("completeFrees"));
		return bean;
	}
}
