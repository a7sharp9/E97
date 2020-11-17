package cscie97.smartcity.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cscie97.smartcity.model.CommandParser.ParserException;

public class EventFactory {

	/**
	 * Tries to parse location from the event text; if can't, returns the location of the reporting device
	 */
	private static Location extractLocation (Device<?> device, String value) {
		Map<String, List<String>> parsedArgs = CommandParser.parseArgs (value.split (" "), 0, Set.of ("lat", "long"));
		Location loc = null;
		try {
			loc = CommandParser.parseLocation (parsedArgs);
		} catch (ParserException px) {
			// nothing
		}
		
		if (loc == null) {
			loc = device.getLocation ();
		}
		
		return (loc);
	}
	
	/**
	 * Performs rudimentary parsing of the event text value and constructs appropriate event objects
	 */
	public static SensorEvent parseEvent (Device<?> device, String sensorType, String value, String subject) {
		if (sensorType == null || value == null) {
			return (null);
		}
		
		value = value.toLowerCase ().replaceAll ("\"", "");
		
		if (sensorType.equalsIgnoreCase ("co2meter")) {
			return (new CO2 (Integer.valueOf (value)));
		} else if (sensorType.equalsIgnoreCase ("camera")) {
			
			if (value.contains ("fire") 
					|| value.contains ("flood")  
					|| value.contains ("earthquake") 
					|| (value.contains ("severe") && value.contains ("weather"))
					|| value.contains ("accident")) {
				return (new Emergency (value).withLocation (extractLocation (device, value)));
			} else if (value.contains ("littering")) {
				return (new Litter (null).withLocation (extractLocation (device, value)).withSubject (subject));
			} else if (value.contains ("person") && value.contains ("seen")) {
				return (new PersonSeen (value).withLocation (extractLocation (device, value)).withSubject (subject));
			} else if (value.contains ("board") && value.contains ("bus")) {
				return (new BoardBus (value).withSubject (subject));
			} else if (value.contains ("vehicle") && value.contains ("parked")) {
				String [] tokens = value.split (" ");
				return (new Parking (Integer.valueOf (tokens[tokens.length - 2])).withSubject (tokens[1]));
			}
		} else if (sensorType.equalsIgnoreCase ("microphone")) {
			if (value.contains ("glass") && (value.contains ("break") || value.contains ("broke"))) {
				return (new BrokenGlass ().withLocation (extractLocation (device, value)));
			} else if (value.contains ("help") && value.contains ("find")) {
				String [] tokens = value.split (" ");
				return (new MissingChild (null).withLocation (extractLocation (device, value)).withSubject (tokens[tokens.length - 1]));
			} else if (value.contains ("bus") && value.contains ("go to")) {
				return (new BusRoute (value.substring (value.indexOf ("go to") + "go to".length () + 1)).withSubject (subject));
			} else if (value.contains ("mov") && value.contains ("showing")) {
				return (new MovieInfo ().withSubject (subject));
			} else if (value.contains ("reserve")) {
				return (new Reservation (value).withSubject (subject));
			}
		} else if (sensorType.equals ("thermometer")) {
			return (new Temperature (value)).withSubject (subject);
		}
		
		return (null);
	}


}
