package persistence.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import persistence.DaoException;
import persistence.Manager;
import persistence.RollbackException;

public class GenericViewDaoImpl<T> {
	protected Manager manager;
	protected Class<T>   beanClass;
	protected Property[] properties;

	public GenericViewDaoImpl(Class<T> beanClass, Manager manager) throws DaoException {
		// Check for null values and throw here (it's less confusing for the caller)
		if (beanClass == null) throw new NullPointerException("beanClass");
		if (manager == null) throw new NullPointerException("manager");

		this.manager = manager;
		this.beanClass = beanClass;
		properties = Property.findProperties(beanClass, manager.getLowerCaseColumnNames());
		
	}
    
	public T[] executeQuery(String sql, Object... args) throws RollbackException {
		Connection con = null;
		try {
			con = transJoin();
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				pstmt.setObject(i+1, args[i]);
			}
			
			ResultSet rs = pstmt.executeQuery();
			
			List<T> beanList = new ArrayList<T>();
			while (rs.next()) {
				T bean = newBean();
				for (Property prop : properties) {
					Object value = rs.getObject(prop.getColumnName());
					//value = fixDate(value);
					setBeanValue(bean, prop, value);
				}
				beanList.add(bean);
			}
			
			rs.close();
			pstmt.close();
			
			T[] beanArray = newArray(beanList.size());
			beanList.toArray(beanArray);
			return beanArray;
		} catch (SQLException e) {
			TranImpl.rollbackAndThrow(con, e);
			throw new AssertionError("rollbackAndThrow returned (can't happen)");
		}
	}
	
	protected Connection transJoin() throws RollbackException, SQLException {
		return (TranImpl.isActive()) ? manager.getTransactionConnection() : manager.getConnection();
	}

    protected Object getBeanValue(Object bean, Property property) throws RollbackException {
        Method getter = property.getGetter();
        try {
            return getter.invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            TranImpl.rollbackAndThrow("Exception when getting "+
                    property+" from bean="+bean,e);
        } 

        throw new AssertionError("Should not get here.");
    }

    @SuppressWarnings("unchecked")
	protected T[] newArray(int size) {
		Object array = java.lang.reflect.Array.newInstance(beanClass,size);
		return (T[]) array;
	}

    protected T newBean() throws RollbackException {
    	T bean = null;
    	try {
    		bean =  beanClass.newInstance();
    	} catch (IllegalAccessException | InstantiationException e) {
    		TranImpl.rollbackAndThrow("Error instantiating " + beanClass.getName(), e);
    	} 
    	return bean;
    }


    protected void setBeanValue(T bean, Property property, Object value) throws RollbackException {
		try {
			Method setter = property.getSetter();
			setter.invoke(bean,value);
		} catch (NullPointerException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
			TranImpl.rollbackAndThrow("Exception when setting "+
                    property+" to value="+value+" for bean="+bean,e);
		} 
	}


}
