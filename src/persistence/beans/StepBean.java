package persistence.beans;

import persistence.PrimaryKey;

@PrimaryKey("stepNum")
public class StepBean {
	private int stepNum;
	private int indentation;
	private int activeRuleId;
	private int startAllocs;
	private int startFrees;
	private int completeAllocs;
	private int completeFrees;
	private String startData;
	private String completeData;
	private String cookies;
	
	public StepBean() { }
	
	public StepBean(int stepNum) {
		this.stepNum = stepNum;
	}

	public StepBean(int stepNum, int indentation, int activeRuleId,
			int startAllocs, int startFrees, int completeAllocs,
			int completeFrees, String startData, String completeData,
			String cookies) {
		super();
		this.stepNum = stepNum;
		this.indentation = indentation;
		this.activeRuleId = activeRuleId;
		this.startAllocs = startAllocs;
		this.startFrees = startFrees;
		this.completeAllocs = completeAllocs;
		this.completeFrees = completeFrees;
		this.startData = startData;
		this.completeData = completeData;
		this.cookies = cookies;
	}

	public int getStepNum() {
		return stepNum;
	}

	public void setStepNum(int stepNum) {
		this.stepNum = stepNum;
	}

	public int getIndentation() {
		return indentation;
	}

	public void setIndentation(int indentation) {
		this.indentation = indentation;
	}

	public int getActiveRuleId() {
		return activeRuleId;
	}

	public void setActiveRuleId(int activeRuleId) {
		this.activeRuleId = activeRuleId;
	}

	public int getStartAllocs() {
		return startAllocs;
	}

	public void setStartAllocs(int startAllocs) {
		this.startAllocs = startAllocs;
	}

	public int getStartFrees() {
		return startFrees;
	}

	public void setStartFrees(int startFrees) {
		this.startFrees = startFrees;
	}

	public int getCompleteAllocs() {
		return completeAllocs;
	}

	public void setCompleteAllocs(int completeAllocs) {
		this.completeAllocs = completeAllocs;
	}

	public int getCompleteFrees() {
		return completeFrees;
	}

	public void setCompleteFrees(int completeFrees) {
		this.completeFrees = completeFrees;
	}

	public String getStartData() {
		return startData;
	}

	public void setStartData(String startData) {
		this.startData = startData;
	}

	public String getCompleteData() {
		return completeData;
	}

	public void setCompleteData(String completeData) {
		this.completeData = completeData;
	}

	public String getCookies() {
		return cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
}