package cscie97.smartcity.authentication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Base class for any entitlement that can contain other entitlements.
 */
public abstract class EntitlementHolder<T extends EntitlementHolder<T>> extends Entitlement<T> {

	private final Set<Entitlement<?>> mEntitlements = new HashSet <> ();

	public EntitlementHolder (String id) {
		super (id);
	}

	public EntitlementHolder (String id, String description) {
		super (id, description);
	}

	@Override
	public void accept (ItemVisitor visitor, int level) {
		visitor.visit (this, level);
		
		for (Iterator<Entitlement<?>> iter = listEntitlements (); iter.hasNext (); ) {
			iter.next ().accept (visitor, level + 1);
		}
	}

	public Iterator<Entitlement<?>> listEntitlements () {
		return (mEntitlements.iterator ());
	}
	
	public void addEntitlement (Entitlement<?> entitlement) {
		mEntitlements.add (entitlement);
	}

	public T withEntitlement (Entitlement<?> entitlement) {
		mEntitlements.add (entitlement);
		return (getThis ());
	}
}