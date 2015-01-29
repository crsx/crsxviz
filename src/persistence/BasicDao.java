package persistence;

import persistence.impl.BasicDaoImp;

/**
 * This class is used to read and write rows of a database table that correspond
 * to instances of a JavaBean of type T.
 * 
 * The JavaBean must have a public, no-argument constructor
 * 
 * JavaBeans are declared in the usual way with getter and setter methods for the properties.
 * The properties that comprise the primary key (for the table that backs the beans)
 * must be specified with the @PrimaryKey annotation.
 */
public class BasicDao<T> {
	private BasicDaoImp<T> impl;

	/**
	 * Creates a new BasicDao object.
	 *
	 * The constructor will
	 * (1) analyze your bean (to make sure it has all the parts it needs to read and write it),
	 * (2) create the table in SQLite,
	 * (3) compare the table to the bean (if it already there).

	 * @param beanClass the class description of the bean.
	 * @param tableName the name of the table in the database.
	 * @param manaager the manager to use to manage connections to the database.
	 * @throws DaoException if there are any problems, including problems accessing the database, problems with the bean class, etc.
	 * @throws RollbackException 
	 */
	public BasicDao(Class<T> beanClass, String tableName, Manager manager) throws DaoException, RollbackException {
		impl = BasicDaoImp.getInstance(beanClass, tableName, manager);
		if (!impl.tableExists()) {
			impl.createTable();
		}
		impl.validateTable();
	}

	/**
	 * Creates a new row in the table using the values provided the contents of the <tt>bean</tt>.
	 *
	 * @param bean an instance of type.
	 * @throws RollbackException if the work cannot be completed for any one of a number of reasons,
	 * including SQLExceptions, deadlocks, errors accessing the bean, etc.
	 * Any enclosing transaction is rolled back in the process of throwing this exception.
	 * @throws DuplicateKeyException if the
	 */
	public void create(T bean) throws RollbackException {
		impl.create(bean);
	}

    /**
     * Deletes from the table the row with the given primary key.
     *
     * @param primaryKeyValues the values of the properties that comprise the primary key for
     * bean being deleted.
     * @throws RollbackException if there is no bean in the table with this primary key or
     *     if there is an error accessing the database, including IOException or deadlock.
     */
	public void delete(Object... primaryKeyValues) throws RollbackException {
		impl.delete(primaryKeyValues);
	}

    /**
     * Returns the number of rows in the table.
     *
     * @return the number of rows in the table.
     *
     * @throws RollbackException if there is an error accessing the database, including SQLException or deadlock.
     */
	public int getCount() throws RollbackException {
		return impl.getBeanCount();
	}

    /**
     * Searches the table for rows matching the given constraints.
     * Constraints are specified with Matchers which limit properties to
     * values or ranges, such as equals, less-than or greater-than a given value.
     * Operators on strings also include starts-with, ends-with, and contains.
     * A bean is instantiated and returned to hold the values for each row that matches the constraints.
     *
     * If no constraints are specified, all the rows in the table are returned.
     *
     *	Examples of use can be found in unitTests.crsxviz.tests.MatchTest
     *
     * @param constraints zero or more constraints, all of which must be <code>true</code> for each bean
     * returned by this call.
     * @return an array of beans that match the given constraints.  If no beans match the
     * constraints, a zero length array is returned.
     * @throws RollbackException if there is an error accessing the database
     */
	public T[] match(Matcher...constraints) throws RollbackException {
		return impl.match(constraints);
	}

    /**
     * Returns the row in the table with the given primary key.
     *
     * @param primaryKeyValues the values of the properties that comprise the primary key for
     * bean being looked up.
     * @return a reference to an instance of T with the given primary key and values populated from the table.
     * If there is no such row, then null is returned.
     * @throws RollbackException if there is an error accessing the database
     */
	public T read(Object... primaryKeyValues) throws RollbackException {
		return impl.read(primaryKeyValues);
	}
	
    /**
     * Updates the row in the table with the primary key specified by the values in the bean passed in as a parameter.
     * The fields in the row (other than the primary key fields) are set to the values specified in the bean.
	 *
	 * @param bean an instance of type T that contains the values to store in the table.
     * @throws RollbackException if there is an error accessing the database,
     * including SQLException or deadlock.
     */
	public void update(T bean) throws RollbackException {
		impl.update(bean);
	}
}
