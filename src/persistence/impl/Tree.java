package persistence.impl;

import java.util.Iterator;

import persistence.Matcher;
import persistence.RollbackException;
import persistence.impl.matcharg.BinaryMatcher;
import persistence.impl.matcharg.LogicMatcher;
import persistence.impl.matcharg.Internal;
import persistence.impl.matcharg.Leaf;
import persistence.impl.matcharg.MatchOp;
import persistence.impl.matcharg.UnaryMatcher;

public abstract class Tree {

	/*
     * All exceptions (including IllegalArgumentException and NullPointerException) are caught and
     * chained in RollbackException to ensure any active transaction for this thread is rolled back.
     */

	public static Tree createTree(Property[] allBeanProperties, Matcher constraint) throws RollbackException {
        try {
            if (constraint == null) throw new NullPointerException("constraint cannot be null)");

        	if (constraint instanceof UnaryMatcher) {
        		UnaryMatcher arg = (UnaryMatcher) constraint;
        		return new Leaf(allBeanProperties,arg);
        	}

        	if (constraint instanceof BinaryMatcher) {
        		BinaryMatcher arg = (BinaryMatcher) constraint;
        		return new Leaf(allBeanProperties,arg);
        	}

    		LogicMatcher arg = (LogicMatcher) constraint;
    		return new Internal(allBeanProperties,arg);
        } catch (Exception e) {
        	TranImpl.rollbackAndThrow(e);
        	throw new AssertionError("rollbackAndThrow returned (can't happen)");
        }
	}

	protected MatchOp  op = null;

    public Tree(MatchOp op) {
    	this.op = op;
    }

    public MatchOp getOp() { return op; }

    public abstract boolean containsNonPrimaryKeyProps();
    public abstract boolean containsMaxOrMin();

    public abstract Property[] getProperties();
    public abstract Object[]   getValues();

    public abstract Iterator<Leaf> leafIterator();
}
