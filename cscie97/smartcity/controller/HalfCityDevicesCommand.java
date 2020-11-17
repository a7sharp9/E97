package cscie97.smartcity.controller;

import cscie97.smartcity.model.Device;

/**
 * Filters half of the devices of a certain type in the city and directs a command to them
 */
public class HalfCityDevicesCommand extends CityCommand {
	
	public HalfCityDevicesCommand (String city, Device <?> template, String text, boolean otherHalf) {
		super (city, template, text,
				new HalfCityDevicesFilter (city, template.getType (), otherHalf));
	}

		
	@Override
	public String toString () {
		return ("apply " + super.toString () + " to half of the devices");
	}

}
