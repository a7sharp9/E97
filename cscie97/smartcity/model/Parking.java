package cscie97.smartcity.model;

public class Parking extends SensorEvent {

	public Parking (int hours) {
		super (Parking, String.valueOf (hours));
	}

}
