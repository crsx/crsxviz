package contentmanager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ActiveRuleManager {
	private static ActiveRuleManager _singleton;
	private static boolean _loadingComplete = false;
	private static int _loadedCount = 0;
	private static int _totalCount = 0;
	
	public final String[] ActiveRules;
	
	public static float getInitializeCompletion() {
		if (_loadingComplete)
			return 1;
		if (_totalCount == 0)
			return 0;
		return ((float)_loadedCount)/((float)_totalCount);
	}
	
	public static void Initialize(Connection c) throws IllegalAccessError {
		if (_singleton != null)
			throw new IllegalAccessError("ActiveRuleManager already initialized.");
		if (c == null)
			throw new IllegalArgumentException("Connection cannot be null");
		
		_singleton = new ActiveRuleManager(c);
	}
	
	protected static ActiveRuleManager instance() throws IllegalAccessError {
		if (_loadedCount != 0 && !_loadingComplete)
			throw new IllegalAccessError("ActiveRuleManager initialization " + getInitializeCompletion() + "% complete. Please wait...");
		if (_singleton == null)
			throw new IllegalAccessError("ActiveRuleManager must be initialized first.");
		
		return _singleton;
	}
	
	protected ActiveRuleManager(Connection c) {
		ActiveRules = loadActiveRules(c);
	}
	
	protected String[] loadActiveRules(Connection c) {
		String[] arr = null;
		
		try {
			Statement s = c.createStatement();
			s.closeOnCompletion();
			if (!s.execute("SELECT Count(*) FROM `ActiveRules`;")) {
				throw new RuntimeException("Error executing COUNT on ActiveRules");
			} else {
				ResultSet rs = s.getResultSet();
				if (!rs.next()) {
					System.err.println("Error! 0 rows returned from COUNT `ActiveRules`");
					return null;
				}
				_totalCount = rs.getInt(1);
				arr = new String[_totalCount];
				rs.close();
			}
		} catch (SQLException e) {
			System.err.println("Error getting row count from `ActiveRules`");
			e.printStackTrace();
			return null;
		}
		
		try {
			Statement s = c.createStatement();
			if (!s.execute("SELECT * FROM `ActiveRules`;")) {
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
	
	public static String getActiveRule(int id) {
		return ActiveRuleManager.instance().ActiveRules[id];
	}
	
	public static Integer numActiveRules() {
		return ActiveRuleManager.instance().ActiveRules.length;
	}
	
	public static String[] getActiveRules() {
		return ActiveRuleManager.instance().ActiveRules;
	}
	
}
