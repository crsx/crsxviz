package persistence.impl.matcharg;

import persistence.Matcher;

public class LogicMatcher extends Matcher {
	private MatchOp    op;
	private Matcher[] constraints;

    public LogicMatcher(MatchOp op, Matcher...constraints) {
    	this.op = op;
    	this.constraints = constraints.clone();
    }

    public Matcher[] getArgs() { return constraints.clone(); }
    public MatchOp    getOp()   { return op;                  }
}
