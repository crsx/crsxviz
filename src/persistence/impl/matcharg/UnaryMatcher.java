package persistence.impl.matcharg;

import persistence.Matcher;

public class UnaryMatcher extends Matcher {
    private String  keyName;
    private MatchOp op;

    public UnaryMatcher(String keyName, MatchOp op) {
        this.keyName  = keyName;
        this.op       = op;
    }

    public String  getKeyName()  { return keyName;  }
    public MatchOp getOp()       { return op;       }
}
