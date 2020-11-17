package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

import java.util.Iterator;

import cscie97.smartcity.authentication.AuthenticationService;
import cscie97.smartcity.authentication.User;

/**
 * The base class for all objects abstracting persons
 *
 */
public abstract class Person<T extends Person<T>> extends User<T> implements ModelServiceConstants {

	/**
	 * current location
	 */
	private Location mLocation;
	
	public Person (String id) {
		super (id);
	}
	
	public Person (String id, String name) {
		super (id, name);
	}
	
	public T withLocation (Location location) {
		if (location != null) {
			mLocation = location;
		}
		
		return (getThis ());
	}

	public T withName (String name) {
		return (withDescription (name));
	}

	public final Location getLocation () {
		return mLocation;
	}
	
	public void validate () throws ModelServiceException {
		
	}
	
	public void update (Person<?> fromPerson, AuthenticationService service) throws ModelServiceException {
		for (Iterator<String> biometric = fromPerson.getBiometric (); biometric.hasNext (); ) {
			withBiometric (service, biometric.next ());
		}
		withLocation (fromPerson.mLocation);		
	}
	
	public final String show () {
		return (show (""));
	}
	
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder ();
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("person: ").append (getId ()).append (lineSeparator ());
		sb.append (prefix).append ("type: ").append (getType ()).append (lineSeparator ());
		sb.append (prefix).append ("name: ").append (getDescription ()).append (lineSeparator ());
		for (Iterator<String> biometric = getBiometric (); biometric.hasNext (); ) {
			sb.append (prefix).append ("biometric: ").append (biometric.next ()).append (lineSeparator ());
		}
		sb.append (prefix).append ("location: ").append (getLocation ()).append (lineSeparator ());
		
		return (sb.toString ());
	}

}
 