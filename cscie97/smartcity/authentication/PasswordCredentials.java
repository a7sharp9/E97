package cscie97.smartcity.authentication;

/**
 * The instance of the Credentials class that supports password identification
 * The password string is MD5-hashed before storage
 */
public class PasswordCredentials extends Credentials implements AuthenticationServiceConstants{
	
	public String mPasswordHash;
	
	public PasswordCredentials (String userId, String password) {
		super (userId);
		mPasswordHash = hash (password);
	}

	@Override
	public int hashCode () {
		return (mPasswordHash.hashCode ());
	}
	
	@Override
	public boolean equals (Object obj) {
		if (obj instanceof PasswordCredentials) {
			return (mPasswordHash.equals (((PasswordCredentials) obj).mPasswordHash));
		} else {
			return (false);
		}
	}

	@Override
	public String getType () {
		return (PASSWORD);
	}
	
	/**
	 * package-level helper used to obfuscate the master password in the code
	 */
	void setPasswordHash (String passwordHash) {
		mPasswordHash = passwordHash;
	}

}
