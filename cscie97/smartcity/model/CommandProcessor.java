package cscie97.smartcity.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.StringTokenizer;

import cscie97.smartcity.authentication.AuthenticationException;
import cscie97.smartcity.authentication.Credentials;
import cscie97.smartcity.authentication.AuthenticationService;
import cscie97.smartcity.authentication.AuthenticationServiceConstants;
import cscie97.smartcity.authentication.BiometricCredentials;
import cscie97.smartcity.authentication.Entitlement;
import cscie97.smartcity.authentication.PasswordCredentials;
import cscie97.smartcity.authentication.Permission;
import cscie97.smartcity.authentication.Resource;
import cscie97.smartcity.authentication.ResourceRole;
import cscie97.smartcity.authentication.Role;

import cscie97.smartcity.ledger.Ledger;
import cscie97.smartcity.ledger.LedgerException;

import static cscie97.smartcity.model.CommandParser.ParserException;

/**
 * Accepts and processes commands, either individually or collected in a file,
 * to perform model service operations.
 */
public class CommandProcessor implements ModelServiceConstants, AuthenticationServiceConstants {

	/**
	 * The instance of the ModelService class on which all operations are performed.
	 */
	private ModelService mModelService = null;

	public CommandProcessor (ModelService service) {
		mModelService = service;
	}
	
	/**
	 * Placeholder for an authorization token to connect to the model service
	 */
	private String mAuthToken = null;
	
	/**
	 * The output stream where all messages arising from the operations being accepted or
	 * rejected are directed. Default to the system output.
	 */
	private PrintStream mPrintStream = System.out;
	
	/**
	 * Builder-pattern method for changing the output stream
	 * @param stream The new output stream
	 * @return this processor
	 */
	public CommandProcessor withPrintStream (PrintStream stream) {
		mPrintStream = stream;
		return (this);
	}

	/**
	 * The common functional interface representing a single model service command.
	 */
	private interface Command {
		/**
		 * @param args a sequence of string arguments to be passed to the
		 * implementation
		 * @return the string representing the result of the command
		 * @throws ModelServiceException if the command is not valid
		 */
		public String doIt (String... args) throws ModelServiceException;
	}

	/**
	 * The expected options of a city command
	 */
	private static final Set<String> CITY_OPTIONS = Set.of (
			OPTION_ACCOUNT,
			OPTION_LAT,
			OPTION_LON,
			OPTION_NAME,
			OPTION_RADIUS
	);
	
	/**
	 * A utility function that constructs a city instance from a command
	 * @param args command tokens
	 * @return a City instance
	 * @throws ModelServiceException
	 */
	private City parseCity (String... args)  throws ModelServiceException {
		if (args.length <= 0) {
			throw new ModelServiceException (OP_DEFINE, CITY, "no id suppied for city");
		}
		
		String id = args[0];
		
		// The list of recognized option words
		Map<String, List<String>> parsedOptions = CommandParser.parseArgs (args, 1, CITY_OPTIONS);
		
		try {
			City ret = new City (id)
					.withAccount (CommandParser.getSingleOption (OPTION_ACCOUNT, parsedOptions, true))
					.withName (CommandParser.getCombinedOption (OPTION_NAME, parsedOptions, true))
					.withLocation (CommandParser.parseLocation (parsedOptions))
					.withRadius (CommandParser.parseDoubleValue (OPTION_RADIUS, parsedOptions));
			
			return (ret);
		} catch (ParserException px) {
			throw new ModelServiceException (OP_DEFINE, CITY, px.getMessage ());
		}
	}
	
	/**
	 * A utility function that fills a device instance from a map of parsed options
	 * @param operation a string indicating the kind of operation being performed. Will be
	 * included in the message of any exceptions raised by this method
	 * @param device an instance of a Device class to fill
	 * @param parsedOptions a map of option values keyed by the option keyword
	 * @throws ModelServiceException
	 */
	private static void parseDeviceAttributes (String operation, Device<?> device, Map<String, List<String>> parsedOptions) 
			 throws ModelServiceException
	{
		try {
			device.withEnabled (CommandParser.parseBooleanValue (OPTION_ENABLED, parsedOptions))
				.withLocation (CommandParser.parseLocation (parsedOptions));
			String statusString = CommandParser.getSingleOption (OPTION_STATUS, parsedOptions, true);
			if (statusString != null) {
				device.withStatus (Status.valueOf (statusString));
			}
		} catch (ParserException e) {
			throw new ModelServiceException (operation, device.getType (), e.getMessage ());
		}
	}
	
