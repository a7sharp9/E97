package cscie97.smartcity.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * utility class to parse the command strings and extract options from it
 */
public class CommandParser implements ModelServiceConstants {
	
	/**
	 * a generic exception thrown by the parser
	 */
	public static class ParserException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParserException (String reason) {
			super (reason);
		}
	}
	
	/**
	 * thrown if the requested option is specified as a single-value, but
	 * has more than one token
	 */
	public static class NotASingleOption extends ParserException {
		private static final long serialVersionUID = 1L;

		public NotASingleOption (String optionName) {
			super ("Option " + optionName + " has more than one value.");
		}
		
	}
	
	/**
	 * thrown if the requested option is mandatory, but is not present in the
	 * command string or has no value string
	 */
	public static class NoOption extends ParserException {
		private static final long serialVersionUID = 1L;

		public NoOption (String optionName) {
			super ("Option " + optionName + " does not have a value.");
		}
		
	}
	
	/**
	 * thrown if the requested option is expected to contain an elementary type,
	 * but the value cannot be interpreted as that type
	 */
	public static class InvalidFormat extends ParserException {
		private static final long serialVersionUID = 1L;

		public InvalidFormat (String optionName) {
			super ("Value for " + optionName + " could not be parsed");
		}
		
	}
	
	/**
	 * Performs simple parsing of command lines;
	 * is capable of recognizing a token as an option word and collect all tokens
	 * that follow it into a list until next option word is encountered.
	 * @param args the list of command tokens
	 * @param the index of the argument in the list to start processing from
	 * @param the collection of tokens to be treated as option word
	 * @return a map of lists of non-option tokens keyed by the option word which 
	 * they followed in the argument list 
	 */
	public static Map<String, List<String>> parseArgs (String [] args, int fromArg, Set<String> options) {
		Map<String, List<String>>	parsed = new HashMap<> ();
		
		ArrayList<String> curOpts = null;
		for (int iArg = fromArg; iArg < args.length; iArg ++) {
			String arg = args[iArg];
			if (options.contains (arg)) {
				curOpts = new ArrayList<> ();
				parsed.put (arg, curOpts);
			} else if (curOpts != null) {
				curOpts.add (arg);
			}
		}
		
		return (parsed);
	}

	/**
	 * Given a combined <cityId>:<objectId> identifier, splits off the city id
	 * @param id combined identifier
	 * @return city id
	 */
	public static final String parseCityId (String id) {
		StringTokenizer st = new StringTokenizer (id, ":");
		return (st.nextToken ());
	}

	/**
	 * Given a combined <cityId>:<objectId> identifier, splits off the object id
	 * @param id combined identifier
	 * @return object idm or null if not present
	 */
	public static final String parseObjectId (String id) {
		StringTokenizer st = new StringTokenizer (id, ":");
		st.nextToken ();
		return (st.hasMoreTokens () ? st.nextToken () : null);
	}
	
	public static final String reconstituteCombinedId (String cityId, String deviceId) {
		String ret = cityId;
		if (deviceId != null) {
			ret += (":" + deviceId);
		}
		
		return (ret);
	}
	
	/**
	 * Tries to interpret the requested option as a floating point number
	 * @param valueName the name of the option
	 * @param parsedOptions the map of options keyed by name
	 * @return the interpreted elementary value
	 * @throws ParserException if the string cannot be parsed as the requested elementary type
	 */
	public static final Double parseDoubleValue (String valueName, Map<String, List<String>> parsedOptions) throws ParserException {
		String valueString = getSingleOption (valueName, parsedOptions, true);
		if (valueString == null) {
			return (null);
		}
		
		try {
			return (Double.parseDouble (valueString));
		} catch (NumberFormatException nfx) {
			throw new InvalidFormat (valueName);
		}		
	}
	
	/**
	 * Tries to interpret the requested option as a boolean flag
	 * @param valueName the name of the option
	 * @param parsedOptions the map of options keyed by name
	 * @return the interpreted elementary value
	 * @throws ParserException if the string cannot be parsed as the requested elementary type
	 */
	public static final Boolean parseBooleanValue (String valueName, Map<String, List<String>> parsedOptions) throws ParserException {
		String valueString = getSingleOption (valueName, parsedOptions, true);
		if (valueString == null) {
			return (null);
		}
		
		return (Boolean.parseBoolean (valueString));
	}
	
	/**
	 * Tries to interpret the requested option as an integer number
	 * @param valueName the name of the option
	 * @param parsedOptions the map of options keyed by name
	 * @return the interpreted elementary value
	 * @throws ParserException if the string cannot be parsed as the requested elementary type
	 */
	public static final Integer parseIntegerValue (String valueName, Map<String, List<String>> parsedOptions) throws ParserException {
		String valueString = getSingleOption (valueName, parsedOptions, true);
		if (valueString == null) {
			return (null);
		}
		
		try {
			return (Integer.parseInt (valueString));
		} catch (NumberFormatException nfx) {
			throw new InvalidFormat (valueName);
		}		
	}
	
	/**
	 * Tries to interpret the options for longitude and latitude as a location
	 * @param parsedOptions the map of options keyed by name
	 * @return the interpreted location
	 * @throws ParserException if the latitude or longitude could not be parsed
	 */
	public static final Location parseLocation (Map<String, List<String>> parsedOptions) throws ParserException {
		Location ret = null;
		Double latValue = parseDoubleValue (OPTION_LAT, parsedOptions);
		Double lonValue = parseDoubleValue (OPTION_LON, parsedOptions);
		
		if (latValue != null && lonValue != null) {	
			ret = new Location (latValue, lonValue);
		}
		
		return (ret);
	}
	
	/**
	 * Given a parameter name, assumes that it has only one element in the set of option values
	 * corresponding to this name and returns is
	 * @param optionName the name of the option
	 * @param parsedOptions the map of options keyed by name
	 * @param isOptional whether to ignore the possibility that this option might not be present;
	 * if false, will throw exception in this case, if true, will quietly return null
	 * @return the option value
	 * @throws ParserException
	 */
	public static String getSingleOption (String optionName, Map<String, List<String>> parsedOptions, boolean isOptional) throws ParserException {
		List<String> values = parsedOptions.get (optionName);
		if (values == null || values.size () <= 0) {
			if (isOptional) {
				return (null);
			} else {
				throw new NoOption (optionName);
			}
		} else if (values.size () > 1) {
			throw new NotASingleOption (optionName);
		}
		
		return (values.get (0));
	}
	
	/**
	 * Given a parameter name, combines its set of option values into a single string divided
	 * with single spaces and returns it
	 * corresponding to this name and returns is
	 * @param optionName the name of the option
	 * @param parsedOptions the map of options keyed by name
	 * @param isOptional whether to ignore the possibility that this option might not be present;
	 * @return the combined option value string
	 * @throws ParserException if the value is not present and not optional
	 */
	public static String getCombinedOption (String optionName, Map<String, List<String>> parsedOptions, boolean isOptional) throws ParserException {
		List<String> values = parsedOptions.get (optionName);
		if (values == null || values.size () <= 0) {
			if (isOptional) {
				return (null);
			} else {
				throw new NoOption (optionName);
			}
		}
		
		StringBuilder sb = new StringBuilder ();
		
		values.stream ().forEach (elem -> sb.append (elem).append (' '));
		sb.deleteCharAt (sb.length () - 1);
		
		return (sb.toString ());
	}

	

}
