package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

/**
 * an object abstracting the functionality of an IoT robot.
 * is capable of performing activities
 */
public class Robot extends PayableDevice<Robot> {

	public Robot (String city, String id) {
		super (city, id);
	}

	protected Robot getThis () {
		return (this);
	}

	
	public void validate () {
		
	}
	
	/**
	 * the current activity
	 */
	private String mActivity;
	
	public final String getActivity () {
		return mActivity;
	}

	public Robot withActivity (String activity) {
		if (activity != null) {
			mActivity = activity;
		}
		
		return (this);
	}
	
	@Override
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("activity: ").append (getActivity ()).append (lineSeparator ());
		return (sb.toString ());
	}

	@Override
	public void update (Device <?> fromDevice) {
		super.update (fromDevice);
		
		if (fromDevice instanceof Robot) {
			Robot fromRobot = (Robot) fromDevice;
			withActivity (fromRobot.getActivity ());
		}
	}
	
	public String getType () {
		return (ROBOT);
	}

}
