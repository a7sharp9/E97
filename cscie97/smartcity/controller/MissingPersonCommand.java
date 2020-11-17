package cscie97.smartcity.controller;

import java.util.Formatter;

import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.Location;
import cscie97.smartcity.model.ModelService;
import cscie97.smartcity.model.Person;

/**
 * A command to respond to a missing person report. Locates the person
 * and emits a message with the location
 */
public class MissingPersonCommand extends PersonCommand implements ControllerConstants {

	private final Device<?> mDevice;
	public MissingPersonCommand (Device<?> device, String person) {
		super (person);
		mDevice = device;
	}

	public void execute (Controller controller) throws ControllerException {
		ModelService service = controller.getModelService ();
		Person<?> person = service.getPerson (getPerson ());
		Location location = person.getLocation ();
		StringBuilder text = new StringBuilder ();
		new Formatter (text).format (
				MISSING_PERSON_BROADCAST_FORMAT, getPerson (), location.getLat (), location.getLon ()
				).close ();
		new BroadcastCommand (mDevice, text.toString ()).execute (controller);	
	}
	
	@Override
	public String toString () {
		return ("Person " + getPerson () + " located, reporter advised to stay at " + mDevice.getLocation ().toString ());
	}


}
