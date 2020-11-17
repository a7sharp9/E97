package cscie97.smartcity.authentication;

/**
 * An instance of visitor that traverses the tree of entitlements of an item and compares
 * them to the required permission.
 */
public class AccessVisitor implements ItemVisitor {
	
	/**
	 * The entitlement to look for
	 */
	private final String mAccessItem;
	
	/**
	 * The result; true if the correct entitlement has been found
	 */
	private boolean mHasAccess = false;
	
	public final boolean hasAccess () {
		return (mHasAccess);
	}
	
	public AccessVisitor (String entitlement) {
		mAccessItem = entitlement;
	}

	@Override
	public void visit (IterableItem<?> item, int level) {
		int resourceSepIdx = mAccessItem.indexOf (':');
		
		if (mAccessItem.equals (item.getId ())) {
			mHasAccess = true; // entitlement or resource matched as is
		} else if (resourceSepIdx >= 0) {
			// we are checking for a resource with a combined id, <city>:<device>
			if (mAccessItem.substring (0, resourceSepIdx).equals (item.getId ())) {
				// try to see if the entitlement is on the entire city
				mHasAccess = true;
			} else if (item instanceof Resource<?> && mAccessItem.equals (((Resource<?>) item).getCombinedId ())) {
				// conversely, compare with the combined id of the item
				mHasAccess = true;
			}
		}
	}

}
