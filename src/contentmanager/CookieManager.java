package contentmanager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CookieManager {
	private static CookieManager _singleton;
	private static boolean _loadingComplete = false;
	private static int _loadedCount = 0;
	private static int _totalCount = 0;
	
	public final String[] cookies;
	
	public static float getInitializeCompletion() {
		if (_loadingComplete)
			return 1;
		if (_totalCount == 0)
			return 0;
		return ((float)_loadedCount)/((float)_totalCount);
	}
	
	public static void Initialize(Connection c) throws IllegalAccessError {
		if (_singleton != null)
			throw new IllegalAccessError("CookieManager already initialized.");
		if (c == null)
			throw new IllegalArgumentException("Connection cannot be null");
		
		_singleton = new CookieManager(c);
	}
	
	protected static CookieManager instance() throws IllegalAccessError {
		if (_loadedCount != 0 && !_loadingComplete)
			throw new IllegalAccessError("CookieManager initialization " + getInitializeCompletion() + "% complete. Please wait...");
		if (_singleton == null)
			throw new IllegalAccessError("CookieManager must be initialized first.");
		
		return _singleton;
	}
	
	protected CookieManager(Connection c) {
		cookies = loadCookies(c);
	}
	
	protected String[] loadCookies(Connection c) {
		String[] arr = null;
		
		try {
			Statement s = c.createStatement();
			s.closeOnCompletion();
			if (!s.execute("SELECT Count(*) FROM `Cookies`;")) {
				throw new RuntimeException("Error executing COUNT on Cookies");
			} else {
				ResultSet rs = s.getResultSet();
				if (!rs.next()) {
					System.err.println("Error! 0 rows returned from COUNT `Cookies`");
					return null;
				}
				_totalCount = rs.getInt(1);
				arr = new String[_totalCount];
				rs.close();
			}
		} catch (SQLException e) {
			System.err.println("Error getting row count from `Cookies`");
			e.printStackTrace();
			return null;
		}
		
		try {
			Statement s = c.createStatement();
			if (!s.execute("SELECT * FROM `Cookies`;")) {
				System.out.println("Error executing SELECT");
				System.exit(-1);
			} else {
				ResultSet rs = s.getResultSet();
				rs.next();
				if (rs.isFirst()) {
					do {
						Integer id = rs.getInt(1);
						String value = rs.getString(2);
						arr[id] = value;
						_loadedCount++;
					} while (rs.next());
				} else {
					System.out.println("Error! 0 rows returned");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		_loadingComplete = true;
		return arr;
	}
	
	public static String getCookie(int id) {
		return CookieManager.instance().cookies[id];
	}
	
	public static Integer numCookies() {
		return CookieManager.instance().cookies.length;
	}
	
}
