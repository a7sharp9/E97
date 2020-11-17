package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

/**
 * an object abstracting the functionality of an IoT information kiosk.
 * is capable of displaying an image specified by an Internet URI
 */
public class InformationKiosk extends PayableDevice<InformationKiosk> {
	
	@Override
	public String getType () {
		return (INFO_KIOSK);
	}


	public InformationKiosk (String city, String id) {
		super (city, id);
	}

	
	protected InformationKiosk getThis () {
		return (this);
	}

	
	public void validate () {
		
	}
	
	/**
	 * The URI of the current displayed image
	 */
	private String mImage;

	public final String getImage () {
		return mImage;
	}


	@Override
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("image URL: ").append (getImage ()).append (lineSeparator ());
		return (sb.toString ());
	}


	@Override
	public void update (Device <?> fromDevice) {
		super.update (fromDevice);
		
		if (fromDevice instanceof InformationKiosk) {
			InformationKiosk fromKiosk = (InformationKiosk) fromDevice;
			withImage (fromKiosk.getImage ());
		}
	}
	
	public InformationKiosk withImage (String image) {
		if (image != null) {
			mImage = image;
		}
		
		return (this);
	}


}
