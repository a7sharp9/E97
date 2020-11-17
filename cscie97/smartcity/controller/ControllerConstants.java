package cscie97.smartcity.controller;

/**
 * Common constants for the package objects. Implement this interface to use.
 */
public interface ControllerConstants {
	public static final String EMERGENCY_BROADCAST_FORMAT = "There is a %s in %s, please find shelter immediately";
	public static final String EMERGENCY_ACTIVITY_FORMAT = "Address %s at lat %.4f, long %.4f";
	public static final String LITTER_ACTIVITY_FORMAT = "clean garbage at lat %.4f, long %.4f";
	public static final String MISSING_PERSON_BROADCAST_FORMAT = "person %s is at lat %.4f, long %.4f, a robot is retrieving now, stay where you are";
	public static final String MISSING_PERSON_ACTIVITY_FORMAT = "retrieve person %s and bring to lat %.4f, long %.4f";
	public static final String BROKEN_GLASS_ACTIVITY_FORMAT = "clean broken glass at lat %.4f, long %.4f";
	public static final String GOES_TO = "Yes, this bus goes to ";
	public static final String DOESNT_GO_TO = "No, this bus does not go to ";
	public static final String GOOD_TO_SEE = "Hello, good to see you, ";
	public static final String TICKETS_RESERVED = "your seats are reserved; please arrive a few minutes early";
	public static final String LITTER_BROADCAST = "Please do not litter";
	
	public static final String SHELTER_ACTIVITY = "help people find shelter";
	
	public static final String ACCIDENT_BROADCAST = "Stay calm, help is on its way";
	
	public static final int LITTER_FEE = 50;
	public static final int RESERVATION_FEE = 10;
}
