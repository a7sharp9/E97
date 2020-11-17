package cscie97.smartcity.model;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cscie97.smartcity.authentication.AuthenticationException;
import cscie97.smartcity.authentication.AuthenticationService;
import cscie97.smartcity.authentication.AuthenticationServiceConstants;
import cscie97.smartcity.ledger.Ledger;
import cscie97.smartcity.ledger.LedgerConstants;
import cscie97.smartcity.ledger.LedgerException;
import cscie97.smartcity.ledger.Transaction;

import static java.lang.System.lineSeparator;

public class ModelService implements ModelServiceConstants, AuthenticationServiceConstants, LedgerConstants, EventSubject {

	private Map<String, City> mCities = new HashMap<> ();

	private Map<String, Person<?>> mPeople = new HashMap<>();
	
	private Ledger mLedger;
	
	private AuthenticationService mAuthService;
	
	public ModelService withLedger (Ledger ledger) {
		mLedger = ledger;
		return (this);
	}
	
	public ModelService withAuthService (AuthenticationService service) {
		mAuthService = service;
		return (this);
	}
	
	public AuthenticationService getAuthService () {
		return (mAuthService);
	}
	
	public void defineCity(City city, String authToken) throws ModelServiceException {
		checkAccess (authToken, OP_CREATE, CITY);
		String id = city.getId ();
		if (mCities.containsKey (id)) {
			throw new ModelServiceException (OP_DEFINE, CITY, "The city with id " + id + " already exists.");
		}
			
		city.validate ();
		
		try {
			getAuthService ().addResource (id, city, authToken);
		} catch (AuthenticationException ax) {
			// should not happen - we checked for access at the top of this method
			throw new RuntimeException ("your operation-to-permission table is wrong");
		}
		
		mCities.put (id, city);
		createAndFund (city.getAccount ().getAddress (), INITIAL_CITY_BALANCE);
	}

	public String showCity(String id, String authToken) throws ModelServiceException {
		checkAccess (authToken, OP_SHOW, CITY);
		City city = mCities.get (id);
		
		if (city == null) {
			throw new ModelServiceException (OP_SHOW, CITY, "The city with id " + id + " does not exist.");
		}
		
		StringBuilder sb = new StringBuilder (city.toString ());
		
		sb.append ("  devices:").append (lineSeparator ());
		for (Iterator<Device<?>> cityDevices = city.listDevices (); cityDevices.hasNext (); ) {
			sb.append (cityDevices.next ().show ("    ")).append (lineSeparator ());
		}
		
		Location cityCenter = city.getLocation ();
		double cityRadius = city.getRadius ();
		sb.append ("  people:").append (lineSeparator ());
		for (Person<?> person: mPeople.values ()) {
			Location loc = person.getLocation ();
			if (loc != null && loc.withinDistance (cityCenter, cityRadius)) {
				sb.append (person.show ("    ")).append (lineSeparator ());
			}
		}
		
		return (sb.toString ());
	}

	public void defineDevice (String cityId, Device<?> device, String authToken) throws ModelServiceException {
		checkAccess (authToken, OP_DEFINE, DEVICE);
		City city = mCities.get (cityId);

		if (city == null) {
			throw new ModelServiceException(OP_DEFINE, DEVICE, "The city with id " + cityId + " does not exist.");
		}

		try {
			getAuthService ().addResource (CommandParser.reconstituteCombinedId (cityId, device.getId ()), device, authToken);
		} catch (AuthenticationException ax) {
			// should not happen - we checked for access at the top of this method
			throw new RuntimeException ("your operation-to-permission table is wrong");
		}
		
		city.defineDevice (device, authToken);
		if (device instanceof PayableDevice) {
			PayableDevice<?> pd = (PayableDevice<?>) device;
			createAndFund (pd.getAccount (), INITIAL_DEVICE_BALANCE);
		}

	}
	
	public void updateDevice (String cityId, Device<?> device, String authToken) throws ModelServiceException {
		City city = mCities.get (cityId);
		
		if (city == null) {
			throw new ModelServiceException(OP_UPDATE, DEVICE, "The city with id " + cityId + " does not exist.");
		}
		
		Device <?> thisDevice = city.getDevice (device.getId ());

		String fineType = thisDevice.getType ();
		String checkType = DEVICE;
		if (CAR.equalsIgnoreCase (fineType) || ROBOT.equalsIgnoreCase (fineType)) {
			checkType = fineType;
		}
		
		String resourceId = CommandParser.reconstituteCombinedId (cityId, device.getId ());
		
		checkAccess (authToken, OP_UPDATE, checkType, resourceId);

		city.updateDevice (device, authToken);
	}
	