	/**
	 * polymorphic device factory
	 */
	private static class DeviceFactory {		
		public static final <T extends Device<T>> T newDevice (Class<T> c, String id) {
			try {
				Constructor<T> cons = c.getDeclaredConstructor (String.class, String.class);
				T newInstance = cons.newInstance (CommandParser.parseCityId (id), CommandParser.parseObjectId (id));
				return (newInstance);
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException x) {
				return (null);
			}
		}
	}

	/**
	 * Creates an instance of Device of an appropriate child class type 
	 * @param c the class of the desired instance
	 * @param combinedId a string that contains the city id and the device id separated by a colon;
	 * the id of the city is parsed and ignored
	 * @return the device instance
	 */
	private static <T extends Device<T>> T createDevice (Class<T> c, String combinedId) 
	{
		return (DeviceFactory.newDevice (c, combinedId));
	}
	
	/**
	 * The expected options of a device command
	 */
	private static final Set<String> DEVICE_OPTIONS = Set.of (
			OPTION_ENABLED,
			OPTION_DESCRIPTION,
			OPTION_LAT,
			OPTION_LON,
			OPTION_STATUS
	);
	
	/**
	 * Processes a command string for a device operation
	 * @param additionalOptions option keywords to expect on the string in addition to the
	 * options common for all devices
	 * @param args the command string
	 * @return the map of option values keyed by the option keyword
	 */
	private static Map<String, List<String>> parseDeviceOptions (Set<String> additionalOptions, String[] args) 
	{
		Set<String> combinedOptions = new HashSet<String> (DEVICE_OPTIONS);
		combinedOptions.addAll (additionalOptions);
		
		Map<String, List<String>> parsedOptions = CommandParser.parseArgs (args, 1, combinedOptions);
		return (parsedOptions);
	}
	
	/**
	 * Processes a command string for a device operation and fills the device instance
	 * @param operation a string indicating the kind of operation being performed. Will be
	 * included in the message of any exceptions raised by this method
	 * @param device an instance of a Device class to fill; will set the options common for all devices
	 * @param additionalOptions option keywords to expect on the string in addition to the
	 * options common for all devices
	 * @param args the command string
	 * @return the map of option values keyed by the option keyword
	 * @throws ModelServiceException
	 */
	private static Map<String, List<String>> parseDevice (String operation, Device<?> device, Set<String> additionalOptions, String[] args) 
			 throws ModelServiceException
	{
		Map<String, List<String>> parsedOptions = parseDeviceOptions (additionalOptions, args);
		parseDeviceAttributes (operation, device, parsedOptions);
		return (parsedOptions);
	}
	
	/**
	 * A group of helper utilities to parse various device types.
	 */
	
	private static StreetSign parseStreetSign (String operation, String... args) throws ModelServiceException {
		StreetSign ret = createDevice (StreetSign.class, args[0]);
		Map<String, List<String>> parsedOptions = parseDevice (operation, ret, Set.of (OPTION_TEXT), args);

		try {
			ret.withText (CommandParser.getCombinedOption (OPTION_TEXT, parsedOptions, true));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, STREET_SIGN, px.getMessage ());
		}
		
