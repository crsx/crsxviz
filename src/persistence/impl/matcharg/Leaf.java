package persistence.impl.matcharg;

import java.util.Iterator;
import java.util.NoSuchElementException;

import persistence.impl.Tree;
import persistence.impl.Property;


public class Leaf extends Tree {
	private Property property;
	private Object   matchValue    = null;


    public Leaf(Property[] allBeanProperties, UnaryMatcher arg) {
    	super(arg.getOp());

        property = Property.propertyForName(allBeanProperties,arg.getKeyName());

		if (!isNumber() && !isDate() && !isString()) {
    		throw new IllegalArgumentException(op+" cannot be applied to this property type: "+property);
		}
    }

    public Leaf(Property[] allBeanProperties, BinaryMatcher arg) {
    	super(arg.getOp());

        property = Property.propertyForName(allBeanProperties,arg.getFieldName());

        matchValue = arg.getFieldValue();

		matchingTypeCheck();

        switch (op) {
        	case EQUALS:
        	case NOT_EQUALS:
        		break;
        	case GREATER:
        	case GREATER_OR_EQUALS:
        	case LESS:
        	case LESS_OR_EQUALS:
        		if (!isNumber() && !isDate() && !isString()) {
            		throw new IllegalArgumentException(op+" cannot be applied to this property type: "+property);
        		}
        		break;
        	case CONTAINS:
        	case STARTS_WITH:
        	case ENDS_WITH:
        	case EQUALS_IGNORE_CASE:
        	case CONTAINS_IGNORE_CASE:
        	case STARTS_WITH_IGNORE_CASE:
        	case ENDS_WITH_IGNORE_CASE:
        		if (!isString()) {
            		throw new IllegalArgumentException(op+" cannot be applied to this property type: "+property);
        		}
        		break;
        	default:
        		throw new AssertionError("Unknown op: "+op);
        }
    }

    public void fixConstraint(MatchOp newOp, Object newValue) {
    	op = newOp;
    	matchValue = newValue;
    }

	public Property   getProperty()   { return property; }
	public Property[] getProperties() { return new Property[] { property }; }
	public Object     getValue()      { return matchValue;    }
	public Object[]   getValues()     { return new Object[]   { matchValue    }; }

    public Iterator<Leaf> leafIterator() {
    	return new MyLeafIterator(this);
    }

    public boolean containsMaxOrMin() {
    	if (op == MatchOp.MAX) return true;
    	if (op == MatchOp.MIN) return true;
    	return false;
    }

    public boolean containsNonPrimaryKeyProps() {
    	return !property.isPrimaryKey();
    }

    private boolean isDate() {
    	Class<?> c = property.getType();
    	if (c == java.util.Date.class) return true;
    	if (c == java.sql.Date.class)  return true;
    	if (c == java.sql.Time.class)  return true;
    	return false;
    }

    private boolean isNumber() {
    	Class<?> c = property.getType();
    	if (c == float.class)  return true;
    	if (c == int.class)    return true;
    	if (c == double.class) return true;
    	if (c == long.class)   return true;
    	return false;
    }

    private boolean isString() {
    	return property.getType() == String.class;
    }

    private void matchingTypeCheck() {
        if (property.isPrimaryKey() && matchValue == null) throw new IllegalArgumentException("Primary key constraint value cannot be null: property="+property.getName());
        if (matchValue != null && !property.isInstance(matchValue)) throw new IllegalArgumentException("Constraint value for property "+property.getName()+" is not instance of "+property.getType()+".  Rather it is "+matchValue.getClass());
        if (matchValue == null && !property.isNullable()) throw new IllegalArgumentException("Constraint value for property "+property.getName()+" cannot be null");
    }

    private static class MyLeafIterator implements Iterator<Leaf> {
    	private Leaf node;

    	public MyLeafIterator(Leaf node) {
    		this.node = node;
    	}

    	public boolean hasNext() {
    		return node != null;
    	}

    	public Leaf next() {
    		if (node == null) throw new NoSuchElementException();
    		Leaf answer = node;
    		node = null;
    		return answer;
    	}

    	public void remove() {
    		throw new UnsupportedOperationException();
    	}
    }
}
