package cscie97.smartcity.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

/**
 * The parent class for storing of the credentials required to log in a user into the
 * system. Tied to a specific user; the authentication method is contained in the implementers
 */
public abstract class Credentials {

	private final String mUserId;
	
	public Credentials (String userId) {
		mUserId = userId;
	}
	
	public String getUserId () {
		return (mUserId);
	}
	
	public abstract String getType ();

	protected static String hash (String text) {
		String ret = null;
		try {
			MessageDigest md = MessageDigest.getInstance ("MD5");
			byte[] digest = md.digest (text.getBytes ());
			StringBuilder sb = new StringBuilder ();
			for (byte c: digest) {
				sb.append (Integer.toHexString ((c & 0xFF) | 0x100).substring (1, 3));
			}
			ret = sb.toString ();
		} catch (NoSuchAlgorithmException e) {
		}
		
		return (ret);
	}
	
	/**
	 * Checks the supplied credentials against all credentials known for the specified user;
	 * if a match is found, constructs and returns a token, otherwise throws an auth exception
	 * It is up to the implementers to override the equals () method for checking the match
	 */
	public final Token getToken (AuthenticationService service) throws AuthenticationException {
		String userId = getUserId ();
		User<?> user = service.getUser (userId);
		
		if (user == null) {
			throw new AuthenticationException ("No user " + userId + " defined");
		}
		
		for (Iterator<Credentials> credentials = user.listCredentials (); credentials.hasNext (); ) {
			if (equals (credentials.next ())) {
				return (new Token (userId));
			}
		}
		
		throw new AuthenticationException ("Credentials mismatch for user " + userId);
	}



}
