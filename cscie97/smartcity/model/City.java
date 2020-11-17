package cscie97.smartcity.model;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import cscie97.smartcity.authentication.Resource;
import cscie97.smartcity.ledger.Account;

import static java.lang.System.lineSeparator;

/**
 * An abstraction of the smart city.
 */
public class City extends Resource<City> implements ModelServiceConstants {

	@Override
	protected City getThis () {
		return (this);
	}



	/**
	 * A human-readable name
	 */
	private String mName;

	/**
	 * The radius (in km) of the circle with the center in the
	 * city's location that defines its boundaries
	 */
	private Double mRadius;

	/**
	 * The blockchain account of the city
	 */
	private Account mAccount;

	/**
	 * The coordinates of the sity center
	 */
	private Location mLocation;

	private final HashMap<String, Device<?>> mDevices;
	
	public City (String id) {
		super (id);
		mDevices = new HashMap <> ();
	}

	public final String getName() {
		return mName;
	}

	public final double getRadius() {
		return mRadius;
	}

	public final Account getAccount() {
		return mAccount;
	}

	public final Location getLocation() {
		return mLocation;
	}
	
	public final City withLocation (Location location) {
		if (location != null) {
			mLocation = location;
		}
		
		return (this);
	}
	
	public final City withName (String name) {
		if (name != null) {
			mName = name;
		}
		
		return (this);
	}
	
	public final City withAccount (String account) {
		if (account != null) {
			mAccount = new Account (account);
		}
		
		return (this);
	}
	
	public final City withRadius (Double radius) {
		if (radius != null) {
			mRadius = radius;
		}
		
		return (this);
	}

	/**
	 * Validates the city object. Throws an exception if validation fails
	 * @throws ModelServiceException
	 */
	public final void validate () throws ModelServiceException {
		
	}

	/**
	 * Lists all IoT devices registered with the city.
	 * @return the iterator over device objects
	 */
	public Iterator<Device<?>> listDevices () {
		return (mDevices.values().iterator());
	}
	
	/**
	 * Lists all IoT devices registered with the city.
	 * @return the iterator over device objects
	 */
	public Iterator<Device<?>> listDevicesType (String type) {
		return (mDevices.values().stream ()
				.filter (d -> type == null || type.equalsIgnoreCase (d.getType ()))
				.collect (Collectors.toList ())
				.iterator ()
				);
	}
	
	/**
	 * Looks up an IoT device by the identifier
	 * @param id
	 * @return the device object; null if no such device defined in this city
	 */
	public Device<?> getDevice (String id) {
		return (mDevices.get (id));
	}
	
	/**
	 * Collects all IoT devices of a certain type
	 * @param the class of the requested type of device
	 * @return a list of all devices of this type
	 */
	@SuppressWarnings("unchecked")
	public <T extends Device<T>> List<T> getDevices (Class<T> deviceClass) {
		return (
				mDevices.values ().stream ()
				.filter (d -> d.getClass ().equals (deviceClass))
				.map (d -> (T) d)
				.collect (Collectors.toList())
				);
	}
	
	/**
	 * Given a constructed device object, performs validation and adds it to the
	 * map of devices for this city
	 * @param device the new device
	 * @param authToken the authorization token
	 * @throws ModelServiceException
	 */
	public void defineDevice (Device<?> device, String authToken) throws ModelServiceException {
		String deviceId = device.getId ();
		if (getDevice (deviceId) != null) {
			throw new ModelServiceException (OP_DEFINE, DEVICE, "The device with id " + deviceId + " already exists.");
		}

		device.validate ();
		mDevices.put (deviceId, device);
	}
	
	/**
	 * Given a constructed device object, finds a device with this identifier in the
	 * city and updates those parameters whic have been set in the incoming object
	 * @param device the container for the parameters
	 * @param authToken the authorization token
	 * @throws ModelServiceException
	 */
	public void updateDevice (Device<?> device, String authToken) throws ModelServiceException {
		Device<?> cityDevice = getDevice (device.getId ());
		if (cityDevice == null) {
			throw new ModelServiceException (OP_UPDATE, DEVICE, "The device with id " + device.getId () + " does not exist.");
		}

		cityDevice.update (device);
	}
	
	
	
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (prefix + "City " + getId () + ":").append (lineSeparator ());
		sb.append (prefix).append ("  name: ").append (mName).append (lineSeparator ());
		sb.append (prefix).append ("  account: ").append (mAccount).append (lineSeparator ());
		sb.append (prefix).append ("  radius: ").append (mRadius).append (lineSeparator ());
		sb.append (prefix).append ("  location: ").append (mLocation).append (lineSeparator ());
		
		return (sb.toString());
	}
	
	@Override
	public String toString () {
		return (show (""));
	}
	
	@Override
	public String getType () {
		return (CITY);
	}

	@Override
	public final String getCombinedId () {
		return (getId ());
	}

}
