package cscie97.smartcity.authentication;

import java.util.HashMap;
import java.util.Map;

/**
 * An entitlement that takes the form of a single-level permission.
 */
public class Permission extends Entitlement<Permission> implements AuthenticationServiceConstants {
	
	private static final Map<String, Permission> mDefinedPermissions = new HashMap <> ();

	private Permission (String id, String name) {
		super (id);
		mName = name;
	}
	
	/**
	 * The factory method to ensure that all permission objects with the same id point
	 * to the same singleton instance
	 */
	public static Permission add (String id, String name) {
		Permission ret = mDefinedPermissions.get (id);
		if (ret == null) {
			ret = new Permission (id, name);
			synchronized (mDefinedPermissions) {
				mDefinedPermissions.put (id, ret);
			}
		}
		
		return (ret);
	}
	
	public static Permission get (String id) {
		return (mDefinedPermissions.get (id));
	}
	
	private String mName;
	
	public String getName () {
		return mName;
	}

	@Override
	public void accept (ItemVisitor visitor, int level) {
		visitor.visit (this, level);
	}
	
	public Permission getThis () {
		return (this);
	}

	@Override
	public String getType () {
		return (PERMISSION);
	}
		
	@Override
	public String show (String prefix) {
		String parentString = super.show (prefix);
		return (parentString + ", name: " + getName ());
	}


}