	public void defineStreetSign(String cityId, StreetSign sign, String authToken) throws ModelServiceException {
		defineDevice (cityId, sign, authToken);
	}

	public void updateStreetSign(String cityId, String id, Boolean enabled, String text, String authToken) 
			throws ModelServiceException	
	{
		StreetSign sign = new StreetSign (cityId, id)
				.withEnabled (enabled)
				.withText (text);
		updateDevice (cityId, sign, authToken);
	}

	public void defineStreetLight(String cityId, StreetLight light, String authToken) throws ModelServiceException {
		defineDevice (cityId, light, authToken);
	}

	public void updateStreetLight(String cityId, String id, Boolean enabled, Integer brightness, String authToken) 
			throws ModelServiceException	
	{
		StreetLight sign = new StreetLight (cityId, id)
				.withEnabled (enabled)
				.withBrightness (brightness);
		updateDevice (cityId, sign, authToken);
	}

	public void defineInformationKiosk(String cityId, InformationKiosk kiosk, String authToken) throws ModelServiceException {
		defineDevice (cityId, kiosk, authToken);
	}

	public void updateInformationKiosk(String cityId, String id, Boolean enabled, String image, String authToken) 
			throws ModelServiceException	
	{
		InformationKiosk kiosk = new InformationKiosk (cityId, id)
				.withEnabled (enabled)
				.withImage (image);
		updateDevice (cityId, kiosk, authToken);
	}

	public void defineRobot (String cityId, Robot robot, String authToken) throws ModelServiceException {
		defineDevice (cityId, robot, authToken);
	}

	public void updateRobot(String cityId, String id, Location location, Boolean enabled, String activity, String authToken) 
			throws ModelServiceException
	{
		Robot robot = new Robot (cityId, id)
				.withEnabled (enabled)
				.withLocation (location)
				.withActivity (activity);
		updateDevice (cityId, robot, authToken);
	}

	public void defineParkingSpace (String cityId, ParkingSpace space, String authToken) throws ModelServiceException {
		defineDevice (cityId, space, authToken);
	}

	public void updateParkingSpace(String cityId, String id, Boolean enabled, Integer rate, String occupiedBy, String authToken) 
			throws ModelServiceException
	{
		ParkingSpace space = new ParkingSpace (cityId, id)
				.withEnabled (enabled)
				.withOccupiedBy (occupiedBy)
				.withRate (rate);
		updateDevice (cityId, space, authToken);
	}

	public void defineVehicle(String cityId, Vehicle<?> vehicle, String authToken) throws ModelServiceException {
		defineDevice (cityId, vehicle, authToken);
	}

	public void updateVehicle(String cityId, String id, Location location, Boolean enabled, String activity, int fee, String authToken) 
			throws ModelServiceException
	{
		VehicleAttributeHolder vehicle = new VehicleAttributeHolder (cityId, id)
				.withActivity (activity)
				.withFee (fee)
				.withEnabled (enabled)
				.withLocation (location);
		updateDevice (cityId, vehicle, authToken);
	}

	private interface DeviceVisitor {
		public void visit (City city, Device<?> device, Object... args) throws ModelServiceException;
	}
	
	private boolean visitDevices (DeviceVisitor visitor, String cityId, String deviceId, String op, Object... args) 
		throws ModelServiceException
	{
		City city = getCity (cityId);

		if (city == null) {
			throw new ModelServiceException (op, null, "City with id " + cityId + " is not defined");
		}
		
		return (visitDevices (visitor, city, deviceId, op, args));
	}
	
		
	private boolean visitDevices (DeviceVisitor visitor, City city, String deviceId, String op, Object... args) 
			throws ModelServiceException
	{		
		boolean ret = false;
		
		if (deviceId == null || deviceId.length () <= 0) {
			ret = true;
			for (Iterator<Device<?>> devices = city.listDevices (); devices.hasNext ();) {
				visitor.visit (city, devices.next (), args);
			}
		} else {
			Device<?> device = city.getDevice (deviceId);
			if (device == null) {
				throw new ModelServiceException (op, null, "Device with id " + deviceId + " is not defined in city " + city.getId ());
			}
			
			visitor.visit (city, device, args);
		}
		
		return (ret);
		
	}
	
