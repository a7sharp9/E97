package cscie97.smartcity.controller;

import cscie97.smartcity.model.Car;
import cscie97.smartcity.model.ModelServiceConstants;

/**
 * This command is invoked upon receiving a CO2 reading from a sensor
 * Decides if the pollution level in the city is sufficient to disable
 * all private cars. The task of accumulating the events and
 * making the decision is encapsulated in an instance of CO2LevelMonitor
 */
public class CO2Command implements Command, ModelServiceConstants {

	/**
	 * 
	 */
	private final String mCity;
	private final int mLevel;
	
	public CO2Command (String city, int level) {
		mLevel = level;
		mCity = city;
	}

	@Override
	public void execute (Controller controller) throws ControllerException {
		CO2LevelMonitor monitor = controller.getMonitor (mCity);
		
		// notify the monitor
		monitor.notifyCO2 (mLevel);
		
		Car template = null;
		
		// monitor will say if enough events accumulated to change the state of car objects
		if (monitor.needEnable ()) {
			template = new Car (mCity, null).withEnabled (true);
			monitor.setDisabled (false);
		} else if (monitor.needDisable ()) {
			template = new Car (mCity, null).withEnabled (false);
			monitor.setDisabled (true);
		}
		
		// take action if necessary
		if (template != null) {
			new CityCommand (mCity, template, null, new DeviceTypeFilter (mCity, CAR)).execute (controller);
		}
	}
	
	public String toString () {
		return ("Notified the controller of CO2 level " + mLevel + " in " + mCity);
	}

}
