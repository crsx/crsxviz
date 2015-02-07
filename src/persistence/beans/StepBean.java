package persistence.beans;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

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
	private List<Integer> cookies;
	
	public StepBean() { 
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

	public List<Integer> getCookies() {
		return this.cookies;
	}

	public void setCookies(byte[] array) {
		if (array == null) 
			return;
		cookies = new ArrayList<Integer>();
		
		IntBuffer buf = ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		int[] tmp = new int[buf.remaining()];
		buf.get(tmp);
		for (int i : tmp)
			cookies.add(i);
   	}
}