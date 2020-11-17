package cscie97.smartcity.controller;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import cscie97.smartcity.model.Bus;
import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.InformationKiosk;
import cscie97.smartcity.model.LocatedEvent;
import cscie97.smartcity.model.Location;
import cscie97.smartcity.model.ModelServiceConstants;
import cscie97.smartcity.model.ParkingSpace;
import cscie97.smartcity.model.Person;
import cscie97.smartcity.model.PersonAttributeHolder;
import cscie97.smartcity.model.Robot;
import cscie97.smartcity.model.SensorEvent;

/**
 * The encapsulation of the process of transforming the events received by the
 * controller through subscription into command instances to be executed.
 */
public class CommandFactory implements ModelServiceConstants, ControllerConstants {

	private static CommandFactory mInstance = null;
	
	public final static synchronized CommandFactory instance () {
		if (mInstance == null) {
			mInstance = new CommandFactory ();
		}
		
		return (mInstance);
	}
	
	/**
	 * Functional interface to build the command depending on the event
	 * and the device sending it
	 */
	private interface Builder {
		public Command build (Device<?> device, SensorEvent event);
	}
	
	/**
	 * Depending on the accumulated CO2 measurements, enables or disables
	 * all cars in the city of the reporting device
	 */
	private static Builder CO2Builder = (device, event) -> {
		return (new CO2Command (device.getCity (), Integer.valueOf (event.getValue ())));
	};
	
	/**
	 * Reacts to an emergency. Emits a compound command, consisting of a broadcast on
	 * some or all city devices and a command to robots to attend to the emergency 
	 */
	private static Builder emergencyBuilder  = (device, event) -> {
		Location location = ((LocatedEvent) event).getLocation ();		
		String city = device.getCity ();
		StringBuilder activity = new StringBuilder ();
		new Formatter (activity).format (
				EMERGENCY_ACTIVITY_FORMAT, event.getValue (), location.getLat (), location.getLon ()
				).close ();
		Robot emergencyRobot = new Robot (city, null).withActivity (activity.toString ());

		if (event.getValue ().equalsIgnoreCase ("traffic_accident")) {
			// send 2 nearest robots
			return (new CompoundCommand (
						new BroadcastCommand (device, ACCIDENT_BROADCAST),
						new NearestDevicesCommand (location, emergencyRobot, 2)
						)
					);
		} else {
			StringBuilder text = new StringBuilder ();
			new Formatter (text).format (EMERGENCY_BROADCAST_FORMAT, event.getValue (), city).close ();
			Robot shelterRobot = new Robot (city, null).withActivity (SHELTER_ACTIVITY);
			// divide robots in half and send to either emergency or finding shelter
			return (new CompoundCommand (
						new CityCommand (city, null, text.toString (), null),
						new HalfCityDevicesCommand (city, emergencyRobot, null, true),
						new HalfCityDevicesCommand (city, shelterRobot, null, false)
					)
			);
		}
	};
	
	/**
	 * Reacts to a littering report. Directs one closest robot to remove litter
	 * and charges the person indicated in the event as the subject a littering fee
	 * (unless a visitor)
	 */
	private static Builder litterBuilder  = (device, event) -> {
		Location location = ((LocatedEvent) event).getLocation ();
		StringBuilder activity = new StringBuilder ();
		new Formatter (activity).format (
				LITTER_ACTIVITY_FORMAT, location.getLat (), location.getLon ()
				).close ();
		Robot litterRobot = new Robot (device.getCity (), null).withActivity (activity.toString ());
		return (new CompoundCommand (
					new BroadcastCommand (device, LITTER_BROADCAST),
					new NearestDevicesCommand (location, litterRobot, 1),
					new ChargePersonCommand (event.getSubject (), device, LITTER_FEE)
					)
				);
	};
	
	/**
	 * Reacts to a broken glass event. Sends the robot closest to the reporting device
	 * to clear up the glass
	 */
	private static Builder brokenGlassBuilder  = (device, event) -> {
		Location location = ((LocatedEvent) event).getLocation ();
		StringBuilder activity = new StringBuilder ();
		new Formatter (activity).format (
				BROKEN_GLASS_ACTIVITY_FORMAT, location.getLat (), location.getLon ()
				).close ();
		Robot glassRobot = new Robot (device.getCity (), null).withActivity (activity.toString ());
		return (new NearestDevicesCommand (location, glassRobot, 1));
	};
	

	/**
	 * Reacts to the report of person being cited. Updates the location of that
	 * person to the coordinates indicated in the event
	 */
	private static Builder personSeenBuilder  = (device, event) -> {
		Location location = ((LocatedEvent) event).getLocation ();
		Person<?> person = new PersonAttributeHolder (event.getSubject ()).withLocation (location);
		return (new UpdatePersonCommand (person));
	};
	
