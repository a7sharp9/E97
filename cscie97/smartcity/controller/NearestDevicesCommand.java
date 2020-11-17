package cscie97.smartcity.controller;

import java.util.Collection;

import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.Location;

/**
 * A command that is directed at no more than the specified number of devices of a
 * certain type nearest the specified location
 */
public class NearestDevicesCommand implements Command {
	private final Device<?> mTemplate;
	private final Location mLocation;
	private final int mNumber;

	public NearestDevicesCommand (Location location, Device <?> template, int number) {
		mLocation = location;
		mTemplate = template;
		mNumber = number;
	}

	@Override
	public void execute (Controller controller) throws ControllerException {
		Collection<Device<?>> devices = new NearestDevicesFilter (mLocation, mTemplate.getType (), mNumber).filter (controller);
		
		for (Device<?> device: devices) {
			new UpdateDeviceCommand (device, mTemplate).execute (controller);
		}
	}
	
	@Override
	public String toString () {
		return ("apply change state command to devices nearest to " + mLocation.toString ());
	}

}
