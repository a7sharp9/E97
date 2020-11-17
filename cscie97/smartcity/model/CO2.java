package cscie97.smartcity.model;

public class CO2 extends SensorEvent {
	public CO2 (int level) {
		super (CO2, String.valueOf (level));
	}
}
