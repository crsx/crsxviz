package persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DbProperties {
	private static final String PROP_FILE = "properties.xml";
	private Map<String, String> xml = null;
	private String file;
	private String driver;
	private String dbpath;
	
	public DbProperties() {
		this(PROP_FILE);
	}
	
	public DbProperties(String dbpath, String driver) {
		this.dbpath = dbpath;
		this.driver = driver;
	}
	
	public DbProperties(String file) {
		this.file = file;
		this.populateXML();
	}
	
	private void populateXML() {
		if (file == null || file.isEmpty()) throw new NullPointerException("property file");
		
		try {
			xml = new HashMap<String, String>();
			FileInputStream fileInput = new FileInputStream(new File(file));
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			
			Enumeration<Object> enu = properties.keys();
			while (enu.hasMoreElements()) {
				String key = (String) enu.nextElement();
				String value = properties.getProperty(key);
				xml.put(key, value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		dbpath = xml.get("jdbcUrl");
		driver = xml.get("jdbcDriver");
	}
	
	public String getJdbcDriver() {
		return dbpath;
	}
	
	public String getJdbcUrl() {
		return driver;
	}
	
	public String getJdbcTestUrl() {
		return xml.get("jdbcTestUrl");
	}
}
