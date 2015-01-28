package persistence.impl.matcharg;

import persistence.Matcher;

public class BinaryMatcher extends Matcher {
    private String  fieldName;
    private MatchOp op;
    private Object  fieldValue;

    public BinaryMatcher(String fieldName, MatchOp op, Object fieldValue) {
        this.fieldName  = fieldName;
        this.op         = op;
        this.fieldValue = fieldValue;
    }

    public String  getFieldName()  { return fieldName;  }
    public MatchOp getOp()         { return op;         }
    public Object  getFieldValue() { return fieldValue; }
}
