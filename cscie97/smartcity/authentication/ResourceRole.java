package cscie97.smartcity.authentication;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The entitlement that, in addition to being a role, also allows access to one or more resources.
 */
public class ResourceRole extends EntitlementHolder<ResourceRole> implements AuthenticationServiceConstants {

	@Override
	protected ResourceRole getThis () {
		return (this);
	}

	private final Set<Resource<?>> mResources = new HashSet<> ();
	
	public ResourceRole (String id) {
		super (id);
	}

	public ResourceRole (String id, String description) {
		super (id, description);
	}

	public Iterator<Resource<?>> listResources () {
		return (mResources.iterator ());
	}
	
	public ResourceRole withResource (Resource<?> r) {
		mResources.add (r);
		return (this);
	}
	
	@Override
	public void accept (ItemVisitor visitor, int level) {
		super.accept (visitor, level);
		
		for (Iterator<Resource<?>> iter = listResources (); iter.hasNext (); ) {
			iter.next ().accept (visitor, level + 1);
		}
	}

	@Override
	public String getType () {
		return (RESOURCE_ROLE);
	}
	
	@Override
	public String show (String prefix) {
		String parentString = super.show (prefix);
		return (parentString + ", resources: " + mResources.stream ().map (r -> r.getId ()).collect (Collectors.joining (", ")) );
	}




	
}