		return (ret);
	}
	
	private static InformationKiosk parseInformationKiosk (String operation, String... args) throws ModelServiceException {
		InformationKiosk ret = createDevice (InformationKiosk.class, args[0]);
		Map<String, List<String>> parsedOptions = parseDevice (operation, ret, Set.of (OPTION_ACCOUNT, OPTION_IMAGE), args);

		try {
			ret.withImage (CommandParser.getSingleOption (OPTION_IMAGE, parsedOptions, true))
			.withAccount (CommandParser.getSingleOption (OPTION_ACCOUNT, parsedOptions, true));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, INFO_KIOSK, px.getMessage ());
		}
		
		return (ret);
	}
	
	private static ParkingSpace parseParkingSpace (String operation, String... args) throws ModelServiceException {
		ParkingSpace ret = createDevice (ParkingSpace.class, args[0]);
		Map<String, List<String>> parsedOptions = parseDevice (operation, ret, Set.of (OPTION_ACCOUNT, OPTION_RATE, OPTION_OCCUPIED), args);

		try {
			ret
			.withRate (CommandParser.parseIntegerValue (OPTION_RATE, parsedOptions))
			.withOccupiedBy (CommandParser.getSingleOption (OPTION_OCCUPIED, parsedOptions, true))
			.withAccount (CommandParser.getSingleOption (OPTION_ACCOUNT, parsedOptions, true));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, PARKING_SPACE, px.getMessage ());
		}
		return (ret);
	}
	
	private static Robot parseRobot (String operation, String... args) throws ModelServiceException {
		Robot ret = createDevice (Robot.class, args[0]);
		Map<String, List<String>> parsedOptions = parseDevice (operation, ret, Set.of (OPTION_ACCOUNT, OPTION_ACTIVITY), args);

		try {
			ret.withActivity (CommandParser.getCombinedOption (OPTION_ACTIVITY, parsedOptions, true))
			.withAccount (CommandParser.getSingleOption (OPTION_ACCOUNT, parsedOptions, true));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, ROBOT, px.getMessage ());
		}
		
		return (ret);
	}
	
	private static StreetLight parseStreetLight (String operation, String... args) throws ModelServiceException {
		StreetLight ret = createDevice (StreetLight.class, args[0]);
		Map<String, List<String>> parsedOptions = parseDevice (operation, ret, Set.of (OPTION_BRIGHTNESS), args);

		try {
			ret.withBrightness (CommandParser.parseIntegerValue (OPTION_BRIGHTNESS, parsedOptions));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, STREET_LIGHT, px.getMessage ());
		}
		return (ret);
	}
	
	/**
	 * The expected options of a vehicle command
	 */
	private static Set<String> VEHICLE_OPTIONS = Set.of (
			OPTION_TYPE,
			OPTION_ACCOUNT,
			OPTION_ACTIVITY,
			OPTION_CAPACITY,
			OPTION_FEE
			);
						
	/**
	 * Creates an instance of a vehicle of an appropriate child class type 
	 * @param operation the operation string (define/update/show)
	 * @param args the command string
	 * @return the vehicle instance
	 * @throws ModelServiceException
	 */
	private Vehicle<?> parseVehicle (String operation, String... args) throws ModelServiceException {
		Map<String, List<String>> parsedOptions = parseDeviceOptions (VEHICLE_OPTIONS, args);
		try {
			String type = CommandParser.getSingleOption (OPTION_TYPE, parsedOptions, false);
			
			return (parseVehicleWithType (operation, parsedOptions, args[0], type));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, VEHICLE, px.getMessage ());
		}
	}
	
	/**
	 * A polymorphic vehicle factory based on the string representation of a type
	 */
	private static class VehicleFactory implements ModelServiceConstants {
		public static final Vehicle<?> newVehicle (String city, String id, String type) {
			if (type.equalsIgnoreCase (BUS)) {
				return (new Bus (city, id));
			} else if (type.equalsIgnoreCase (CAR)) {
				return (new Car (city, id));
			} else {
				return (new VehicleAttributeHolder (city, id));
			}
		}
	}
	/**
	 * Given a string representation of the vehicle type, create an appropriate object
	 * from a parsed map of options.
	 * @param operation the operation string (define/update/show)
	 * @param parsedOptions a map of option values keyed by the option keyword
	 * @param id String identifier combining the city and the vehicle id
	 * @param type the required type (bus or car)
	 * @return the vehicle instance
	 * @throws ModelServiceException
	 */
	private Vehicle<?> parseVehicleWithType (String operation, Map<String, List<String>> parsedOptions, String id, String type) throws ModelServiceException {
		try {
			Vehicle<?> ret = VehicleFactory.newVehicle (
					CommandParser.parseCityId (id), CommandParser.parseObjectId (id), type);
			
			ret.withActivity (CommandParser.getCombinedOption (OPTION_ACTIVITY, parsedOptions, true))
			.withFee (CommandParser.parseIntegerValue (OPTION_FEE, parsedOptions))
			.withCapacity (CommandParser.parseIntegerValue (OPTION_CAPACITY, parsedOptions))
			.withAccount (CommandParser.getSingleOption (OPTION_ACCOUNT, parsedOptions, true));
			parseDeviceAttributes (operation, ret, parsedOptions);		
			
			return (ret);
		} catch (ParserException px) {
			throw new ModelServiceException (operation, VEHICLE, px.getMessage ());
		}
	}
	
	/**
	 * Polymorphic person factory
	 */
	private static class PersonFactory {
		public static final <T extends Person<T>> T newPerson (Class<T> c, String id) {
			try {
				Constructor<T> cons = c.getConstructor (String.class);
				T newInstance = cons.newInstance (id);
				return (newInstance);
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException x) {
				return (null);
			}
		}

	}
	
	/**
	 * Constructs a person object with the correct subclass type
	 * @param c the required person subclass
	 * @param id the unique identifier
	 * @return the person class instance
	 * @throws ModelServiceException
	 */
	private static <T extends Person<T>> T createPerson (Class<T> c, String id) 
			 throws ModelServiceException
	{
		return (PersonFactory.newPerson (c, id));
	}
		
	/**
	 * A group of helper utilities to parse various person types.
	 */

	private Visitor parseVisitor (String operation, String... args) throws ModelServiceException {
		Visitor ret = createPerson (Visitor.class, args[0]);
		Map<String, List<String>> parsedOptions = CommandParser.parseArgs (args, 1, Set.of (OPTION_BIOMETRIC, OPTION_LAT, OPTION_LON));
		try {
			ret.withBiometric (mModelService.getAuthService (), CommandParser.getCombinedOption (OPTION_BIOMETRIC, parsedOptions, true))
			.withLocation (CommandParser.parseLocation (parsedOptions));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, VISITOR, px.getMessage ());
		}
		return (ret);
	}
	
	
	private static final Set<String> RESIDENT_OPTIONS = Set.of (
				OPTION_BIOMETRIC,
				OPTION_LAT,
				OPTION_LON,
				OPTION_NAME,
				OPTION_PHONE,
				OPTION_ROLE,
				OPTION_ACCOUNT
			);
	
	private Resident parseResident (String operation, String... args) throws ModelServiceException {		
		Resident ret = createPerson (Resident.class, args[0]);
		Map<String, List<String>> parsedOptions = CommandParser.parseArgs (args, 1, RESIDENT_OPTIONS);
		try {
			ret.withBiometric (mModelService.getAuthService (), CommandParser.getSingleOption (OPTION_BIOMETRIC, parsedOptions, true))
			.withAccount (CommandParser.getSingleOption (OPTION_ACCOUNT, parsedOptions, true))
			.withName (CommandParser.getCombinedOption (OPTION_NAME, parsedOptions, true))
			.withPhone (CommandParser.getCombinedOption (OPTION_PHONE, parsedOptions, true))
			.withRole (mModelService.getAuthService (), CommandParser.getSingleOption (OPTION_ROLE, parsedOptions, true))
			.withLocation (CommandParser.parseLocation (parsedOptions));
		} catch (ParserException px) {
			throw new ModelServiceException (operation, RESIDENT, px.getMessage ());
		}
		
		return (ret);
	}
	
