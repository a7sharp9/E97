package cscie97.smartcity.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cscie97.smartcity.model.City;
import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.Location;
import cscie97.smartcity.model.ModelService;

/**
 * Filters devices that are nearest a specified location. No more than
 * a specified number will be returned (but may be less if there are not enough
 * devices of this type registered with the city)
 */
public class NearestDevicesFilter implements DeviceFilter {

	private final int mNumber;
	private final String mType;
	private final Location mLocation;
	
	public NearestDevicesFilter (Location location, String type, int number) {
		mLocation = location;
		mNumber = number;
		mType = type;
	}

	private final double tryAddClosest (Map<Double, Device<?>> map, Device<?> device, double max) {
		double dist = mLocation.haversineDistance (device.getLocation ());
		double ret = max;
		if (map.size () < mNumber) { // just add
			map.put (dist, device);
			ret = Math.max (max, dist);
		} else { // evict the farthest
			if (dist < max) {
				map.remove (max);
				map.put (dist, device);
				ret = map.keySet ().stream ().max (Double::compareTo).get ();
			}
		}
		
		return (ret);
	}
	
	@Override
	public Collection<Device <?>> filter (Controller controller) {
		ModelService service = controller.getModelService ();
		
		Map <Double, Device<?>> ret = new HashMap<> ();
		
		Iterator<String> cities = service.listCities ();
		
		double max = 0;
		
		while (cities.hasNext ()) {
			City city = service.getCity (cities.next ());
			Iterator<Device<?>> devices = city.listDevices ();
			while (devices.hasNext ()) {
				Device<?> device = devices.next ();
				if (mType == null || mType.equalsIgnoreCase (device.getType ())) {
					max = tryAddClosest (ret, device, max);
				}
			}
		}
		
		return (ret.values ());
	}

}
