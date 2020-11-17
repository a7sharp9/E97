package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

/**
 * abstracts the events which are either generated by sensors in the devices
 * or emulated by the administrator
 */
public class SensorEvent implements ModelServiceConstants {
	
	/**
	 * the event type
	 */
	private final String mType;
	
	/**
	 * The string value representing the event
	 */
	private final String mValue;
	
	/**
	 * The optional subject of the event - an identifier of a person
	 */
	private String mSubject;
	
	public SensorEvent (String type, String value) {
		mType = type;
		mValue = value;
	}
	
	public SensorEvent withSubject (String subject) {
		if (subject != null) {
			mSubject = subject;
		}
		
		return (this);
	}

	public final String getType () {
		return mType;
	}

	public final String getValue () {
		return mValue;
	}

	public final String getSubject () {
		return mSubject;
	}

	@Override
	public String toString () {
		return (show (""));
	}
	
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder ();
		
		sb.append (prefix).append ("type: ").append (mType).append (lineSeparator ());
		if (mValue != null) {
			sb.append (prefix).append ("value: ").append (mValue).append (lineSeparator ());
		}
		if (mSubject != null) {
			sb.append (prefix).append ("subject: ").append (mSubject).append (lineSeparator ());
		}
		
		return (sb.toString ());
	}
	

}