package cscie97.smartcity.controller;

/**
 * A functional interface to filter city devices
 */
public abstract class CityDevicesFilter implements DeviceFilter {

	private final String mCity;
	
	public CityDevicesFilter (String city) {
		mCity = city;
	}

	public String getCity () {
		return (mCity);
	}
}
