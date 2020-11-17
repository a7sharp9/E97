package cscie97.smartcity.authentication;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static java.lang.System.lineSeparator;

/**
 * The authentication service provides the interface for checking access to the model service and
 * controller operations through a common mechanism based on permissions. The permissions are
 * checked against the supplied auth token; the token is tied to the user, and the user may
 * possess one or more entitlements.
 */
public class AuthenticationService extends IterableItem<AuthenticationService> implements AuthenticationServiceConstants {
	
	/**
	 * The current tokens. A token is created when user logs in.
	 */
	private final Map<String, Token> mTokenStore = new HashMap <> ();

	/**
	 * The tree of known entitlements
	 */
	private final Map<String, Entitlement<?>> mEntitlementStore = new HashMap <> ();

	/**
	 * The set of known users
	 */
	private final Map<String, User<?>> mUserStore = new HashMap <> ();
	
	/**
	 * The resources which require access permissions; cities and devices
	 */
	private final Map<String, Resource<?>> mResourceStore = new HashMap <> ();
	
	/**
	 * The list of permissions defined in the system
	 */
	private static final Set<Permission> DEFAULT_PERMISSIONS = Set.of (
			Permission.add (PERMISSION_USERS, "User Administrator").withDescription ("Create, Update, Delete Users"),
			Permission.add (PERMISSION_CITIES, "City Administrator").withDescription ("Create or update a City"),
			Permission.add (PERMISSION_DEVICES, "Device Administrator").withDescription ("Create or update a device"),
			Permission.add (PERMISSION_RESOURCES, "Resource Administrator").withDescription ("Create, Update, Delete Resources"),
			Permission.add (PERMISSION_ENTITLEMENTS, "Entitlement Administrator").withDescription ("Create, Update, Delete entitlements"),
			Permission.add (PERMISSION_ROBOTS, "Control Robot").withDescription ("Full Control of Robots"),
			Permission.add (PERMISSION_EVENT, "Simulate Event").withDescription ("Simulate an event"),
			Permission.add (PERMISSION_BUS, "Ride Bus").withDescription ("Ride a bus"),
			Permission.add (PERMISSION_CARS, "Drive car").withDescription ("Drive a car")		
	);
	
	/**
	 * The list of pre-defined roles
	 */
	private static final Set<Role> DEFAULT_ROLES = Set.of (
			new Role (ADMIN_ROLE, "Admin Role").withDescription ("Has all permissions of an administrator"),
			new Role (ADULT_ROLE, "Adult Role").withDescription ("Has all permissions of an adult"),
			new Role (CHILD_ROLE, "Child Role").withDescription ("Has all permissions of a child"),
			new Role (PUBLIC_ADMIN_ROLE, "Public Administrator Role").withDescription ("Has all permissions of a public administrator")			
	);
	
	private static AuthenticationService mInstance = null;
	private final static Object mInstanceLock = new Object ();
	
	private AuthenticationService () throws AuthenticationException {
		super ("authService");
		// add default roles
		for (Role r: DEFAULT_ROLES) {
			mEntitlementStore.put (r.getId (), r);
		}
			
		// add default permissions
		for (Permission p: DEFAULT_PERMISSIONS) {
			mEntitlementStore.put (p.getId (), p);
			// the admin role has all known permissions
			addEntitlementToRole (ADMIN_ROLE, p.getId ());
		}
		
		// fill the entitlements of the public admin role
		addEntitlementToRole (PUBLIC_ADMIN_ROLE, PERMISSION_DEVICES);
		addEntitlementToRole (PUBLIC_ADMIN_ROLE, PERMISSION_ROBOTS);
		addEntitlementToRole (PUBLIC_ADMIN_ROLE, PERMISSION_CARS);

		// fill the entitlements of the adult role
		addEntitlementToRole (ADULT_ROLE, PERMISSION_ROBOTS);
		addEntitlementToRole (ADULT_ROLE, PERMISSION_CARS);
		addEntitlementToRole (ADULT_ROLE, PERMISSION_BUS);

		// fill the entitlements of the child role
		addEntitlementToRole (CHILD_ROLE, PERMISSION_ROBOTS);
		addEntitlementToRole (CHILD_ROLE, PERMISSION_BUS);
		
		// create an admin user and assign password credentials and admin role
		User<?> rootUser = new InternalUser ("root");
		
		// set the hash directly to avoid disclosing the password in the code
		PasswordCredentials rootCredentials = new PasswordCredentials (rootUser.getId (), "");
		rootCredentials.setPasswordHash ("5ebe2294ecd0e0f08eab7690d2a6ee69");
		rootUser.addCredentials (rootCredentials);
		mUserStore.put (rootUser.getId (), rootUser);
		addRoleToUser (rootUser, ADMIN_ROLE);
	}

