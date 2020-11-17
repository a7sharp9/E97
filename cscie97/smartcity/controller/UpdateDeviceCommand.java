package cscie97.smartcity.controller;

import cscie97.smartcity.model.CommandParser;
import cscie97.smartcity.model.Device;

/**
 * Applies changes to some or all of the device's attributes contained in the template
 */
public class UpdateDeviceCommand extends DeviceCommand {
	
	private Device<?> mTemplate;

	public UpdateDeviceCommand (Device<?> device, Device <?> template) {
		super (device);
		mTemplate = template;
	}

	@Override
	public void execute (Controller controller) throws ControllerException {
		getDevice ().update (mTemplate);
	}

	@Override
	public String toString () {
		return ("updating state of device " + CommandParser.reconstituteCombinedId (getDevice ().getCity (), getDevice ().getId ()));
	}

}
