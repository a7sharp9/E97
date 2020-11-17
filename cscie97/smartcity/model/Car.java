package cscie97.smartcity.model;

/**
 * An instance of a Vehicle representing a public bus
 */
public class Car extends Vehicle<Car> implements ModelServiceConstants {
	public Car (String city, String id) {
		super (city, id);
	}
	
	protected Car getThis () {
		return (this);
	}

	@Override
	public String getType () {
		return (CAR);
	}
	
}
