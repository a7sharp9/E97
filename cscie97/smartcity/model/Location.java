package cscie97.smartcity.model;

import static java.lang.Math.toRadians;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.asin;
import static java.lang.Math.sqrt;

/**
 * A container for latitude and longitude coordinates
 */
public class Location {

	private final double mLon;
	private final double mLat;
	
	public Location (double lat, double lon) {
		mLat = lat;
		mLon = lon;
	}
	
	public double getLon () {
		return mLon;
	}

	public double getLat () {
		return mLat;
	}

	private final static double EARTH_DIAMETER_KM = 2 * 6371.; // mean btw polar and equatorial

	/**
	 * Computes the big circle distance between two locations defined as
	 * lat-lon pairs. Uses the haversine (square of sine of half-angle) formula.
	 * The latitude in longitude is assumed in degrees
	 * @param otherLoc the location to compute distance to
	 * @return the distance in kilometers
	 */
	public double haversineDistance (Location otherLoc) {
		double latRadians = toRadians (mLat);
		double otherLatRadians = toRadians (otherLoc.mLat);
		
		double deltaLat = otherLatRadians - latRadians; 
		double deltaLon = toRadians (otherLoc.mLon - mLon); 
		
		double sinHalfDeltaLat = sin (.5 * deltaLat);
		double sinHalfDeltaLon = sin (.5 * deltaLon);
		
		double det = sinHalfDeltaLat * sinHalfDeltaLat +
				sinHalfDeltaLon * sinHalfDeltaLon *
				cos (otherLatRadians) * cos (latRadians);

		return (EARTH_DIAMETER_KM * asin (sqrt (det)));
	} 

	/**
	 * Calculates the distance from this location to another and compares it with a given value (in km)
	 * @param otherLoc the location to which the distance is calculated
	 * @param distance the required threshold
	 * @return if the other location is within the given distance
	 */
	public boolean withinDistance (Location otherLoc, double distance) {
		if (otherLoc == null) {
			return (false);
		}
		
		return (haversineDistance (otherLoc) < distance);
	}
	
	public String toString () {
		return ("lat: " + mLat + ", lon: " + mLon);
	}
}
