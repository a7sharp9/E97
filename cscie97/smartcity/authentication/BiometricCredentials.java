package cscie97.smartcity.authentication;

/**
 * The instance of the Credentials class that supports biometric identification
 * The biometric string is MD5-hashed before storage
 */
public class BiometricCredentials extends Credentials implements AuthenticationServiceConstants {
	
	private final String mBiometricHash;
	
	public BiometricCredentials (String userId, String biometric) {
		super (userId);
		mBiometricHash = hash (biometric);
	}

	@Override
	public int hashCode () {
		return (mBiometricHash.hashCode ());
	}
	
	@Override
	public boolean equals (Object obj) {
		if (obj instanceof BiometricCredentials) {
			return (mBiometricHash.equals (((BiometricCredentials) obj).mBiometricHash));
		} else {
			return (false);
		}
	}
	
	@Override
	public String getType () {
		return (BIOMETRIC);
	}

}