	public static AuthenticationService instance () throws AuthenticationException {
		if (mInstance == null) {
			synchronized (mInstanceLock) {
				mInstance = new AuthenticationService ();
			}
		}
		
		return (mInstance);
	}
	
	/**
	 * Allows user to obtain an auth token tied to their permissions
	 * Stores the token keyed by its id.
	 * @param credentials the supplied credentials for the user
	 * @return the token, if credentials match
	 */
	public Token login (Credentials credentials) throws AuthenticationException {
		Token ret = null;
		
		// the token id is randomly generated; check that we're not duplicating an existing one
		do {
			ret = credentials.getToken (this);
		} while (mTokenStore.containsKey (ret.getId ()));
		
		// store it
		synchronized (mTokenStore) {
			mTokenStore.put (ret.getId (), ret);
		}
		
		return (ret);
	}
	
	/**
	 * Invalidates the token for this user
	 * @param user id
	 * @return true if this user had a token
	 */
	public synchronized boolean logout (String tokenId) throws AuthenticationException {
		return (mTokenStore.remove (tokenId) != null);
	}
	
	/**
	 * helper method that will attempt to find a token by id and then validate access
	 */
	public void checkAccess (String token, String entitlementId) throws AuthenticationException {
		checkAccess (findToken (token), entitlementId);
	}
	
	/**
	 * Given a token, check that it allows access to the requested entitlement
	 * @param token auth token to check
	 * @param entitlement the identifier of entitlement
	 * @throws AuthenticationException if access is forbidden, or token is invalid or has expired
	 */
	public void checkAccess (Token token, String entitlementId) throws AuthenticationException {
		if (!token.isValid ()) {
			throw new AuthenticationException ("Invalid token; please relogin");
		}
		
		String userId = token.getUserId ();
		User<?> user = mUserStore.get (userId);
		
		AccessVisitor v = new AccessVisitor (entitlementId);
		
		if (user == null) {
			throw new AuthenticationException ("Invalid token: no user " + userId);
		}
		
		for (Iterator<Entitlement<?>> entitlements = user.listEntitlements (); entitlements.hasNext () && !v.hasAccess (); ) {
			entitlements.next ().accept (v);
		}
		
		if (!v.hasAccess ()) {
			throw new NoAccessException (userId, entitlementId);
		}
	}
	
	private Token findToken (String id) throws AuthenticationException {
		if (id == null) {
			throw new AuthenticationException ("Not logged in");
		}
		Token ret = mTokenStore.get (id);
		if (ret == null) {
			throw new AuthenticationException ("No token with id " + id + " found");
		}
		
		return (ret);
	}
	
	/**
	 * Attempts to validate that the caller has permission to operate on users,
	 * if so, adds the requested user to the list
	 */
	public void addUser (User<?> user, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_USERS);
		
