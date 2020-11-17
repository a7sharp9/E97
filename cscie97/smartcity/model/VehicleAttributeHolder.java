package cscie97.smartcity.model;

/**
 * A helper class to hold the vehicle attributes for update operation
 */
public class VehicleAttributeHolder extends Vehicle<VehicleAttributeHolder> {
	public VehicleAttributeHolder (String city, String id) {
		super (city, id);
	}
	
	protected VehicleAttributeHolder getThis () {
		return (this);
	}

	@Override
	public String getType () {
		return (null);
	}
	
	
}
