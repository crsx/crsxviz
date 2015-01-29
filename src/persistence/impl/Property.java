package persistence.impl;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import persistence.DaoException;
import persistence.PrimaryKey;



public class Property implements Comparable<Property> {
    private static final Integer INTEGER_ZERO = new Integer(0);
    private static final Long    LONG_ZERO    = new Long(0);
    private static final Float   FLOAT_ZERO   = new Float(0);
    private static final Double  DOUBLE_ZERO  = new Double(0);
    private static final int	 MAX_STR_LEN  = 255;

    private static Class<?> classFor(String s) {
        if (s.equals("java.lang.String"))  return String.class;

        if (s.equals("boolean"))        return boolean.class;
        if (s.equals("byte[]"))         return byte[].class;
        if (s.equals("int"))            return int.class;
        if (s.equals("long"))           return long.class;
        if (s.equals("double"))         return double.class;
        if (s.equals("float"))          return float.class;

        if (s.equals("java.sql.Date"))  return java.sql.Date.class;
        if (s.equals("java.util.Date")) return java.util.Date.class;
        if (s.equals("java.sql.Time"))  return java.sql.Time.class;

        return null;
    }

    private static void isDuplicateProperties(Property[] property) throws DaoException {
        for (int i=0; i<property.length; i++) {
            if (indexOfProperty(property,property[i].getName()) != i) {
                throw new DaoException("Duplicate property names: "+
                        property[indexOfProperty(property,property[i].getName())]+
                        " and "+property[i]);
            }
        }
    }
    
    private static <T> List<String> findPrimaryKeys(Class<T> beanClass) throws DaoException {
    	List<String> nameList = new ArrayList<String>();
    	
    	PrimaryKey annotation = beanClass.getAnnotation(PrimaryKey.class);
    	if (annotation == null) return nameList;
    	
    	String value = annotation.value();
    	String[] names = value.split(",");
    	for (String name : names) nameList.add(name);
    	return nameList;
    }

    public static <T> Property[] findProperties(Class<T> beanClass, boolean lowerCaseColumnNames) throws DaoException {
    	List<String> primaryKeyPropertyNames = findPrimaryKeys(beanClass);
        ArrayList<Property> list = new ArrayList<Property>();

        Method[] methods = beanClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            Class<?>  propType = method.getReturnType();
            Property newProp = null;
            if (methodName.length() > 2 && methodName.startsWith("is") && propType == boolean.class) {
                String propName = methodName.substring(2,3).toLowerCase() + methodName.substring(3);
                newProp = findProperty(propName,propType,beanClass,primaryKeyPropertyNames,lowerCaseColumnNames);
            } else if (methodName.length() > 3 && methodName.startsWith("get")) {
                String propName = methodName.substring(3,4).toLowerCase() + methodName.substring(4);
                newProp = findProperty(propName,propType,beanClass,primaryKeyPropertyNames,lowerCaseColumnNames);
            } else {
            	newProp = null;
            }
            if (newProp != null) list.add(newProp);
        }

