package cscie97.smartcity.model;

/**
 * An instance of a Vehicle representing a public bus
 */
public class Bus extends Vehicle<Bus> implements ModelServiceConstants {
	public Bus (String city, String id) {
		super (city, id);
	}
	
	protected Bus getThis () {
		return (this);
	}

	@Override
	public String getType () {
		return (BUS);
	}
}
