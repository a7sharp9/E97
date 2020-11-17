package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

/**
 * The parent class for all vehicle types
 */
public abstract class Vehicle<T extends Vehicle<T>> extends PayableDevice<T> {

	public Vehicle (String city, String id) {
		super (city, id);
	}

	public void validate () {
		
	}
	
	/**
	 * A string representing the current activity of the vehicle
	 */
	private String mActivity;

	/**
	 * The current capacity
	 */
	private Integer mCapacity;

	/**
	 * an hourly or one-time rate for use of this vehicle
	 */
	private Integer mFee;

	public T withCapacity (Integer capacity) {
		if (capacity != null) {
			mCapacity = capacity;
		}
		
		return (getThis ());
	}

	public T withFee (Integer fee) {
		if (fee != null) {
			mFee = fee;
		}
		
		return (getThis ());
	}
	
	public T withActivity (String activity) {
		if (activity != null) {
			mActivity = activity;
		}
		
		return (getThis ());
	}

	public final String getActivity () {
		return mActivity;
	}

	public final Integer getCapacity () {
		return mCapacity;
	}

	public final Integer getFee () {
		return mFee;
	}
	
	@Override
	public void update (Device <?> fromDevice) {
		super.update (fromDevice);
		
		if (fromDevice instanceof Vehicle) {
			Vehicle<?> fromVehicle = (Vehicle<?>) fromDevice;
			withActivity (fromVehicle.getActivity ())
			.withCapacity (fromVehicle.getCapacity ());
		}
	}

	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("type: ").append (getType ()).append (lineSeparator ());
		sb.append (prefix).append ("capacity: ").append (getCapacity ()).append (lineSeparator ());
		sb.append (prefix).append ("fee: ").append (getFee ()).append (lineSeparator ());
		sb.append (prefix).append ("activity: ").append (getActivity ()).append (lineSeparator ());
		
		return (sb.toString ());
	}

}
