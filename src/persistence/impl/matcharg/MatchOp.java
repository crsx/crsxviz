package persistence.impl.matcharg;

public enum MatchOp {
    // Valid for comparing any types, except arrays
        EQUALS,
        NOT_EQUALS,

    // Valid for comparing numbers, Dates, or Strings
        GREATER,
        GREATER_OR_EQUALS,
        LESS,
        LESS_OR_EQUALS,

    // Valid for matching String properties, only
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        EQUALS_IGNORE_CASE,
        CONTAINS_IGNORE_CASE,
        STARTS_WITH_IGNORE_CASE,
        ENDS_WITH_IGNORE_CASE,

    // Valid for matching the max/min values of numbers, Dates or Strings
        MAX,
        MIN,

    // Logical ops valid only for combining other ops
        OR,
        AND;

/*
    public String toString() {
    	return getClass().getSimpleName()+"."+super.toString();
    }
*/
}
