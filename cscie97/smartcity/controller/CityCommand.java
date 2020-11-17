package cscie97.smartcity.controller;

import java.util.Iterator;

import cscie97.smartcity.model.City;
import cscie97.smartcity.model.Device;

/**
 * The command directed to all or some devices registered to the city. The list of devices
 * is obtained through a supplied instance of CityDevicesFilter. Capable of changing
 * the state of the device according to a template and emitting a message through speakers
 */
public class CityCommand implements Command {
	/**
	 * The city id
	 */
	private final String mCity;
	
	/**
	 * The template with the attributes needed to be set for devices; can be null
	 */
	private final Device<?> mTemplate;
	
	/**
	 * The text to be emitted trhough speakers; can be null
	 */
	private final String mText;
	
	/**
	 * The instance of device filter; can be null - in that cases applies to all devices
	 */
	private final CityDevicesFilter mFilter;

	public CityCommand (String city, Device<?> template, String text, CityDevicesFilter filter) {
		mCity = city;
		mTemplate = template;
		mText = text;
		mFilter = filter;
	}

	@Override
	public void execute (Controller controller) throws ControllerException {
		City city = controller.getModelService ().getCity (mCity);
		Iterator<Device<?>> devices = null;
		
		// Filter the devices
		if (mFilter == null) {
			devices = city.listDevices ();
		} else {
			devices = mFilter.filter (controller).iterator ();
		}
		
		while (devices.hasNext ()) {
			Device<?> device = devices.next ();
			// change state if necessary
			if (mTemplate != null) {
				new UpdateDeviceCommand (device, mTemplate).execute (controller);
			}
			// emit message if necessary
			if (mText != null) {
				new BroadcastCommand (device, mText).execute (controller);
			}
		}
	}
	
		
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder ();
		
		if (mTemplate != null) {
			sb.append ("changing state of ").append (mTemplate.getType ()).append (", ");
		}
		
		if (mText != null) {
			sb.append ("broadcasting ").append (mText).append (", ");
		}
		
		return (sb.toString ());
	}


}
