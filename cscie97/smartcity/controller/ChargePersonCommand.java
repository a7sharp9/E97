package cscie97.smartcity.controller;

import cscie97.smartcity.model.City;
import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.ModelService;
import cscie97.smartcity.model.PayableDevice;
import cscie97.smartcity.model.Person;
import cscie97.smartcity.model.Resident;

/**
 * The command to charge the account of a person, if they have an account. The receiver is the 
 * device which generated the event, or the city to which the device is registered if the device is
 * not capable of accepting payments
 */
public class ChargePersonCommand extends ChargeCommand {

	private final String mPerson;
	private final Device<?> mDevice;
	
	public ChargePersonCommand (String person, Device <?> device, int amount) {
		super (amount);
		mPerson = person;
		mDevice = device;
	}
	
	@Override
	public String getFrom (ModelService service) {
		Person<?> person = service.getPerson (mPerson);		
		return (person instanceof Resident ? ((Resident) person).getAccount ().getAddress () : null);
	}
	
	@Override
	public String getTo (ModelService service) {
		String ret = null;
		if (mDevice instanceof PayableDevice) {
			ret = ((PayableDevice <?>) mDevice).getAccount ();
		}
		
		if (ret == null) {
			City city = service.getCity (mDevice.getCity ());		
			ret = (city != null) ? city.getAccount ().getAddress () : null;
		}
		
		return (ret);
	}
	
	@Override
	public String toString () {
		return ("Charge person " + mPerson + " " + getAmount () + " units unless a visitor");
	}



}
