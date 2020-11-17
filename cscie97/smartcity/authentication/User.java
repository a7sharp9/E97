package cscie97.smartcity.authentication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The base class for the user of the authentication service.
 */
public abstract class User<T extends User<T>> extends IterableItem<T> {

	private Set<String> mBiometric = new HashSet<> ();
	
	private final Set<Entitlement<?>> mEntitlements = new HashSet<> ();
	private final Set<Credentials> mCredentials = new HashSet<> ();
	
	public User (String id) {
		super (id);
	}
	
	public User (String id, String name) {
		super (id, name);
	}
	
	public final Iterator<String> getBiometric () {
		return (mBiometric.iterator ());
	}
	
	public final String getName () {
		return (getDescription ());
	}

	/**
	 * if a new biometric is added, this causes a new credential to be created as well
	 */
	public T withBiometric (AuthenticationService authService, String biometric) {
		if (biometric != null) {
			if (mBiometric.add (biometric)) {
				addCredentials (new BiometricCredentials (getId (), biometric));
			}
		}
		return (getThis ());
	}
	
	public void addCredentials (Credentials credentials) {
		mCredentials.add (credentials);
	}
	
	public void addEntitlement (Entitlement<?> entitlement) {
		mEntitlements.add (entitlement);
	}
	
	public Iterator<Entitlement<?>> listEntitlements () {
		return (mEntitlements.iterator ());
	}

	public Iterator<Credentials> listCredentials () {
		return (mCredentials.iterator ());
	}

	@Override
	public void accept (ItemVisitor visitor, int level) {
		visitor.visit (this, level);
		for (Iterator<Entitlement<?>> entitlements = listEntitlements (); entitlements.hasNext (); ) {
			visitor.visit (entitlements.next (), level + 1);
		}
	}


}
