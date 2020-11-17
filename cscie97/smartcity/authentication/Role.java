package cscie97.smartcity.authentication;

/**
 * The entitlement that represents a user role. Can also contain a hierarchy of child
 * entitlements
 */
public class Role extends EntitlementHolder<Role> implements AuthenticationServiceConstants {

	public Role (String id) {
		super (id);
	}

	public Role (String id, String name) {
		super (id);
		mName = name;
	}

	private String mName;
	
	public String getName () {
		return mName;
	}

	protected Role getThis () {
		return (this);
	}
		
	@Override
	public String getType () {
		return (ROLE);
	}

	@Override
	public String show (String prefix) {
		String parentString = super.show (prefix);
		return (parentString + ", name: \"" + getName () + "\"");
	}



}
