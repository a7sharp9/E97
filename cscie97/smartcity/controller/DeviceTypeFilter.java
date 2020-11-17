package cscie97.smartcity.controller;

import java.util.Collection;
import java.util.HashSet;

import cscie97.smartcity.model.Device;

/**
 * An instance of the device filter coolecting all devices of a certain type in a city
 */
public class DeviceTypeFilter extends CityDevicesFilter {

	private final String mType;
	
	public DeviceTypeFilter (String city, String type) {
		super (city);
		mType = type;
	}

	@Override
	public Collection<Device<?>> filter (Controller controller) {
		Collection<Device<?>> ret = new HashSet<> ();
		controller.getModelService ()
				.getCity (getCity ())
				.listDevices ()
				.forEachRemaining (d -> {if (mType == null || mType.equalsIgnoreCase (d.getType ())) ret.add (d);});
		return (ret);
	}

}
