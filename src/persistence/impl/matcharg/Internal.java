package persistence.impl.matcharg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import persistence.Matcher;
import persistence.RollbackException;
import persistence.impl.Tree;
import persistence.impl.Property;


public class Internal extends Tree  {
	private List<Tree> subNodes = new ArrayList<Tree>();

    public Internal(Property[] allBeanProperties, LogicMatcher arg) throws RollbackException {
    	super(arg.getOp());
    	for (Matcher subConstraint : arg.getArgs()) {
    		subNodes.add(Tree.createTree(allBeanProperties,subConstraint));
    	}
    }

    public boolean containsMaxOrMin() {
    	for (Tree subNode : subNodes) {
    		if (subNode.containsMaxOrMin()) return true;
    	}
    	return false;
    }

    public boolean containsNonPrimaryKeyProps() {
    	for (Tree subNode : subNodes) {
    		if (subNode.containsNonPrimaryKeyProps()) return true;
    	}
    	return false;
    }

    public List<Tree> getSubNodes() { return subNodes; }

    public Iterator<Leaf> leafIterator() {
    	return new MyLeafIterator(subNodes);
    }

    public Object[] getValues() {
    	List<Object> list = new ArrayList<Object>();
    	for (Tree subNode : subNodes) {
    		list.addAll(Arrays.asList(subNode.getValues()));
    	}
    	return list.toArray(new Object[list.size()]);
    }

    private static class MyLeafIterator implements Iterator<Leaf> {
    	private List<Tree> subNodes;
    	private Iterator<Leaf> subIter = null;
    	private int pos = 0;

    	public MyLeafIterator(List<Tree> subNodes) {
    		this.subNodes = subNodes;
    		if (subNodes.size() > 0) subIter = subNodes.get(0).leafIterator();
    	}

    	public boolean hasNext() {
    		while (pos < subNodes.size()) {
    			if (subIter.hasNext()) return true;
    			pos++;
        		if (pos < subNodes.size()) subIter = subNodes.get(pos).leafIterator();
    		}
    		return false;
    	}

    	public Leaf next() {
    		if (!hasNext()) throw new NoSuchElementException();
    		return subIter.next();
    	}

    	public void remove() {
    		throw new UnsupportedOperationException();
    	}
    }
}