	/**
	 * Reacts to the report of a missing person. Directs the robot nearest to the last known coordinates 
	 * of the person and emits a message
	 */
	private static Builder missingChildBuilder  = (device, event) -> {
		String missingPerson = event.getSubject ();
		Location location = ((LocatedEvent) event).getLocation ();
		StringBuilder activity = new StringBuilder ();
		new Formatter (activity).format (
				MISSING_PERSON_ACTIVITY_FORMAT, missingPerson, location.getLat (), location.getLon ()
				).close ();
		Robot retrieveRobot = new Robot (device.getCity (), null).withActivity (activity.toString ());
		return (new CompoundCommand (
					new NearestDevicesCommand (location, retrieveRobot, 1),
					new MissingPersonCommand (device, missingPerson)
				)
			);
	};
	
	/**
	 * Reacts to a report of a vehicle having been parked in a paid space. Charges the
	 * account of the vehicle the per-hour fee for this space times the number of
	 * hours reported.
	 */
	private static Builder parkingBuilder  = (device, event) -> {
		ParkingSpace space = (ParkingSpace) device;
		int numHours = Integer.valueOf (event.getValue ());
		return (new ChargeDeviceCommand (event.getSubject (), space.getRate () * numHours));
	};
	
	/**
	 * Reacts to a request for bus route information
	 */
	private static Builder busRouteBuilder = (device, event) -> {
		String destination = event.getValue ();
		// some clever consulting with the route map
		return (new BroadcastCommand (device, GOES_TO + destination));
	};
	
	/**
	 * Reacts to a report of a person boarding the bus. Emits a welcome message
	 * and charges the person's account a set fee for using the bus
	 * (unless a visitor)
	 */
	private static Builder boardBusBuilder = (device, event) -> {
		String person = event.getSubject ();
		return (new CompoundCommand (
					new BroadcastCommand (device, GOOD_TO_SEE + person),
					new ChargePersonCommand (person, device, ((Bus) device).getFee ())
				)
		);		
	};
	
	/**
	 * Reacts to a request for information on the movie theater schedule. Emits a message
	 * and changes the image on the reporting kiosk
	 */
	private static Builder movieInfoBuilder = (device, event) -> {
		// some clever parsing of the question from event.getValue ()
		String text = "Casablanca is showing at 9 pm";
		String displayURL = "https://en.wikipedia.org/wiki/Casablanca_(film)#/media/File:CasablancaPoster-Gold.jpg";
		InformationKiosk kiosk = new InformationKiosk (device.getCity (), null).withImage (displayURL);
		return (new CompoundCommand (
					new BroadcastCommand (device, text),
					new UpdateDeviceCommand (device, kiosk)
				)
		);

	};
	
	/**
	 * Responds to a request for movie reservation. Emits a message and charges the person
	 * a reservation fee (unless a visitor)
	 */
	private static Builder reservationBuilder = (device, event) -> {
		return (new CompoundCommand (
					new BroadcastCommand (device, TICKETS_RESERVED),
					new ChargePersonCommand (event.getSubject (), device, RESERVATION_FEE)
				)
		);
	};
	
	/**
	 * Responds to a temperature reading. Emits a message on the reporting device, indicating whether
	 * a person has a fever
	 */
	private static Builder temperatureBuilder = (device, event) -> {
		return (new BroadcastCommand (device, "Your temperature is " + (Double.valueOf (event.getValue ()) > 100. ? "elevated" : "normal")));
	};
	
	/**
	 * All currently supported command builders, keyed by event type
	 */
	private static final Map<String, Builder> mBuilders = new HashMap <> ();
	static {
		mBuilders.put (CO2, CO2Builder);
		mBuilders.put (Emergency, emergencyBuilder);
		mBuilders.put (Litter, litterBuilder);
		mBuilders.put (BrokenGlass, brokenGlassBuilder);
		mBuilders.put (PersonSeen, personSeenBuilder);
		mBuilders.put (MissingChild, missingChildBuilder);
		mBuilders.put (Parking, parkingBuilder);
		mBuilders.put (BusRoute, busRouteBuilder);
		mBuilders.put (BoardBus, boardBusBuilder);
		mBuilders.put (MovieInfo, movieInfoBuilder);
		mBuilders.put (Reservation, reservationBuilder);
		mBuilders.put (Temperature, temperatureBuilder);
	}

	/**
	 * Creates a Command instance based on the type of the received event
	 */
	public Command build (Device<?> device, SensorEvent event) {
		Command ret = null;
		
		Builder	builder = mBuilders.get (event.getType ());
		
		if (builder != null) {
			ret = builder.build (device, event);
		}
		
		return (ret);
	}

}
