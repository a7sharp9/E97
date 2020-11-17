package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

/**
 * an object abstracting the functionality of an IoT street light.
 * is capable of changing its light intensity
 */
public class StreetLight extends Device<StreetLight> {

	public StreetLight (String city, String id) {
		super (city, id);
	}

	public StreetLight getThis () {
		return (this);
	}

	
	public void validate () {
		
	}
	
	private Integer mBrightness;
	
	public final Integer getBrightness () {
		return mBrightness;
	}

	public StreetLight withBrightness (Integer brightness) {
		if (brightness != null) {
			mBrightness = brightness;
		}
		
		return (this);
	}
	
	@Override
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("brightness: ").append (getBrightness ()).append (lineSeparator ());
		return (sb.toString ());
	}

	@Override
	public void update (Device <?> fromDevice) {
		super.update (fromDevice);
		
		if (fromDevice instanceof StreetLight) {
			StreetLight fromLight = (StreetLight) fromDevice;
			withBrightness (fromLight.getBrightness ());
		}
	}
	
	@Override
	public String getType () {
		return (STREET_LIGHT);
	}


}