		mUserStore.put (user.getId (), user);
	}
	
	/**
	 * Attempts to validate that the caller has permission to operate on resources,
	 * if so, adds the requested resource to the list
	 */
	public void addResource (String id, Resource<?> resource, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_RESOURCES);
		
		mResourceStore.put (id, resource);
	}
	
	/**
	 * Attempts to validate that the caller has permission to operate on resources,
	 * if so, retrieves the requested resource object
	 */
	public Resource<?> getResource (String id, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_RESOURCES);
		return (mResourceStore.get (id));
	}
		
	/**
	 * Attempts to validate that the caller has permission to operate on entitlements,
	 * if so, adds the requested entitlement to the list
	 */
	public void addEntitlement (Entitlement<?> entitlement, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_ENTITLEMENTS);
		
		mEntitlementStore.put (entitlement.getId (), entitlement);
	}
	
	/**
	 * Attempts to validate that the caller has permission to operate on users,
	 * if so, adds the requested entitlement to the user
	 */
	public void addRoleToUser (String userId, String entitlementId, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_USERS);
		User<?> user = mUserStore.get (userId);
		if (user == null) {
			throw new AuthenticationException ("No user " + userId + " defined");
		}
		addRoleToUser (user, entitlementId);
	}
		
	public void addRoleToUser (User<?> user, String entitlementId) throws AuthenticationException {
		
		Entitlement<?> entitlement = mEntitlementStore.get (entitlementId);
		if (entitlement == null) {
			throw new AuthenticationException ("No entitlement " + user.getId () + " defined");
		}
		
		user.addEntitlement (entitlement);
	}

	/**
	 * Attempts to validate that the caller has permission to operate on users,
	 * if so, adds the requested credential to the user
	 */
	public void addCredentialsToUser (String userId, Credentials credentials, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_USERS);
		
		User<?> user = getUser (userId);
		if (user == null) {
			throw new AuthenticationException ("No user " + userId + " defined");
		}
		
		user.addCredentials (credentials);
	}
	
	/**
	 * Attempts to validate that the caller has permission to operate on entitlements,
	 * if so, adds the requested entitlement to the role
	 */
	public void addEntitlementToRole (String roleId, String permissionId, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_ENTITLEMENTS);
		addEntitlementToRole (roleId, permissionId);
	}
		
	private void addEntitlementToRole (String roleId, String permissionId) throws AuthenticationException {
		Entitlement<?> e = mEntitlementStore.get (roleId);
		if (e == null) {
			throw new AuthenticationException ("No role " + roleId + " defined");
		} else if (!(e instanceof EntitlementHolder)) {
			throw new AuthenticationException ("Entitlement with id " + roleId + " is not a role");
		}
		
		EntitlementHolder<?> role = (EntitlementHolder<?>) e;
		
		e = mEntitlementStore.get (permissionId);
		if (e == null) {
			throw new AuthenticationException ("No permission " + permissionId + " defined");
		}
		
		role.addEntitlement (e);
	}
	
	public void addResourceToRole (String roleId, String resourceId, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, PERMISSION_ENTITLEMENTS);
		
		Entitlement<?> e = mEntitlementStore.get (roleId);
		if (e == null) {
			throw new AuthenticationException ("No role " + roleId + " defined");
		} else if (!(e instanceof ResourceRole)) {
			throw new AuthenticationException ("Entitlement with id " + roleId + " is not a resource role");
		}
		
		Resource<?> resource = mResourceStore.get (resourceId);
		if (resource == null) {
			throw new AuthenticationException ("No resource " + resourceId + " defined");
		}
		
		ResourceRole role = (ResourceRole) e;
		role.withResource (resource);
	}
	
	/**
	 * Checks that the caller has admin access; if so, traverses the list of users, the list of resources
	 * and the tree of entitlements and puts their text representations into the supplied stream
	 */
	public void inventory (PrintStream toStream, String tokenId) throws AuthenticationException {
		checkAccess (tokenId, ADMIN_ROLE);
		InventoryVisitor v = new InventoryVisitor ();
		accept (v);
		toStream.append (v.getText ()).append (lineSeparator ());
	}
	
	/**
	 * package-level helper method
	 */
	User<?> getUser (String id) {
		return (mUserStore.get (id));
	}

	// Traversing all collections
	@Override
	public void accept (ItemVisitor visitor, int level) {
		visitor.visit (this, level);
		for (User<?> user: mUserStore.values ()) {
			user.accept (visitor, level + 1);
		}
		
		for (Resource<?> resource: mResourceStore.values ()) {
			resource.accept (visitor, level + 1);
		}

		for (Entitlement<?> entitlement: mEntitlementStore.values ()) {
			entitlement.accept (visitor, level + 1);
		}
	}

	@Override
	protected AuthenticationService getThis () {
		return (this);
	}

	@Override
	public String getType () {
		return ("Authentication Service");
	}

	@Override
	public String show (String prefix) {
		return (getType () + ":" + lineSeparator ());
	}

}
