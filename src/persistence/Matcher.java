package persistence;

import persistence.impl.matcharg.BinaryMatcher;
import persistence.impl.matcharg.LogicMatcher;
import persistence.impl.matcharg.MatchOp;
import persistence.impl.matcharg.UnaryMatcher;

/**
 * A class to specify constraints when matching beans.  Use with the GenericDao.match() method, which generates
 * a SQL SELECT call on the underlying table.  The Matcher parameters are converted into the WHERE clause
 * for the SELECT so as to constrain which rows are returned.
 * 
 * For example, given a typical User Bean
 * this call would return all users with password equal to "admin":
 * User[] array = dao.match(Matcher.equals("password", "admin"));
 *
 */
public abstract class Matcher {
	protected Matcher() {}

	protected abstract MatchOp getOp();

	/**
	 * Logical AND operator for use with the GenericDao.match() method.
	 * Takes as parameters a variable number of Matcher constraints,
	 * all of which must evaluate to true for a row to be returned by GenericDao.match().
	 * For example, using the User bean defined above,
	 * this match call would return all users with first name George and last name Bush
	 *     User[] array = dao.match(
	 *                          Matcher.and(
	 *                                Matcher.equals("firstName", "George"),
	 *                                Matcher.equals("lastName",  "Bush")));
	 * @param constraints zero or more other Matcher parameters
	 * @return a Matcher which evaluates true for a row if all the constraint arguments evaluate to true for that row
	 */
    public static Matcher and(Matcher...constraints) {
    	return new LogicMatcher(MatchOp.AND, constraints);
    }

    public static Matcher contains(String fieldName, String s) {
    	return new BinaryMatcher(fieldName, MatchOp.CONTAINS, s);
    }

    public static Matcher containsIgnoreCase(String fieldName, String s) {
    	return new BinaryMatcher(fieldName, MatchOp.CONTAINS_IGNORE_CASE, s);
    }

    public static Matcher endsWith(String fieldName, String ending) {
    	return new BinaryMatcher(fieldName, MatchOp.ENDS_WITH, ending);
    }

    public static Matcher endsWithIgnoreCase(String fieldName, String ending) {
    	return new BinaryMatcher(fieldName, MatchOp.ENDS_WITH_IGNORE_CASE, ending);
    }

    public static Matcher equals(String fieldName, Object matchValue) {
    	return new BinaryMatcher(fieldName, MatchOp.EQUALS, matchValue);
    }

    public static Matcher equalsIgnoreCase(String keyName, String matchValue) {
    	return new BinaryMatcher(keyName,MatchOp.EQUALS_IGNORE_CASE,matchValue);
    }

    public static Matcher greaterThan(String keyName, Object matchValue) {
    	return new BinaryMatcher(keyName,MatchOp.GREATER,matchValue);
    }

    public static Matcher greaterThanOrEqualTo(String keyName, Object matchValue) {
    	return new BinaryMatcher(keyName,MatchOp.GREATER_OR_EQUALS,matchValue);
    }

    public static Matcher lessThan(String keyName, Object matchValue) {
    	return new BinaryMatcher(keyName,MatchOp.LESS,matchValue);
    }

    public static Matcher lessThanOrEqualTo(String keyName, Object matchValue) {
    	return new BinaryMatcher(keyName,MatchOp.LESS_OR_EQUALS,matchValue);
    }

    public static Matcher max(String keyName) {
    	return new UnaryMatcher(keyName,MatchOp.MAX);
    }

    public static Matcher min(String keyName) {
    	return new UnaryMatcher(keyName,MatchOp.MIN);
    }

    public static Matcher notEquals(String fieldName, Object matchValue) {
    	return new BinaryMatcher(fieldName, MatchOp.NOT_EQUALS ,matchValue);
    }

    public static Matcher or(Matcher...constraints) {
    	return new LogicMatcher(MatchOp.OR, constraints);
    }

    public static Matcher startsWith(String keyName, String beginning) {
    	return new BinaryMatcher(keyName,MatchOp.STARTS_WITH,beginning);
    }

    public static Matcher startsWithIgnoreCase(String keyName, String beginning) {
    	return new BinaryMatcher(keyName,MatchOp.STARTS_WITH_IGNORE_CASE,beginning);
    }
}
