package persistence;

import persistence.impl.GenericViewDaoImpl;

public class GenericViewDao<T> {
	private GenericViewDaoImpl<T> impl;

	public GenericViewDao(Class<T> beanClass, Manager manager) throws DaoException {
		impl = new GenericViewDaoImpl<T>(beanClass, manager);
	}

	public T[] executeQuery(String sql, Object... args) throws RollbackException {
		return impl.executeQuery(sql, args);
	}
	
	/*
	public String[] getPropertyNames() {
		return impl.getPropertyNames();
	}*/
}