	public String showDevice (String combinedId) throws ModelServiceException {
		return (showDevice (CommandParser.parseCityId (combinedId), CommandParser.parseObjectId (combinedId)));
	}
	
	public String showDevice (String cityId, String id, String authToken) throws ModelServiceException {
		checkAccess (authToken, OP_SHOW, DEVICE);
		return (showDevice (cityId, id));
	}
	
	private String showDevice (String cityId, String id) throws ModelServiceException {
		City city = getCity (cityId);

		if (city == null) {
			throw new ModelServiceException (OP_SHOW, DEVICE, "City with id " + cityId + " is not defined");
		}
		
		StringBuilder ret = new StringBuilder ();
		
		if (id == null || id.length () <= 0) {
			ret.append ("Devices for city ").append(cityId).append (":").append (lineSeparator ());
			for (Iterator<Device<?>> devices = city.listDevices (); devices.hasNext ();) {
				ret.append (devices.next ().show ("  ")).append (lineSeparator ());
			}
		} else {
			Device<?> device = city.getDevice (id);
			if (device == null) {
				throw new ModelServiceException (OP_SHOW, DEVICE, "Device with id " + id + " is not defined in city " + cityId);
			}
			ret.append (device.show ()).append (lineSeparator ());
		}
		
		return (ret.toString ());
	}

	public SensorEvent createSensorEvent(String cityId, String deviceId, String type, String value, String subject, String authToken) 
		throws ModelServiceException
	{
		checkAccess (authToken, OP_CREATE, EVENT);
		City thisCity = getCity (cityId);
		Device<?> thisDevice = null;

		if (thisCity != null) {
			thisDevice = thisCity.getDevice (deviceId);
		}

		SensorEvent event = EventFactory.parseEvent (thisDevice, type, value, subject);
		
		if (event != null) {
			visitDevices ((city, device, args) -> device.setLastEvent (this, (SensorEvent) args[0]), 
					cityId, deviceId, OP_CREATE + " " + EVENT, event);
		}
		
		return (event);
	}

	public boolean createDeviceOutput (String cityId, String deviceId, String type, String value, String authToken) 
		throws ModelServiceException
	{
		checkAccess (authToken, OP_CREATE, OUTPUT);
		return (visitDevices ((city, device, args) -> device.broadcast (null, (String) args[0]), 
				cityId, deviceId, OP_CREATE + " " + OUTPUT, value));
	}
	
	private void definePerson (Person<?> person, String authToken) 
		throws ModelServiceException
	{
		checkAccess (authToken, OP_CREATE, PERSON);
		
		String id = person.getId ();
		if (mPeople.containsKey (id)) {
			throw new ModelServiceException (OP_DEFINE, PERSON, "The person with id " + id + " already exists.");
		}
			
		person.validate ();
		
		try {
			getAuthService ().addUser (person, authToken);
		} catch (AuthenticationException ax) {
			// should not happen - we checked for access at the top of this method
			throw new RuntimeException ("your operation-to-permission table is wrong");
		}
		
		mPeople.put (id, person);
	}
	
	public void updatePerson (Person<?> fromPerson, String authToken) 
			throws ModelServiceException
	{
		checkAccess (authToken, OP_UPDATE, PERSON);
		Person<?> person = mPeople.get (fromPerson.getId ());
		if (person == null) {
			throw new ModelServiceException (OP_UPDATE, PERSON, "The person with id " + fromPerson.getId () + " does not exist.");
		}

		person.update (fromPerson, mAuthService);
	}
	
	public void createAndFund (String account, int amount) throws ModelServiceException {
		if (account == null) {
			return;
		}
		
		try {
			mLedger.createAccount (account);
			Transaction t = new Transaction ("fund" + account, MASTER_ACCOUNT, account, amount);
			mLedger.processTransaction (t);
		} catch (LedgerException lx) {
			throw new ModelServiceException (OP_CREATE, ACCOUNT, lx.getMessage ());
		}
	}
	

	public void defineResident (Resident resident, String authToken) 
			throws ModelServiceException
	{
		definePerson (resident, authToken);
		if (resident.getAccount () != null) {
			createAndFund (resident.getAccount ().getAddress (), INITIAL_PERSON_BALANCE);
		}
		if (resident.getRole () != null) {
			checkAccess (authToken, OP_ADD, ROLE);
		}
	}

