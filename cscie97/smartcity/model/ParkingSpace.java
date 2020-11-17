package cscie97.smartcity.model;

/**
 * an object abstracting the functionality of an IoT parking space.
 * is capable of changing its hourly rate
 */
import static java.lang.System.lineSeparator;

public class ParkingSpace extends PayableDevice<ParkingSpace> {

	public ParkingSpace (String city, String id) {
		super (city, id);
	}

	protected ParkingSpace getThis () {
		return (this);
	}

	
	public void validate () {
		
	}
	
	/**
	 * hourly rate for using this space
	 */
	private int mRate;
	
	public final Integer getRate () {
		return mRate;
	}

	private String mOccupiedBy = null;
	
	public final String getOccupiedBy () {
		return mOccupiedBy;
	}

	public ParkingSpace withRate (Integer rate) {
		if (rate != null) {
			mRate = rate;
		}
		
		return (this);
	}
	
	public ParkingSpace withOccupiedBy (String ccupiedBy) {
		if (ccupiedBy != null) {
			mOccupiedBy = ccupiedBy;
		}
		
		return (this);
	}
	
	@Override
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("rate: ").append (getRate ()).append (lineSeparator ());
		sb.append (prefix).append ("occupied by: ").append (getOccupiedBy ()).append (lineSeparator ());
		return (sb.toString ());
	}

	@Override
	public void update (Device <?> fromDevice) {
		super.update (fromDevice);
		
		if (fromDevice instanceof ParkingSpace) {
			ParkingSpace fromSpace = (ParkingSpace) fromDevice;
			withRate (fromSpace.getRate ()).withOccupiedBy (fromSpace.getOccupiedBy ());
		}
	}
	
	@Override
	public String getType () {
		return (PARKING_SPACE);
	}
	

}
