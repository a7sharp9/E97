package cscie97.smartcity.controller;

import cscie97.smartcity.ledger.LedgerConstants;
import cscie97.smartcity.model.City;
import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.ModelService;
import cscie97.smartcity.model.PayableDevice;

/**
 * The command to charge the account of a device, if it has an account. The receiver is the city
 * to which the device is registered
 */
public class ChargeDeviceCommand extends ChargeCommand implements LedgerConstants {
	private final String mDevice;
	
	public ChargeDeviceCommand (String device, int amount) {
		super (amount);
		mDevice = device;
	}

	@Override
	public String getFrom (ModelService service) {
		Device<?> device = service.getDevice (mDevice);
		return (device instanceof PayableDevice ? ((PayableDevice<?>) device).getAccount () : null);
	}

	@Override
	public String getTo (ModelService service) {
		Device<?> device = service.getDevice (mDevice);
		City city = service.getCity (device.getCity ());
		return (city != null ? city.getAccount ().getAddress () : null);
	}
	
	@Override
	public String toString () {
		return ("device " + mDevice + " charged " + getAmount ());
	}

	
	
}
