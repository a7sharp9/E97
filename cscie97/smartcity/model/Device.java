package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

import java.io.PrintStream;

import cscie97.smartcity.authentication.Resource;

/**
 * The base class for all IoT device objects 
 */
public abstract class Device <T extends Device <T>> extends Resource<T> implements ModelServiceConstants {

	/**
	 * mandatory sensors
	 */
	
	private final Microphone mMicrophone = new Microphone ();
	private final Camera mCamera = new Camera ();
	private final Thermometer mThermometer = new Thermometer ();
	private final CO2Meter mCO2Meter = new CO2Meter ();

	/**
	 * a flag indicating if the device is switched on
	 */
	private Boolean mEnabled;

	/**
	 * current device coordinates
	 */
	private Location mLocation;

	/**
	 * indicates if the device is operatonal
	 */
	private Status mStatus = Status.READY;
	
	private final String mCity;
	
	/**
	 * The last sensor event received or emulated for this device
	 */
	private SensorEvent mLastEvent;

	public Device (String city, String id) {
		super (id);
		mCity = city;
	}
	
	public abstract String getType ();
	
	protected abstract T getThis ();
	
	public T withEnabled (Boolean enabled) {
		if (enabled != null) {
			mEnabled = enabled;
		}
		
		return (getThis ());
	}
	
	public T withStatus (Status status) {
		if (status != null) {
			mStatus = status;
		}
		
		return (getThis ());
	}
	
	public T withLocation (Location location) {
		if (location != null) {
			mLocation = location;
		}
		
		return (getThis ());
	}
	
	public void update (Device<?> fromDevice)
	{
		withStatus (fromDevice.getStatus ()).
		withEnabled (fromDevice.isEnabled ()).
		withLocation (fromDevice.getLocation ());
	}

	public abstract void validate () throws ModelServiceException;

	public final Microphone getMicrophone () {
		return mMicrophone;
	}

	public final Camera getCamera () {
		return mCamera;
	}

	public final Thermometer getThermometer () {
		return mThermometer;
	}

	public final CO2Meter getCO2Meter () {
		return mCO2Meter;
	}

	public final Boolean isEnabled () {
		return mEnabled;
	}

	public final Location getLocation () {
		return mLocation;
	}

	public final Status getStatus () {
		return mStatus;
	}
	
	public final String show () {
		return (show (""));
	}
	
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder ();
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("device: ").append (getId ()).append (lineSeparator ());
		sb.append (prefix).append ("type: ").append (getType ()).append (lineSeparator ());
		sb.append (prefix).append ("enabled: ").append (isEnabled ()).append (lineSeparator ());
		sb.append (prefix).append ("status: ").append (getStatus ()).append (lineSeparator ());
		sb.append (prefix).append ("location: ").append (getLocation ()).append (lineSeparator ());
		if (mLastEvent != null) {
			sb.append (prefix).append ("last event:").append (lineSeparator ());
			sb.append (mLastEvent.show (prefix + "  "));
		}
		
		return (sb.toString ());
	}

	public final SensorEvent getLastEvent () {
		return mLastEvent;
	}

	public final void setLastEvent (ModelService service, SensorEvent lastEvent) {
		mLastEvent = lastEvent;
		service.notifyEvent (this, lastEvent);
	}
	
	/**
	 * A placeholder for sending a message to the device speaker
	 * currently just prints the message to the specified stream
	 * @param message the text to be run through the text-to-speech processor and emitted from speaker
	 */
	public final void broadcast (PrintStream ps, String message) {
		if (ps == null) {
			ps = System.out;
		}
		
		ps.println (">>>>>> device " + getId () + " says : " + message);
	}
	
	public String getCity () {
		return (mCity);
	}
	
	public final String getCombinedId () {
		return (CommandParser.reconstituteCombinedId (getCity (), getId ()));
	}
}
