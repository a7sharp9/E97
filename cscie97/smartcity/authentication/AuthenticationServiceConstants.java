package cscie97.smartcity.authentication;

/**
 * The string constants common to the auth service classes
 * To use, implement this interface
 */
public interface AuthenticationServiceConstants {
	public static final String ROLE = "role";
	public static final String RESOURCE_ROLE = "resource_role";
	public static final String PERMISSION = "permission";
	public static final String CREDENTIALS = "credentials";
	public static final String RESOURCE = "resource";
	
	public static final String PERMISSION_USERS = "auth_user_admin";
	public static final String PERMISSION_CITIES = "scms_manage_city";
	public static final String PERMISSION_DEVICES = "auth_device_admin";
	public static final String PERMISSION_RESOURCES = "auth_resource_admin";
	public static final String PERMISSION_ENTITLEMENTS = "auth_entitlement_admin";
	public static final String PERMISSION_ROBOTS = "scms_control_robot";
	public static final String PERMISSION_CARS = "scms_drive_car";
	public static final String PERMISSION_BUS = "scms_ride_bus";
	public static final String PERMISSION_EVENT = "scms_simulate_event";
	
	public static final String ADMIN_ROLE = "admin";
	public static final String ADULT_ROLE = "adult";
	public static final String CHILD_ROLE = "child";
	public static final String PUBLIC_ADMIN_ROLE = "public_admin";
	
	public static final String LOGIN = "login";
	public static final String LOGOUT = "logout";
	public static final String SLEEP = "sleep";
	public static final String INVENTORY = "inventory";
	
	public static final String BIOMETRIC = "biometric";
	public static final String PASSWORD = "password";
	
}
