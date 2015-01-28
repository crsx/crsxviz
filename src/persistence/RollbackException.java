package persistence;

/**
 * The exception thrown when a CRUD method encounters a problem, such as
 * not being able to connect to the database, deadlock, when invalid parameters
 * are passed, etc.
 */
public class RollbackException extends Exception {
	private static final long serialVersionUID = 1L;

	public RollbackException(String message) {
		super(message);
	}

	public RollbackException(Exception e) {
		super(e);
	}

    public RollbackException(String message, Exception e) {
        super(message,e);
    }
}
