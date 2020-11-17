package cscie97.smartcity.model;

public interface EventSubject {
	
	public void attach (EventObserver observer);
	public void detach (EventObserver observer);
	public void notifyEvent (Device<?> device, SensorEvent event);

}
