package persistence.beans;

import persistence.PrimaryKey;

@PrimaryKey("stepNum")
public class StepBean {
	private int stepNum;
	private int indentation;
	private String activeTerm;
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

	public StepBean(int stepNum, int indentation, String activeTerm,
			int startAllocs, int startFrees, int completeAllocs,
			int completeFrees, String startData, String completeData,
			String cookies) {
		super();
		this.stepNum = stepNum;
		this.indentation = indentation;
		this.activeTerm = activeTerm;
		this.startAllocs = startAllocs;
		this.startFrees = startFrees;
		this.completeAllocs = completeAllocs;
		this.completeFrees = completeFrees;
		this.startData = startData;
		this.completeData = completeData;
		this.cookies = cookies;
	}

	/**
	 * @return the stepNum
	 */
	public int getStepNum() {
		return stepNum;
	}

	/**
	 * @param stepNum the stepNum to set
	 */
	public void setStepNum(int stepNum) {
		this.stepNum = stepNum;
	}

	/**
	 * @return the indentation
	 */
	public int getIndentation() {
		return indentation;
	}

	/**
	 * @param indentation the indentation to set
	 */
	public void setIndentation(int indentation) {
		this.indentation = indentation;
	}

	/**
	 * @return the activeTerm
	 */
	public String getActiveTerm() {
		return activeTerm;
	}

	/**
	 * @param activeTerm the activeTerm to set
	 */
	public void setActiveTerm(String activeTerm) {
		this.activeTerm = activeTerm;
	}

	/**
	 * @return the startAllocs
	 */
	public int getStartAllocs() {
		return startAllocs;
	}

	/**
	 * @param startAllocs the startAllocs to set
	 */
	public void setStartAllocs(int startAllocs) {
		this.startAllocs = startAllocs;
	}

	/**
	 * @return the startFrees
	 */
	public int getStartFrees() {
		return startFrees;
	}

	/**
	 * @param startFrees the startFrees to set
	 */
	public void setStartFrees(int startFrees) {
		this.startFrees = startFrees;
	}

	/**
	 * @return the completeAllocs
	 */
	public int getCompleteAllocs() {
		return completeAllocs;
	}

	/**
	 * @param completeAllocs the completeAllocs to set
	 */
	public void setCompleteAllocs(int completeAllocs) {
		this.completeAllocs = completeAllocs;
	}

	/**
	 * @return the completeFrees
	 */
	public int getCompleteFrees() {
		return completeFrees;
	}

	/**
	 * @param completeFrees the completeFrees to set
	 */
	public void setCompleteFrees(int completeFrees) {
		this.completeFrees = completeFrees;
	}

	/**
	 * @return the startData
	 */
	public String getStartData() {
		return startData;
	}

	/**
	 * @param startData the startData to set
	 */
	public void setStartData(String startData) {
		this.startData = startData;
	}

	/**
	 * @return the completeData
	 */
	public String getCompleteData() {
		return completeData;
	}

	/**
	 * @param completeData the completeData to set
	 */
	public void setCompleteData(String completeData) {
		this.completeData = completeData;
	}

	/**
	 * @return the cookies
	 */
	public String getCookies() {
		return cookies;
	}

	/**
	 * @param cookies the cookies to set
	 */
	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
	
}