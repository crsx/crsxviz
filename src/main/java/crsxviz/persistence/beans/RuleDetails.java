package crsxviz.persistence.beans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RuleDetails {
	public final Long ActiveRuleID;
	public final String Value;
	public final String Env;
	public final String Args;
	public final String ResultType;
	public final String SrcRuleName;
	public final Long SrcRuleOffset;
	public final String SrcRuleIdent;
	public final String SrcRuleArgs;
	public final String StartState;
	public final String EndState;
	
	public RuleDetails(ResultSet r) throws SQLException {
		if (r == null)
			throw new IllegalArgumentException("Null is not a valid argument");
		ActiveRuleID = r.getLong(1);
		Value = r.getString("Value");
		Env = r.getString("Env");
		Args = r.getString("Args");
		ResultType = r.getString("ResultType");
		SrcRuleName = r.getString("SrcRuleName");
		SrcRuleOffset = r.getLong("SrcRuleOffset");
		SrcRuleIdent = r.getString("SrcRuleIdent");
		SrcRuleArgs = r.getString("SrcRuleArgs");
		StartState = r.getString("StartState");
		EndState = r.getString("EndState");		
	}
	
	public static String toString(List<RuleDetails> d) {
		if (d == null)
			throw new IllegalArgumentException("List must be specified");
		if (d.isEmpty())
			return "";
		
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for (RuleDetails r : d) {
			if (first) {
				first = false;
				s.append(r.Value);
				s.append("\nSource Rule: ");
				s.append(r.SrcRuleName);
			}
			s.append("\nMatch for source index ");
			s.append(r.SrcRuleOffset);
			if (r.SrcRuleIdent != null && r.SrcRuleIdent.length() > 0) {
				s.append(" with identifier ");
				s.append(r.SrcRuleIdent);
			}
			s.append("\n\t");
			s.append(r.StartState);
			s.append("\n\t→\n\t");
			s.append(r.EndState);
		}
		return s.toString();
	}
}
