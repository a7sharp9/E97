package cscie97.smartcity.controller;

import cscie97.smartcity.model.Device;

/**
 * Instance of the command interface capable of sending a text to be announced
 * on a device's speaker. 
 */
public class BroadcastCommand extends DeviceCommand {
	
	/**
	 * text to be announced
	 */
	private final String mText;

	public BroadcastCommand (Device<?> device, String text) {
		super (device);
		mText = text;
	}

	@Override
	public void execute (Controller controller) throws ControllerException {
		getDevice ().broadcast (controller.getPrintStream (), mText);
	}
	
	@Override
	public String toString () {
		return ("Sending text \"" + mText + "\" to " + getDevice ().getId ());
	}


}
