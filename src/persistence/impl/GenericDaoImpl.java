package persistence.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import persistence.DaoException;
import persistence.Manager;
import persistence.Matcher;
import persistence.RollbackException;
import persistence.Transaction;
import persistence.impl.matcharg.MatchOp;
import persistence.impl.matcharg.Internal;
import persistence.impl.matcharg.Leaf;

public abstract class GenericDaoImpl<T> extends GenericViewDaoImpl<T> {
	
    public static <T> GenericDaoImpl<T> getInstance(Class<T> beanClass, String tableName, Manager manager) throws DaoException {
    	return new SqliteImpl<T>(beanClass, tableName, manager);
    }

	protected abstract Object findExtremeValue(Connection con, Leaf arg, String tableName) throws SQLException;
	protected abstract String getLikeOperator();
	protected abstract String getNullSafeEqualsOperator();

	protected abstract String getBlobTypeDeclaration();
	protected abstract String getDateTimeTypeDeclaration();
	protected abstract String getDefaultSchemaName();
	protected abstract String getSerialTypeDeclaration();
	protected abstract String getBigSerialTypeDeclaration();
	protected abstract String getVarChar(int maxStringLength);
 
    
    // Initialized by constructor
    private   Property[] primaryKeyProperties;
    private   Property[] nonPrimaryKeyProperties;

    protected String     tableName;
    private   String     schemaName;
    private   String     tableNameWithoutSchema;
     
    protected String     columnNamesCommaSeparated;
    private   String     columnQuestionsCommaSeparated;
    private   String     nonPrimaryKeyColumnNamesEqualsQuestionsCommaSeparated;
    protected String     nonPrimaryKeyColumnQuestionsCommaSeparated;
    private   String     primaryKeyColumnNamesEqualsQuestionsAndSeparated;
    private   String     primaryKeyColumnNamesCommaSeparated;


	protected GenericDaoImpl(Class<T> beanClass, String tableName, Manager manager) throws DaoException {
		super(beanClass, manager);

		// Check for null values and throw here (it's less confusing for the caller)
		if (tableName == null) throw new NullPointerException("tableName");
		
        this.tableName = tableName.toLowerCase();
        
    	primaryKeyProperties    = getProperties(true);
    	nonPrimaryKeyProperties = getProperties(false);
		
    	if (primaryKeyProperties.length == 0) throw new DaoException("No primary key properties specified in the bean: " + beanClass.getName());

        if (this.tableName.contains(".")) {
        	int dotPos = this.tableName.indexOf('.');
        	schemaName = this.tableName.substring(0,dotPos);
        	tableNameWithoutSchema = this.tableName.substring(dotPos+1);
        } else {
        	schemaName = getDefaultSchemaName();
        	tableNameWithoutSchema = this.tableName;
        }

        columnNamesCommaSeparated = concatNameSep(properties, ", ", "");
        columnQuestionsCommaSeparated = concatToken(properties, "?", ", ");
        nonPrimaryKeyColumnNamesEqualsQuestionsCommaSeparated = concatNameSep(nonPrimaryKeyProperties, "=?, ", "=?");
        nonPrimaryKeyColumnQuestionsCommaSeparated = concatToken(nonPrimaryKeyProperties, "?", ", ");;
        primaryKeyColumnNamesEqualsQuestionsAndSeparated = concatNameSep(primaryKeyProperties, "=? AND ", "=?");
        primaryKeyColumnNamesCommaSeparated = concatNameSep(primaryKeyProperties, ", ", "");
    }
	
