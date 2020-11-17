package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

/**
 * an intermediate class that represents an IoT device with a blockchain account
 * is capable of accepting or dispensing payments
 */

public abstract class PayableDevice<T extends PayableDevice<T>> extends Device<T> {

	private String mAccount;
	
	public final String getAccount () {
		return mAccount;
	}

	public PayableDevice (String city, String id) {
		super (city, id);
	}

	public T withAccount (String account) {
		if (account != null) {
			mAccount = account;
		}
		
		return (getThis ());
	}
	
	@Override
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("account address: ").append (getAccount ()).append (lineSeparator ());
		return (sb.toString ());
	}
}
