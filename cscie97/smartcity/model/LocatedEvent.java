package cscie97.smartcity.model;

public class LocatedEvent extends SensorEvent {

	private Location mLocation;
	
	public LocatedEvent (String type, String value) {
		super (type, value);
	}
	
	public Location getLocation () {
		return (mLocation);
	}
	
	public LocatedEvent withLocation (Location location) {
		if (location != null) {
			mLocation = location;
		}
		
		return (this);
	}

}
