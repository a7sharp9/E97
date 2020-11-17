package cscie97.smartcity.model;

public interface EventObserver {
	public void event (Device<?> device, SensorEvent event);
}
