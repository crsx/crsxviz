package persistence.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import persistence.DaoException;
import persistence.Manager;
import persistence.impl.matcharg.Leaf;
import persistence.impl.matcharg.MatchOp;

public class SqliteImp<B> extends BasicDaoImp<B> {

    public SqliteImp(Class<B> beanClass, String tableName, Manager manager) throws DaoException{
		super(beanClass, tableName, manager);
    }

	protected String getBlobTypeDeclaration()        { return "BLOB"; }
	protected String getDateTimeTypeDeclaration()    { return "TEXT"; }
	protected String getSerialTypeDeclaration()      { return "INTEGER NOT NULL"; }
	protected String getBigSerialTypeDeclaration()   { return "INTEGER NOT NULL"; }
	protected String getDefaultSchemaName()          { return null;       }
	protected String getLikeOperator()				 { return "LIKE"; }
	protected String getNullSafeEqualsOperator()     { return "IS";  }

	protected Object findExtremeValue(Connection con, Leaf arg, String tableName) throws SQLException {
		Property prop = arg.getProperty();
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		if (arg.getOp() == MatchOp.MAX) {
			sql.append("max(");
		} else {
			sql.append("min(");
		}
		if (prop.getType() == String.class) sql.append("binary ");
		sql.append(prop.getName());
		sql.append(") as matchValue from ");
		sql.append(tableName);
		//sql.append(" for update"); for update not supported in sqlite
		//row locking happens by default 

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql.toString());
        // If no rows in the table, then NULL is returned for max or min operator
        if (!rs.next()) throw new AssertionError("No row returned.");
        Object matchValue = rs.getObject("matchValue");
        stmt.close();

        return matchValue;
	}

	protected String getVarChar(int maxStringLength) {
		return "VARCHAR (" + maxStringLength + ")";
	}
}
