package persistence.beans;


public class CookieBean {
	private int cookieId;
	private String value;
	
	public CookieBean() { }

	public int getCookieId() {
		return cookieId;
	}

	public void setCookieId(int cookieId) {
		this.cookieId = cookieId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
