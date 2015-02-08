package contentmanager.beans;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import contentmanager.CookieManager;

public class StepBean {
	public final int stepNum;
	public final int indentation;
	public final int activeRuleId;
	public final int startAllocs;
	public final int startFrees;
	public final int completeAllocs;
	public final int completeFrees;
	public final String startData;
	public final String completeData;
	public final List<Integer> cookies;
	
	private List<Integer> cookieBlobToList(byte[] bs) throws SQLException {
		List<Integer> tmpList = new ArrayList<Integer>();
		if (bs != null && bs.length > 0) {
			if (bs.length % 4 != 0) 
				throw new IllegalArgumentException("cookieBlob contained invalid number of bytes " + bs.length);
			DataInputStream ds = new DataInputStream(new ByteArrayInputStream(bs));
			try {	
				while(tmpList.size() < bs.length / 4) {
				
					tmpList.add(ds.readInt());
				}
				ds.close();
			} catch (IOException e) {
				throw new IllegalArgumentException("CookieBlob read failed before expected end of stream");
			}
		}
		return tmpList;
	}
	
	public StepBean(ResultSet rs) throws SQLException {
		stepNum = rs.getInt(1);
		indentation = rs.getInt(2);
		activeRuleId = rs.getInt(3);
		startAllocs = rs.getInt(4);
		startFrees = rs.getInt(5);
		completeAllocs = rs.getInt(6);
		completeFrees = rs.getInt(7);
		startData = rs.getString(8);
		completeData = rs.getString(9);
		cookies = cookieBlobToList(rs.getBytes(10));
	}
	
	public List<String> getCookieStrings() {
		List<String> tmpList = new ArrayList<String>();
		if (CookieManager.getInitializeCompletion() != 1) {
			System.err.println("WARNING: Attempted to get cookie string from CookieManager before it was fully initialized");
		} else {
			for (int i : cookies) {
				tmpList.add(CookieManager.getCookie(i));
			}
		}
		return tmpList;
	}
}
