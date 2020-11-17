package cscie97.smartcity.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import cscie97.smartcity.model.City;
import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.ModelService;

/**
 * An instance of a device filter that splits all devices of a certain type in the city
 * in halves and returns either one
 */
public class HalfCityDevicesFilter extends CityDevicesFilter {
	
	/**
	 * depending on this, collect either odd- or even-indexed devices of this type
	 */
	private final boolean mOtherHalf;
	
	/**
	 * The type of the device to filter
	 */
	private final String mType;

	public HalfCityDevicesFilter (String city, String type, boolean otherHalf) {
		super (city);
		mType = type;
		mOtherHalf = otherHalf;
	}

	@Override
	public Collection<Device <?>> filter (Controller controller) {
		ModelService service = controller.getModelService ();
		City city = service.getCity (getCity ());
		Iterator<Device<?>> devices = city.listDevicesType (mType);
		
		Collection<Device<?>> ret = new ArrayList<> ();
		
		try {
			while (true) {
				Device<?> thisDevice = devices.next ();
				if (!mOtherHalf) {
					// add this, skip next
					ret.add (thisDevice);
					devices.next ();
				} else {
					// skip this, add next
					thisDevice = devices.next ();
					ret.add (thisDevice);
				}
			}
		} catch (NoSuchElementException nsex) {
			// nothing; thrown by next(), this is the normal end of the loop
		}
		
		return (ret);
	}

}