	private String concatNameSep(Property[] props, String separator, String suffix) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < props.length; i++) {
			if (i > 0) sb.append(separator);
			sb.append(props[i].getColumnName());
		}
		sb.append(suffix);
		return sb.toString();
	}

	private String concatToken(Property[] props, String tokenInPlaceOfName, String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < props.length; i++) {
			if (i > 0) sb.append(separator);
			sb.append(tokenInPlaceOfName);
		}
		return sb.toString();
	}

    public synchronized void createTable() throws DaoException {
        StringBuilder b = new StringBuilder();
        b.append("create table ");
        b.append(tableName);
        b.append(" (");
        for (int i=0; i<properties.length; i++) {
            if (i > 0) b.append(", ");
            Property prop = properties[i];
            if (prop.isPrimaryKey() && prop.getType() == int.class && primaryKeyProperties.length == 1) {
                b.append(prop.getName());
                b.append(' ');
                b.append(getSerialTypeDeclaration());
            } else if (prop.isPrimaryKey() && prop.getType() == long.class && primaryKeyProperties.length == 1) {
                b.append(prop.getName());
                b.append(' ');
                b.append(getBigSerialTypeDeclaration());
            } else {
                b.append(prop.getColumnName());
                b.append(' ');
                b.append(javaToSql(prop.getColumnType(),prop,prop.getColumnMaxStrLen()));
            }
        }

        if (primaryKeyProperties.length > 0) {
	        b.append(", PRIMARY KEY(");
	        b.append(primaryKeyColumnNamesCommaSeparated);
	        b.append(')');
        }

        b.append(')');

        Connection con = null;
        try {
        	con = manager.getConnection();
            Statement stmt = con.createStatement();
            stmt.executeUpdate(b.toString());
            stmt.close();
        } catch (SQLException | RollbackException e) {
            try { if (con != null) con.close(); } catch (SQLException e2) { e.printStackTrace(); }
            throw new DaoException("Error creating table \""+tableName+"\": "+e.getMessage(),e);
        }
    }

    /**
     * Deletes this table from the database.
     * Also deletes any auxiliary tables (that contain array data).
     * This call does not validate the table.
     * This call does not throw an exception if the table does not exist.
     *
     * @throws DaoException if there is an error connecting to the database.
     *
    public synchronized void deleteTable() throws DaoException {
        Connection con = null;
        try {
        	con = manager.getConnection();
            Statement stmt = con.createStatement();
            String sql = "DROP TABLE " + tableName;
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException | RollbackException e) {
            try {
            	if (con != null) con.close();
            } catch (SQLException e2) {
            	e2.printStackTrace();
            }
            throw new DaoException(e);
        }
    }*/

    /**
     * Checks to see if this table exists in the database.
     *
     * @return true if the table exists.
     * @throws BeanFactoryException if there is an error connecting to the database.
     */
    public boolean tableExists() throws DaoException {
        Connection con = null;
        try {
        	con = manager.getConnection();
        	DatabaseMetaData metaData = con.getMetaData();
            ResultSet rs = metaData.getTables(null, schemaName, tableNameWithoutSchema, null);

            boolean answer = false;
            while (rs.next() && !answer) {
                String s = rs.getString("TABLE_NAME");
            	boolean isWindows = ( File.separatorChar == '\\');
            	if (isWindows) {
                    if (tableNameWithoutSchema.equalsIgnoreCase(s)) answer = true;
                } else {
                    if (tableNameWithoutSchema.equals(s)) answer = true;
                }
            }

            rs.close();

            return answer;
        } catch (SQLException | RollbackException e) {
            try { if (con != null) con.close(); } catch (SQLException e2) {  }
            throw new DaoException(e);
        }
    }

    public void create(T bean) throws RollbackException {
		Connection con = null;
		try {
			con = transJoin();
			
            String sql = "INSERT INTO " + tableName + " (" + columnNamesCommaSeparated +
        		") values (" + columnQuestionsCommaSeparated + ")";
            PreparedStatement pstmt = con.prepareStatement(sql);
            for (int i = 0; i < properties.length; i++) {
            	Object value = getBeanValue(bean, properties[i]);
            	pstmt.setObject(i + 1, value);
            }
            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            if (e.getMessage().startsWith("Duplicate")) {
                TranImpl.rollbackAndThrow(con, new DaoException(e.getMessage()));
            }
            TranImpl.rollbackAndThrow(con, e);
            throw new AssertionError("executeRollback returned");
        } 
    }
    
    public void delete(Object... primaryKeyValues) throws RollbackException {
        validatePriKeys(primaryKeyValues);   // throws RollbackException if problems

		Connection con = null;
		try {
			con = transJoin();

			String whereClause = " WHERE " + primaryKeyColumnNamesEqualsQuestionsAndSeparated;
            String sql = "DELETE FROM " + tableName + whereClause;
            PreparedStatement pstmt = con.prepareStatement(sql);
            for (int i = 0; i < primaryKeyValues.length; i++) {
            	pstmt.setObject(i+1, primaryKeyValues[i]);
            }

            int num = pstmt.executeUpdate();
            pstmt.close();

            if (num != 1) {
                StringBuilder b = new StringBuilder();
                for (int i=0; i<primaryKeyValues.length; i++) {
                    if (i > 0) b.append(",");
                    b.append(primaryKeyValues[i]);
                }
                if (num == 0) throw new RollbackException("No row with primary key = \""+b+"\".");
                throw new RollbackException("AssertionError: There are "+num+" rows with primary key = \""+b+"\".");
            }

        } catch (Exception e) {
        	TranImpl.rollbackAndThrow(con, e);
        }
    }
	
    public int getBeanCount() throws RollbackException {
		Connection con = null;
		try {
			con = transJoin();
			
			Statement stmt = con.createStatement();
            String sql = "SELECT COUNT(*) FROM " + tableName;
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int answer = rs.getInt(1);
            stmt.close();

            return answer;
        } catch (Exception e) {
			TranImpl.rollbackAndThrow(con, e);
			throw new AssertionError("rollbackAndThrow returned (can't happen)");
        }
    }
    
    public T[] match(Matcher...constraints) throws RollbackException {
        Tree sepMatchArgs = Tree.createTree(properties,Matcher.and(constraints)); // throws RollbackException in case of problems
        if (!TranImpl.isActive() && sepMatchArgs.containsMaxOrMin()) {
            Transaction.begin();
            T[] answer = sqlMatch(sepMatchArgs);  // throws RollbackException in case of problems
            Transaction.commit();
            return answer;
        }

        return sqlMatch(sepMatchArgs);           // throws RollbackException in case of problems
    }

    public T read(Object... primaryKeyValues) throws RollbackException {
        validatePriKeys(primaryKeyValues);   // throws RollbackException in case of problems

        try {
	        Matcher[] matchArgs = new Matcher[primaryKeyProperties.length];
	        for (int i=0; i<primaryKeyProperties.length; i++) {
	            matchArgs[i] = Matcher.equals(primaryKeyProperties[i].getName(),primaryKeyValues[i]);
	        }

	        T[] list = match(matchArgs);
	        if (list.length == 0) return null;
	        if (list.length == 1) return list[0];

	        StringBuilder b = new StringBuilder();
	        for (int i=0; i<primaryKeyValues.length; i++) {
	            if (i>0) b.append(',');
	            b.append(primaryKeyValues[i]);
	        }
	        throw new RollbackException("AssertionError: "+list.length+" records with same primary key: "+b);
        } catch (Exception e) {
	        TranImpl.rollbackAndThrow(e);
	        throw new AssertionError("rollbackAndThrow returned");
        }
    }

    public void update(T bean) throws RollbackException {
		Connection con = null;
		try {
			con = transJoin();
			
            String sql = "UPDATE " + tableName +
            		" SET " + nonPrimaryKeyColumnNamesEqualsQuestionsCommaSeparated +
            		" WHERE " + primaryKeyColumnNamesEqualsQuestionsAndSeparated;
            PreparedStatement pstmt = con.prepareStatement(sql);
            int i = 0;
            for (Property p : properties) {
            	if (!p.isPrimaryKey()) {
            		i = i + 1;
            		Object value = getBeanValue(bean, p);
            		pstmt.setObject(i, value);
            	}
            }
            for (Property p : primaryKeyProperties) {
        		i = i + 1;
        		Object value = getBeanValue(bean, p);
        		pstmt.setObject(i, value);
            }
            int count = pstmt.executeUpdate();
            pstmt.close();
            
            if (count != 1) throw new RollbackException("AssertionError: Incorrect number of rows updated: " + count);

        } catch (Exception e) {
            TranImpl.rollbackAndThrow(con, e);
        }
    }
    
    private String createSql(Tree argTree) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM ");
        sql.append(tableName);

        String whereTest = createWhere(argTree);
        if (whereTest.length() > 0) {
        	sql.append(" WHERE ");
        	sql.append(whereTest);
        }

        return sql.toString();
    }
    
    private String createWhere(Tree argTree) {
        MatchOp op = argTree.getOp();

        if (argTree instanceof Internal) {
    		Internal internalNode = (Internal) argTree;
    		List<Tree> subNodes = internalNode.getSubNodes();
    		StringBuffer sql = new StringBuffer();
    		for (Tree subNode : subNodes) {
    			if (sql.length() > 0) {
    				if (op == MatchOp.AND) sql.append(" AND ");
    				if (op == MatchOp.OR)  sql.append(" OR ");
    			}
    			sql.append('(');
    			sql.append(createWhere(subNode));
    			sql.append(')');
    		}
    		return sql.toString();
    	}

        if (op == null) return "NULL is not ?";  // op is null when a max or min constraint match any rows

		Leaf leaf = (Leaf) argTree;
        String keyName = leaf.getProperty().getName();
        switch (op) {
            case EQUALS:
                return keyName + " " + getNullSafeEqualsOperator() + " ?";
            case NOT_EQUALS:
                return "NOT (" + keyName + " " + getNullSafeEqualsOperator() + " ?)";
            case GREATER:
                return keyName + " > ?";
            case GREATER_OR_EQUALS:
                return keyName + " >= ?";
            case LESS:
                return keyName + " < ?";
            case LESS_OR_EQUALS:
                return keyName + " <= ?";
            case CONTAINS:
            case STARTS_WITH:
            case ENDS_WITH:
                return keyName + " " + getLikeOperator() + " ?";
            case EQUALS_IGNORE_CASE:
            case CONTAINS_IGNORE_CASE:
            case STARTS_WITH_IGNORE_CASE:
            case ENDS_WITH_IGNORE_CASE:
            	return keyName + " " + getLikeOperator() + " ?";
            case MAX:
            case MIN:
                throw new AssertionError(op+" in constraints should have be converted to EQUALS at this point");
            default:
                throw new AssertionError("Unknown op: "+op);
        }
    }

	private Property[] getProperties(boolean primaryKey) {
		List<Property> list = new ArrayList<Property>();
		for (Property p : properties) {
			if (p.isPrimaryKey() == primaryKey) list.add(p);
		}
		return list.toArray(new Property[list.size()]);
	}
	
    private void standardizeMaxMin(Tree argTree) throws RollbackException {
    	if (!TranImpl.isActive()) throw new AssertionError("Caller should have started a transaction");

    	Iterator<Leaf> iter = argTree.leafIterator();
    	while (iter.hasNext()) {
    		Leaf arg = iter.next();
			MatchOp op = arg.getOp();

    		if (op == MatchOp.MAX || op == MatchOp.MIN) {
    	        Connection con = manager.getConnection();

    	        try {
    	        	Object matchValue = findExtremeValue(con, arg, tableName);

        	        if (matchValue == null) {
        	        	arg.fixConstraint(null,null);
        	        } else {
        	        	arg.fixConstraint(MatchOp.EQUALS,matchValue);
        	        }
    	        } catch (SQLException e) {
    	            TranImpl.rollbackAndThrow(con, e);
    	        }
    		}
    	}
    }
    
    private void fixPartialMatch(Tree argTree) {
    	Iterator<Leaf> iter = argTree.leafIterator();
    	while (iter.hasNext()) {
    		Leaf arg = iter.next();

    		Object value = arg.getValue();
    		if (value instanceof String) {
    			String strValue = (String) value;
	    		MatchOp op = arg.getOp();
	            switch (op) {
	                case CONTAINS:
	                case CONTAINS_IGNORE_CASE:
	    	        	arg.fixConstraint(op,'%'+strValue+'%');
	                    break;
	                case STARTS_WITH:
	                case STARTS_WITH_IGNORE_CASE:
	    	        	arg.fixConstraint(op,strValue+'%');
	                    break;
	                case ENDS_WITH:
	                case ENDS_WITH_IGNORE_CASE:
	    	        	arg.fixConstraint(op,'%'+strValue);
	                    break;
	                default:
	                    // Do nothing
	            }
    		}
        }
    }
    
    private String javaToSql(Class<?> javaType, Property prop, int maxStringLength) throws DaoException {
        StringBuffer sql = new StringBuffer();

        if (javaType.isEnum())                sql.append(getVarChar(maxStringLength));

        if (javaType == String.class)         sql.append(getVarChar(maxStringLength));

        if (javaType == java.sql.Date.class)  sql.append("INTEGER");
        if (javaType == java.util.Date.class) sql.append(getDateTimeTypeDeclaration());
        if (javaType == java.sql.Time.class)  sql.append("INTEGER");
        if (javaType == java.sql.Timestamp.class) sql.append("CURRENT_TIMESTAMP");

        if (javaType == byte[].class)         sql.append(getBlobTypeDeclaration());

        if (sql.length() > 0) {
            if (prop.isPrimaryKey()) sql.append(" NOT NULL");
            return sql.toString();
        }

        if (javaType == boolean.class)        sql.append("INTEGER");
        if (javaType == double.class)         sql.append("INTEGER");
        if (javaType == float.class)          sql.append("REAL");
        if (javaType == int.class)            sql.append("INTEGER");
        if (javaType == long.class)           sql.append("INTEGER");

        if (sql.length() > 0) {
            if (prop.isPrimaryKey()) {
                sql.append(" NOT NULL");
            } else if (javaType == boolean.class) {
                sql.append(" NOT NULL DEFAULT FALSE");
            } else {
                sql.append(" NOT NULL DEFAULT 0");
            }
            return sql.toString();
        }

        throw new DaoException("Cannot find Java type: "+javaType.getCanonicalName());
    }
    
    private Class<?> sqlToJava(int sqlType) throws DaoException {
        if (sqlType == Types.VARCHAR)   return String.class;
        if (sqlType == Types.VARBINARY) return String.class;

        if (sqlType == Types.BIT)       return int.class;
        if (sqlType == Types.BOOLEAN)   return int.class;
        if (sqlType == Types.TINYINT)   return int.class;

        if (sqlType == Types.INTEGER)   return int.class;
        if (sqlType == Types.BIGINT)    return long.class;

        if (sqlType == Types.DOUBLE)    return double.class;
        if (sqlType == Types.REAL)      return float.class;

        if (sqlType == Types.DATE)      return java.sql.Date.class;
        if (sqlType == Types.TIME)      return java.sql.Time.class;
        if (sqlType == Types.TIMESTAMP) return java.sql.Timestamp.class;

        if (sqlType == Types.LONGVARBINARY)  return byte[].class;
        if (sqlType == Types.BLOB)           return byte[].class;
        if (sqlType == Types.BINARY)         return byte[].class;

        throw new DaoException("Cannot find SQL type: "+sqlType);
    }

    private T[] sqlMatch(Tree argTree) throws RollbackException {
    	
    	try {
	    	if (argTree.containsMaxOrMin()) {
		    	standardizeMaxMin(argTree);
	    	}

	        String sql = createSql(argTree);
	        fixPartialMatch(argTree);
	        return executeQuery(sql, (Object[]) argTree.getValues());
    	} catch (Exception e) {
    		TranImpl.rollbackAndThrow(e);
    		throw new AssertionError("rollbackAndThrow() returned");
    	}
    }

    private void validatePriKeys(Object[] keyValues) throws RollbackException {

        try {
            if (keyValues == null) throw new NullPointerException("keyValues");

            if (primaryKeyProperties.length != keyValues.length) {
                throw new IllegalArgumentException("Wrong number of key values: "+keyValues.length+" (should be "+primaryKeyProperties.length+")");
            }

            for (int i = 0; i < primaryKeyProperties.length; i++) {
                if (keyValues[i] == null) {
                    throw new NullPointerException("Primary key value cannot be null: property=" + primaryKeyProperties[i].getName());
                }

                if (!primaryKeyProperties[i].isInstance(keyValues[i])) {
                    throw new IllegalArgumentException("Key value for property " + primaryKeyProperties[i].getName() +
                    		" is not instance of " + primaryKeyProperties[i].getType() + ".  Rather it is " + keyValues[i].getClass());
                }
            }
        } catch (Exception e) {
            TranImpl.rollbackAndThrow(e);
        }
    }

    public void validateTable() throws DaoException, RollbackException {
        Connection con = null;
        try {
            con = manager.getConnection();
        	DatabaseMetaData metaData = con.getMetaData();
            ColumnList columnList = new ColumnList(metaData,schemaName,tableNameWithoutSchema);

            Iterator<Column> columnIter = columnList.iterator();

            for (Property prop : properties) {
                if (!columnIter.hasNext()) throw new DaoException("Table="+tableName+" is missing column: "+prop.getColumnName()+" that backs "+prop);
                Column column = columnIter.next();
                if (!column.name.equals(prop.getColumnName())) {
                    throw new DaoException("Column #"+column.position+" should have name "+prop.getColumnName()+" (but is instead "+column.name+")");
                }

                if (prop.isPrimaryKey() && !column.isPrimaryKey) {
                    throw new DaoException("Table "+tableName+" does not indicate column \""+column.name+"\" as part of the primary key (and it should)");
                }

                if (column.isPrimaryKey && !(prop.isPrimaryKey())) {
                    throw new DaoException("Table "+tableName+" does indicates column \""+column.name+"\" as part of the primary key (and it should not)");
                }

                Class<?> dbType = sqlToJava(column.sqlType);
                if (dbType == null) throw new DaoException("Table="+tableName+", "+column.name+": do not know how to map this database type: "+column.sqlType);
                if (dbType != prop.getColumnType()) throw new DaoException("Table="+tableName+", column="+column.name+": bean & DB types do not match: beanType="+prop.getColumnType()+", DBType="+dbType);

                if (column.isPrimaryKey) {
                    if(!column.isNonNull) throw new DaoException("Table="+tableName+", "+column.name+": database column allows nulls for this type (and should not because it's part of the primary key)");
                } else if (prop.defaultValue() == null && column.isNonNull) {
                    throw new DaoException("Table="+tableName+", "+column.name+": database column does not allow nulls for this type (and should because of it's type: "+dbType+")");
                }
            }

            if (columnIter.hasNext()) {
                Column column = columnIter.next();
                throw new DaoException("Column ("+column.name+") without corresponding bean property");
            }
            
        } catch (SQLException e) {
            try { if (con != null) con.close(); } catch (SQLException e2) {  }
            throw new DaoException(e);
        } catch (DaoException e) {
            try { if (con != null) con.close(); } catch (SQLException e2) {  }
            throw e;
        } catch (RollbackException e) {
            throw e;
        }
    }
    
    private static class Column {
        String  name;
        int     sqlType;
        boolean isNonNull;
        boolean isPrimaryKey;
        int     position;

        public String toString() {
        	StringBuilder sb = new StringBuilder("Column#");
        	sb.append(position).append('(').append(name);
        	if (isPrimaryKey) sb.append(", primary key");
        	if (isNonNull) sb.append(", non null");
        	sb.append(", ").append(sqlType).append(')');
        	return sb.toString();
        }
    }

    private static class ColumnList {
        ArrayList<Column> list = new ArrayList<Column>();

        ColumnList(DatabaseMetaData metaData, String schemaName, String tableNameWithoutSchema) throws SQLException {
        	ResultSet rs = metaData.getColumns(null,schemaName,tableNameWithoutSchema,null);
            int pos = 0;
            while (rs.next()) {
                Column c = new Column();
                c.name = rs.getString("COLUMN_NAME");
                c.sqlType = rs.getInt("DATA_TYPE");
                c.isNonNull = rs.getString("IS_NULLABLE").equals("NO");
                c.isPrimaryKey = false; // Will update below if primary key column
                pos++;
                c.position = pos;
                list.add(c);
            }
            rs.close();

            rs = metaData.getPrimaryKeys(null,schemaName,tableNameWithoutSchema);
        	while (rs.next()) {
        		String columnName = rs.getString("COLUMN_NAME");
        		for (Column col : list) {
        			if (col.name.equalsIgnoreCase(columnName)) {
        				col.isPrimaryKey = true;
        			}
        		}
        	}
            rs.close();

            Collections.sort(list,new Comparator<Column>() {
            	public int compare(Column c1, Column c2) { return c1.position - c2.position; }
            });
        }

        Iterator<Column> iterator() { return list.iterator(); }
    }

 }
