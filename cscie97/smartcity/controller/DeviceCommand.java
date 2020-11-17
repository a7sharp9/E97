package cscie97.smartcity.controller;

import cscie97.smartcity.model.Device;

/**
 * A parent class for all device commands.
 */
public abstract class DeviceCommand implements Command {
	/**
	 * The device which is the subject of the command
	 */
	private final Device<?> mDevice;

	public DeviceCommand (Device<?> device) {
		mDevice = device;
	}
	

	public Device<?> getDevice () {
		return (mDevice);
	}
}
