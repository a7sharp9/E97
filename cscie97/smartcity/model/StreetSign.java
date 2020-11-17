package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

/**
 * an object abstracting the functionality of an IoT street sign.
 * is capable of changing its text
 */
public class StreetSign extends Device<StreetSign> {

	public StreetSign (String city, String id) {
		super (city, id);
	}

	protected StreetSign getThis () {
		return (this);
	}

	
	public void validate () {
		
	}
	
	private String mText;
	
	public final String getText () {
		return mText;
	}

	public StreetSign withText (String text) {
		if (text != null) {
			mText = text;
		}
		
		return (this);
	}

	@Override
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("text: ").append (getText ()).append (lineSeparator ());
		return (sb.toString ());
	}

	@Override
	public void update (Device <?> fromDevice) {
		super.update (fromDevice);
		
		if (fromDevice instanceof StreetSign) {
			StreetSign fromSign = (StreetSign) fromDevice;
			withText (fromSign.getText ());
		}
	}
	
	@Override
	public String getType () {
		return (STREET_SIGN);
	}


}