        Property[] properties = new Property[list.size()];
        list.toArray(properties);
        setPrimaryKeys(properties, primaryKeyPropertyNames);
        Arrays.sort(properties);
        for (int i=0; i<properties.length; i++) properties[i].propertyNum = i;
        isDuplicateProperties(properties);
        return properties;
    }

    private static Property findProperty(String propName, Class<?> propType, Class<?> beanClass,
								    	   List<String> primaryKeyPropertyNames, boolean  lowerCaseColumnNames) throws DaoException {
        if (propType == void.class) {
        	throw new DaoException("propType can not be null");
        }

        String setterName = "set"+propName.substring(0,1).toUpperCase()+propName.substring(1);
        try {
            beanClass.getMethod(setterName, propType);
            return getInstance(propName, propType, beanClass, primaryKeyPropertyNames.contains(propName), lowerCaseColumnNames);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Property getInstance(String   propertyName, Class<?> type, Class<?> beanClass,
                                        boolean  isPrimaryKeyProperty, boolean  lowerCaseColumnNames) throws DaoException {
        if (type == byte[].class) {
            return new Property(propertyName,type,isPrimaryKeyProperty,lowerCaseColumnNames,beanClass);
        }

        if (classFor(type.getName()) != null) {
            return new Property(propertyName,type,isPrimaryKeyProperty,lowerCaseColumnNames,beanClass);
        }

        throw new DaoException("Cannot map this class type: "+type.getCanonicalName()+" (property name: "+propertyName+").");
    }

    private static int indexOfProperty(Property[] properties, String name) {
        for (int i=0; i<properties.length; i++) {
            if (properties[i].getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public static Property propertyForName(Property[] allProps, String propertyName) {
        int num = indexOfProperty(allProps,propertyName);
        if (num != -1) return allProps[num];
        throw new IllegalArgumentException("No such property: "+propertyName);
    }
    
    private static void setPrimaryKeys(Property[] properties, List<String> priKeyPropNames) throws DaoException {
    	for (int i = 0; i < priKeyPropNames.size(); i++) {
    		boolean found = false;
    		for (Property prop : properties) {
    			if (prop.getName().equalsIgnoreCase(priKeyPropNames.get(i))) {
    				found = true;
    				prop.propertyNum = i;
    			}
    		}
    		if (!found) {
    			throw new DaoException("Could not find getter/setter pair for primary key property: " + priKeyPropNames.get(i));
    		}
    	}
    }

	private String     name;
	private Method     getter;
	private Method     setter;
    private Class<?>   type;
    private boolean    primaryKeyProperty;
    private int        columnMaxStrLen;
    private String     columnName;
    private Class<?>   columnType;
    private int        propertyNum = -1;

	public Property(String   name, Class<?> type, boolean  isPrimaryKeyProperty, boolean  lowerCaseColumnNames,
                       Class<?> beanClass) throws DaoException {
		this.name      = name;
        this.type      = type;
        this.primaryKeyProperty = isPrimaryKeyProperty;

		String capName = name.substring(0,1).toUpperCase() + name.substring(1);

		try {
			getter = beanClass.getMethod("get"+capName);
		} catch (NoSuchMethodException e) {
			if (type != boolean.class) {
				throw new DaoException(beanClass.getName()+" doesn't match table: no get"+capName+"() method.  Drop the table and let BasicDao recreate it.");
			}
			try {
				getter = beanClass.getMethod("is" + capName);
			} catch (NoSuchMethodException e2) {
				throw new DaoException(beanClass.getName()+" doesn't match table: no get"+capName+"() or is"+capName+"() method.  Drop the table and let BasicDao recreate it.");
			}
		}

		if (getter.getReturnType() != type) {
			throw new DaoException(beanClass.getName()+" doesn't match table: get"+capName+"() returns "+getter.getReturnType().getCanonicalName()+" (not "+type.getCanonicalName()+", which is the table's type).  Drop the table and let BasicDao recreate it.");
		}

		try {
			setter = beanClass.getMethod("set" + capName,type);
		} catch (NoSuchMethodException e) {
			throw new DaoException(beanClass.getName()+" doesn't match table: no set"+capName+"("+type.getCanonicalName()+") method.  Drop the table and let BasicDao recreate it.");
		}

        columnMaxStrLen = MAX_STR_LEN;
        columnName = lowerCaseColumnNames ? name.toLowerCase() : name;
        columnType = type;
	}

	public int compareTo(Property other) {
		boolean thisPrimary  = (this.primaryKeyProperty);
		boolean otherPrimary = (other.primaryKeyProperty);

        if (thisPrimary && otherPrimary) {
            return getPropertyNum() - other.getPropertyNum();
        }

        if (thisPrimary)  return -1;
		if (otherPrimary) return 1;

		int c = name.compareTo(other.name);
		if (c != 0) return c;

		return type.getName().compareTo(other.type.getName());
	}

    public int      getColumnMaxStrLen() { return columnMaxStrLen; }
    public String   getColumnName()      { return columnName;   }
    public Class<?> getColumnType()      { return columnType;   }

    public Object defaultValue() {
        if (type == int.class) return INTEGER_ZERO;
        if (type == long.class) return LONG_ZERO;
        if (type == boolean.class) return Boolean.FALSE;
        if (type == float.class) return FLOAT_ZERO;
        if (type == double.class) return DOUBLE_ZERO;
        return null;

    }

	public Method   getGetter()      { return getter;    }
	public String   getName()        { return name;      }

    public int getPropertyNum() {
        if (propertyNum < 0) throw new AssertionError("getColumnNum() called before setColumnNum(): "+this);
        return propertyNum;
    }

    public Method   getSetter()      { return setter;    }
    public Class<?> getType()        { return type;      }

	public int hashCode() {
		return name.hashCode();
	}

	public boolean isInstance(Object value) {
		if (type == boolean.class) return value instanceof Boolean;
		if (type == double.class)  return value instanceof Double;
		if (type == float.class)   return value instanceof Float;
		if (type == int.class)     return value instanceof Integer;
		if (type == long.class)    return value instanceof Long;
		return type.isInstance(value);
	}

    public boolean isNullable() {
        if (type == boolean.class) return false;
        if (type == double.class)  return false;
        if (type == float.class)   return false;
        if (type == int.class)     return false;
        if (type == long.class)    return false;
        return true;
    }

    public boolean isPrimaryKey() { return primaryKeyProperty; }
}