	public void updateResident (String id, String name, String biometric, String phone, String role, Location location, String account, String authToken) 
			throws ModelServiceException
	{
		Resident residentTemplate = new Resident (id, name)
				.withBiometric (mAuthService, biometric)
				.withPhone (phone)
				.withRole (getAuthService (), role)
				.withLocation (location)
				.withAccount (account);
		
		updatePerson (residentTemplate, authToken);
	}

	public void defineVisitor (Visitor visitor, String authToken) 
		throws ModelServiceException
	{
		definePerson (visitor, authToken);
	}

	public void updateVisitor (String id, String biometric, Location location, String authToken) 
		throws ModelServiceException
	{
		Visitor visitorTemplate = new Visitor (id)
				.withBiometric (mAuthService, biometric)
				.withLocation (location);
		updatePerson (visitorTemplate, authToken);
	}

	public String showPerson (String id, String authToken) throws ModelServiceException {
		checkAccess (authToken, OP_SHOW, PERSON);
		Person<?> person = mPeople.get (id);
		if (person == null) {
			throw new ModelServiceException (OP_SHOW, PERSON, "The person with id " + id + " does not exist.");
		}

		return (person.show ());
	}
	
	public City getCity (String id) {
		return (mCities.get (id));
	}
	
	public Person<?> getPerson (String id) {
		return (mPeople.get (id));
	}

	public Iterator<String> listCities () {
		return (mCities.keySet ().iterator ());
	}
	
	public Device<?> getDevice (String combinedId) {
		String cityId = CommandParser.parseCityId (combinedId);
		City city = getCity (cityId);
		Device<?> ret = null;
		
		if (city != null) {
			ret = city.getDevice (CommandParser.parseObjectId (combinedId));
		}
		
		return (ret);		
		
	}
	
	public Iterator<String> listPeople () {
		return (mPeople.keySet ().iterator ());
	}
	
	public Ledger getLedger () {
		return (mLedger);
	}
	
	private final Set<EventObserver> mObservers = new HashSet<> ();
	
	@Override
	public void attach (EventObserver observer) {
		mObservers.add (observer);
		
	}

	@Override
	public void detach (EventObserver observer) {
		mObservers.remove (observer);
		
	}

	@Override
	public void notifyEvent (Device<?> device, SensorEvent event) {
		for (EventObserver observer: mObservers) {
			observer.event (device, event);
		}
	}
	
//	private final static class StringPair {
//		private final String mS1;
//		private final String mS2;
//		
//		private StringPair (String s1, String s2) {
//			mS1 = s1;
//			mS2 = s2;
//		}
//		
//		@Override
//		public int hashCode () {
//			return (mS1.hashCode () + mS2.hashCode ());
//		}
//		
//		@Override
//		public boolean equals (Object obj) {
//			StringPair otherPair = (StringPair) obj;
//			return (mS1.equals (otherPair.mS1) && mS2.equals (otherPair.mS2)) ;
//		}
//	}
//	
	private final Map<String, String> OPERATIONS_TO_PERMISSIONS = Map.of (
			PERSON, PERMISSION_USERS,
			CITY, PERMISSION_CITIES,
			DEVICE, PERMISSION_DEVICES,
			ROBOT, PERMISSION_ROBOTS,
			CAR, PERMISSION_CARS,
			EVENT, PERMISSION_EVENT,
			ROLE, PERMISSION_ENTITLEMENTS
	);
	
	private final void checkAccess (String token, String op, String subject) throws ModelServiceException {
		checkAccess (token, op, subject, null);
	}
	
	private final void checkAccess (String token, String op, String subject, String resource) throws ModelServiceException {
		if (resource != null) {
			try {
				getAuthService ().checkAccess (token, resource);
				return;
			} catch (AuthenticationException ax) {
				// nothing; check permission
			}			
		}
		
		String permission = OPERATIONS_TO_PERMISSIONS.get (subject);

		if (permission == null) {
			throw new ModelServiceException (op, subject, "no permission found for this operation: " + subject);			
		}
		
		try {
			getAuthService ().checkAccess (token, permission);
		} catch (AuthenticationException ax) {
			throw new ModelServiceException (op, subject, ax.getMessage ());
		}
	}
	
}
