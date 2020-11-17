package cscie97.smartcity.authentication;

/**
 * The exception resulting from a check on access of a user to a certain entitlement
 */
public class NoAccessException extends AuthenticationException {
	private static final long serialVersionUID = 1L;

	public NoAccessException (String userId, String entitlement) {
		super ("" + userId + " does not have permission " + entitlement);
	}

}
