package cscie97.smartcity.authentication;

/**
 * An exception that is thrown by methods in the auth package
 * Stores a reason text as the message of the parent exception 
 */
public class AuthenticationException extends Exception {
	private static final long serialVersionUID = 1L;

	public AuthenticationException (String message) {
		super (message);
	}
}