//	private City findCity (String operation, String combinedId) throws ModelServiceException {
//		String cityId = CommandParser.parseCityId (combinedId);
//		City ret = mModelService.getCity (cityId);
//		
//		if (ret == null) {
//			throw new ModelServiceException (operation, null, "City with id " + cityId + " is not defined");
//		}
//		
//		return (ret);		
//	}
//	
	/**
	 * The group of device definition commands 
	 */
	
	private Command defineCityCommand = (args) -> {
		City newCity = parseCity (args);
		mModelService.defineCity (newCity, mAuthToken);
		return ("Defined city " + newCity.getId ());
	};
	
	private Command defineStreetSignCommand = (args) -> {
		StreetSign newDevice = parseStreetSign (OP_DEFINE, args);
		mModelService.defineDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Defined new street sign " + mModelService.showDevice (args[0]));
	};
	
	private Command defineInfoKioskCommand = (args) -> {
		InformationKiosk newDevice = parseInformationKiosk (OP_DEFINE, args);
		mModelService.defineDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Defined new street informationKiosk " + mModelService.showDevice (args[0]));
	};
		
	private Command defineStreetLightCommand = (args) -> {
		StreetLight newDevice = parseStreetLight (OP_DEFINE, args);
		mModelService.defineDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Defined new street light " + mModelService.showDevice (args[0]));
	};
		
	private Command defineParkingSpaceCommand = (args) -> {
		ParkingSpace newDevice = parseParkingSpace (OP_DEFINE, args);
		mModelService.defineDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Defined new parking space " + mModelService.showDevice (args[0]));
	};
		
	private Command defineRobotCommand = (args) -> {
		Robot newDevice = parseRobot (OP_DEFINE, args);
		mModelService.defineDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Defined new robot " + mModelService.showDevice (args[0]));
	};
		
	private Command defineVehicleCommand = (args) -> {
		Vehicle<?> newDevice = parseVehicle (OP_DEFINE, args);
		mModelService.defineDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Defined new vehicle " + mModelService.showDevice (args[0]));
	};
		
	/**
	 * The group of device update commands 
	 */
	
	private Command updateStreetSignCommand = (args) -> {
		StreetSign newDevice = parseStreetSign (OP_UPDATE, args);
		mModelService.updateDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Updated street sign " + mModelService.showDevice (args[0]));
	};
	
	private Command updateInfoKioskCommand = (args) -> {
		InformationKiosk newDevice = parseInformationKiosk (OP_UPDATE, args);
		mModelService.updateDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Updated street informationKiosk " + mModelService.showDevice (args[0]));
	};
		
	private Command updateStreetLightCommand = (args) -> {
		StreetLight newDevice = parseStreetLight (OP_UPDATE, args);
		mModelService.updateDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Updated street light " + mModelService.showDevice (args[0]));
	};
		
	private Command updateParkingSpaceCommand = (args) -> {
		ParkingSpace newDevice = parseParkingSpace (OP_UPDATE, args);
		mModelService.updateDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Updated parking space " + mModelService.showDevice (args[0]));
	};
		
	private Command updateRobotCommand = (args) -> {
		Robot newDevice = parseRobot (OP_UPDATE, args);
		mModelService.updateDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Updated robot " + mModelService.showDevice (args[0]));
	};
		
	private Command updateVehicleCommand = (args) -> {
		Map<String, List<String>> parsedOptions = parseDeviceOptions (VEHICLE_OPTIONS, args);
		Vehicle<?> newDevice = parseVehicleWithType (OP_UPDATE, parsedOptions, args[0], "");
		mModelService.updateDevice (CommandParser.parseCityId (args[0]), newDevice, mAuthToken);
		return ("Updated vehicle " + mModelService.showDevice (args[0]));
	};
	
	/**
	 * The group of person commands 
	 */
	
	private Command defineVisitorCommand = (args) -> {
		Visitor person = parseVisitor (OP_UPDATE, args);
		mModelService.defineVisitor (person, mAuthToken);
		return ("Defined visitor " + mModelService.showPerson (person.getId (), mAuthToken));
	};
		
	private Command defineResidentCommand = (args) -> {
		Resident person = parseResident (OP_UPDATE, args);
		mModelService.defineResident (person, mAuthToken);
		return ("Defined resident " + mModelService.showPerson (person.getId (), mAuthToken));
	};
		
	/**
	 * The group of person update commands 
	 */
	
	private Command updateVisitorCommand = (args) -> {
		Visitor person = parseVisitor (OP_UPDATE, args);
		mModelService.updatePerson (person, mAuthToken);
		return ("Updated visitor " + mModelService.showPerson (person.getId (), mAuthToken));
	};
		
	private Command updateResidentCommand = (args) -> {
		Resident person = parseResident (OP_UPDATE, args);
		mModelService.updatePerson (person, mAuthToken);
		return ("Updated resident " + mModelService.showPerson (person.getId (), mAuthToken));
	};
		
	/**
	 * The group of show commands 
	 */
	
	private Command showCityCommand = (args) -> {
		return (mModelService.showCity (args[0], mAuthToken));
	};
	
	private Command showDeviceCommand = (args) -> {
		return (mModelService.showDevice (
				CommandParser.parseCityId (args[0]),
				CommandParser.parseObjectId (args[0]),
				mAuthToken
				)
			);
	};
	
	private Command showPersonCommand = (args) -> {
		return (mModelService.showPerson (args[0], mAuthToken));
	};
	
	/**
	 * The sensor event emulation command 
	 */
	
	private Command createSensorEvent = (args) -> {
		Map<String, List<String>> parsedOptions = CommandParser.parseArgs (args, 1, Set.of (OPTION_TYPE, OPTION_VALUE, OPTION_SUBJECT));
		
		try {
			SensorEvent event = mModelService.createSensorEvent (
					CommandParser.parseCityId (args[0]),
					CommandParser.parseObjectId (args[0]),
					CommandParser.getSingleOption (OPTION_TYPE, parsedOptions, false),
					CommandParser.getCombinedOption (OPTION_VALUE, parsedOptions, true),
					CommandParser.getSingleOption (OPTION_SUBJECT, parsedOptions, true),
					mAuthToken);
			
			return ("Created event " + event.toString ());
		} catch (ParserException px) {
			throw new ModelServiceException (OP_CREATE, EVENT, px.getMessage ());			
		}
	};
	
	/**
	 * The sensor speaker output emulation command 
	 */
	
	private Command createSensorOutput = (args) -> {
		Map<String, List<String>> parsedOptions = CommandParser.parseArgs (args, 1, Set.of (OPTION_TYPE, OPTION_VALUE));
		
		try {
			String value = CommandParser.getCombinedOption (OPTION_VALUE, parsedOptions, true);
			boolean wasBroadcast = mModelService.createDeviceOutput (
					CommandParser.parseCityId (args[0]),
					CommandParser.parseObjectId (args[0]),
					CommandParser.getSingleOption (OPTION_TYPE, parsedOptions, true),
					value,
					mAuthToken);
			return ((wasBroadcast ? "Broadcast output for " : "Created output for ") + args[0] + ": " + value);
					
		} catch (ParserException px) {
			throw new ModelServiceException (OP_CREATE, OUTPUT, px.getMessage ());			
		}

	};
	
	/**
	 * The ledger commands
	 */
	private Command showAccountCommand = (args) -> {
		String account = args[0];
		Ledger ledger = mModelService.getLedger ();
		try {
			if (ledger != null) {
				return ("Balance for account " + account + ": " + ledger.getAccountBalance (account));
			}
		} catch (LedgerException lx) {
			// nothing
		}
			
		return ("");
		
	};
	
	/**
	 * THe authentication service commands
	 */
	private Command definePermissionCommand = (args) -> {
		Entitlement<?> e = Permission.add (args[0], args[1]);
		if (args.length > 2) {
			e.withDescription (args[2]);
		}
		
		try {
			mModelService.getAuthService ().addEntitlement (e, mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (OP_DEFINE, RESOURCE_ROLE, ax.getMessage ());
		}

		return ("Defined permission " + args[0]);
	};
	
	private Command defineRoleCommand = (args) -> {
		Entitlement<?> e = new Role (args[0], args[1]);
		if (args.length > 2) {
			e.withDescription (args[2]);
		}
		
		try {
			mModelService.getAuthService ().addEntitlement (e, mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (OP_DEFINE, RESOURCE_ROLE, ax.getMessage ());
		}
		return ("Defined role " + args[0]);
	};
	
	private Command defineResourceRoleCommand = (args) -> {
		AuthenticationService authService = mModelService.getAuthService ();
		Resource <?> resource = null;
		
		ResourceRole e = new ResourceRole (args[0], args[1]);
		for (int iArg = 2; iArg < args.length; iArg ++) {
			try {
				resource = authService.getResource (args[iArg], mAuthToken);
				if (resource == null) {
					throw new ModelServiceException (OP_DEFINE, RESOURCE_ROLE, "no such resource " + args[2]);
				}		
				e.withResource (resource);
			} catch (AuthenticationException ax) {
				throw new ModelServiceException (OP_DEFINE, RESOURCE_ROLE, ax.getMessage ());
			}
		}
		
		try {
			authService.addEntitlement (e, mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (OP_DEFINE, RESOURCE_ROLE, ax.getMessage ());
		}
		
		return ("Defined resource role " + args[0]);
	};
	
	private Command addRoleToUserCommand = (args) -> {
		try {
			mModelService.getAuthService ().addRoleToUser (args[0], args[1], mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (OP_ADD, ROLE, ax.getMessage ());
		}
		
		return ("Added role " + args[1] + " to user " + args[0]);
	};
	
	private final Credentials parseCredentials (String [] args) throws ModelServiceException {
		String userId = args[0];
		String type = args[1];
		Credentials c = null;
		if (type.equalsIgnoreCase (BIOMETRIC)) {
			c = new BiometricCredentials (userId, args[2]);
		} else if (type.equalsIgnoreCase (PASSWORD)) {
			c = new PasswordCredentials (userId, args[2]);
		} else {
			throw new ModelServiceException (OP_ADD, CREDENTIALS, "unknown credentials type " + type);
		}
		
		return (c);
	}
	
	private Command addCredentialsToUserCommand = (args) -> {
		String userId = args[0];
		Credentials c = parseCredentials (args);
		
		try {
			mModelService.getAuthService ().addCredentialsToUser (userId, c, mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (OP_ADD, CREDENTIALS, ax.getMessage ());
		}
		
		return ("Added " + c.getType () + " credentials to user " + userId);
	};
	
	private Command addPermissionToRoleCommand = (args) -> {
		try {
			mModelService.getAuthService ().addEntitlementToRole (args[0], args[1], mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (OP_ADD, PERMISSION, ax.getMessage ());
		}
		
		return ("Added permission " + args[1] + " to role " + args[0]);
	};
	
	private Command addResourceToRoleCommand = (args) -> {
		try {
			mModelService.getAuthService ().addResourceToRole (args[0], args[1], mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (OP_ADD, PERMISSION, ax.getMessage ());
		}
		
		return ("Added resource " + args[1] + " to role " + args[0]);
	};
	
	private Command loginCommand = (args) -> {
		String userId = args[0];
		Credentials c = parseCredentials (args);

		try {
			mAuthToken = mModelService.getAuthService ().login (c).getId ();
		} catch (AuthenticationException ax) {
			throw new ModelServiceException ("", LOGIN, ax.getMessage ());
		}
		
		return ("User " + userId + " successfully logged in");
	};
	
	private Command logoutCommand = (args) -> {
		try {
			mModelService.getAuthService ().logout (mAuthToken);
			mAuthToken = null;
			return ("Successfully logged out");
		} catch (AuthenticationException ax) {
			throw new ModelServiceException ("", LOGOUT, ax.getMessage ());
		}
		
	};
	
	private Command sleepCommand = (args) -> {
		int numSeconds = Integer.parseInt (args[0]);
		try {
			Thread.sleep (numSeconds * 1000L);
		} catch (InterruptedException ix) {
			return ("wanted to sleep for " + numSeconds + " seconds, but was shaken awake");
		}
		return ("had a sleep for " + numSeconds + " seconds; good morning");
	};
	
	private Command inventoryCommand = (args) -> {
		try {
			mModelService.getAuthService ().inventory (mPrintStream, mAuthToken);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException ("inventory", "", ax.getMessage ());
		}

		return (null);
	};

	/**
	 * The map of all valid commands
	 */
	
	
	private final Map<String, Map <String, Command>> mCommands = Map.of (
			OP_UPDATE, Map.of (
					STREET_SIGN, updateStreetSignCommand,
					INFO_KIOSK, updateInfoKioskCommand,
					STREET_LIGHT, updateStreetLightCommand,
					PARKING_SPACE, updateParkingSpaceCommand,
					ROBOT, updateRobotCommand,
					VEHICLE, updateVehicleCommand,
					RESIDENT, updateResidentCommand,
					VISITOR, updateVisitorCommand
					),
			OP_DEFINE, Map.ofEntries (
					new AbstractMap.SimpleEntry<String, Command> (CITY, defineCityCommand),
					new AbstractMap.SimpleEntry<String, Command> (STREET_SIGN, defineStreetSignCommand),
					new AbstractMap.SimpleEntry<String, Command> (INFO_KIOSK, defineInfoKioskCommand),
					new AbstractMap.SimpleEntry<String, Command> (STREET_LIGHT, defineStreetLightCommand),
					new AbstractMap.SimpleEntry<String, Command> (PARKING_SPACE, defineParkingSpaceCommand),
					new AbstractMap.SimpleEntry<String, Command> (ROBOT, defineRobotCommand),
					new AbstractMap.SimpleEntry<String, Command> (VEHICLE, defineVehicleCommand),
					new AbstractMap.SimpleEntry<String, Command> (RESIDENT, defineResidentCommand),
					new AbstractMap.SimpleEntry<String, Command> (VISITOR, defineVisitorCommand),
					new AbstractMap.SimpleEntry<String, Command> (PERMISSION, definePermissionCommand),
					new AbstractMap.SimpleEntry<String, Command> (ROLE, defineRoleCommand),
					new AbstractMap.SimpleEntry<String, Command> (RESOURCE_ROLE, defineResourceRoleCommand)
					),
			OP_SHOW, Map.of (
					CITY, showCityCommand,
					DEVICE, showDeviceCommand,
					PERSON, showPersonCommand,
					ACCOUNT, showAccountCommand
					),
			OP_ADD, Map.of (
					PERMISSION, addPermissionToRoleCommand,
					CREDENTIALS, addCredentialsToUserCommand,
					ROLE, addRoleToUserCommand,
					RESOURCE, addResourceToRoleCommand
					),
			OP_CREATE, Map.of (
					EVENT, createSensorEvent,
					OUTPUT, createSensorOutput
					),
			LOGIN, Map.of (
					"", loginCommand
					),
			LOGOUT, Map.of (
					"", logoutCommand
					),
			SLEEP, Map.of (
					"", sleepCommand
					),
			INVENTORY, Map.of (
					"", inventoryCommand
					)
			);

	/*-------------------------------------------------------------*/
	/* PUBLIC API */
	/*-------------------------------------------------------------*/
	
	/**
	 * Processes a single command, represented by a list of command tokens
	 * @param cmd command tokens
	 * @return the formatted string representing the result of the command execution
	 * @throws ModelServiceException if the command is not recognized or could not be processed
	 */
	public final String processCommand (String... cmd) throws ModelServiceException {
		String cmdGroupString = cmd[0];
		
		// find the instance of the command interface corresponding to this command keyword
		Map <String, Command> commandGroup = mCommands.get (cmdGroupString.toLowerCase ());

		if (commandGroup == null) { // no such group
			throw new ModelServiceException (cmdGroupString, null, "command not recognized");
		}

		String[] args = null;
		Command command = null;
		
		if (commandGroup.size () == 1) { // single commands do not have the second keyword
			command = commandGroup.values ().iterator ().next ();
			args = Arrays.copyOfRange (cmd, 1, cmd.length);
		} else {
			String cmdString = cmd[1];
			
			command = commandGroup.get (cmdString);
			
			if (command == null) { // no such commans in the group
				throw new ModelServiceException (cmdGroupString, cmdString, "command not recognized");
			}
			
			// collect the arguments, less the group and command keywords
			args = Arrays.copyOfRange (cmd, 2, cmd.length);
		}
		
		// perform the command processing
		return (command.doIt (args));
	}

	/**
	 * Processes commands collected in a text file one by one. Empty lines and lines beginning
	 * with the '#' symbol are ignored. If processing one of the commands causes an
	 * exception, it is logged to the print stream and the processing continues
	 * @param fileName the name of the file with the commands
	 */
	public final void processCommandFile (String fileName) {
		int lineNumber = 0;
		try (BufferedReader rd = new BufferedReader (new FileReader (new File (fileName), Charset.forName ("UTF-8")))) {
			for (String line = rd.readLine (); line != null; line = rd.readLine ()) {
				lineNumber ++;
				line = line.trim ();
				
				if (line.isEmpty () || line.charAt (0) == '#') { // comment or blank line
					mPrintStream.println (line);
					continue;
				}
				
				// collect command tokens
				StringTokenizer st = new StringTokenizer (line);
				ArrayList<String> argsArr = new ArrayList<> ();
				while (st.hasMoreTokens ()) {
					argsArr.add (st.nextToken ());
				}

				// call the single command processor
				try {
					String result = processCommand (argsArr.toArray (String[]::new));
					if (result != null && !result.isEmpty ()) {
						mPrintStream.println ("At line " + lineNumber + " " + result);
					}
				} catch (ModelServiceException lx) {
					mPrintStream.println ("At line " + lineNumber + " " + lx);
				}
			}
		} catch (IOException iox) {
			mPrintStream.println ("Could not read commands from file " + fileName);
		}
	}

}
