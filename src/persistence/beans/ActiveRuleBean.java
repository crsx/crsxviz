package persistence.beans;

import persistence.PrimaryKey;

@PrimaryKey("activeRuleId")
public class ActiveRuleBean {
	private int activeRuleId;
	private String value;
	
	public ActiveRuleBean() { }
	
	public int getActiveRuleId() { return this.activeRuleId; }
	public void setActiveRuleId(int activeRuleId) { this.activeRuleId = activeRuleId; }
	public String getValue() { return this.value; }
	public void setValue(String value) { this.value = value; }
}
