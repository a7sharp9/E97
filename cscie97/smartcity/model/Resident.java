package cscie97.smartcity.model;

import static java.lang.System.lineSeparator;

import cscie97.smartcity.authentication.AuthenticationException;
import cscie97.smartcity.authentication.AuthenticationService;
import cscie97.smartcity.ledger.Account;

/**
 * an object abstracting a city resident
 */
public class Resident extends Person<Resident> {

	public Resident (String id) {
		super (id);
	}
	
	public Resident (String id, String name) {
		super (id, name);
	}
	
	@Override
	public Resident getThis () {
		return (this);
	}
	
	@Override
	public String getType () {
		return (RESIDENT);
	}

	/**
	 * The phone number, if specified
	 */
	private String mPhone;

	/**
	 * The address of the blockchain account, if present
	 */
	private Account mAccount;
	
	/**
	 * The role string
	 */
	private String mRole;
	
	public final String getPhone () {
		return mPhone;
	}

	public final Account getAccount () {
		return mAccount;
	}

	public final String getRole () {
		return mRole;
	}

	public Resident withPhone (String phone) {
		if (phone != null) {
			mPhone = phone;
		}
		
		return (getThis ());
	}
	
	public Resident withAccount (String account) {
		if (account != null) {
			mAccount = new Account (account);
		}
		
		return (getThis ());
	}

	public Resident withAccount (Account account) {
		if (account != null) {
			mAccount = account;
		}
		
		return (getThis ());
	}

	public Resident withRole (AuthenticationService authService, String role) throws ModelServiceException {
		if (role != null) {
			mRole = role;
			try {
				authService.addRoleToUser (this, role);
			} catch (AuthenticationException ax) {
				throw new ModelServiceException (OP_UPDATE, PERSON, ax.getMessage ());
			}
		}
		
		return (getThis ());
	}
	
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (super.show (prefix));
		
		if (prefix == null)
			prefix = "";
		
		sb.append (prefix).append ("name: ").append (getName ()).append (lineSeparator ());
		sb.append (prefix).append ("phone: ").append (getPhone ()).append (lineSeparator ());
		sb.append (prefix).append ("role: ").append (getRole ()).append (lineSeparator ());
		sb.append (prefix).append ("account: ").append (getAccount ().getAddress ()).append (lineSeparator ());
		
		return (sb.toString ());
	}
	
	@Override
	public void update (Person <?> fromPerson, AuthenticationService authService) throws ModelServiceException {
		super.update (fromPerson, authService);
		
		if (fromPerson instanceof Resident) {
			Resident fromResident = (Resident) fromPerson;
			
			withAccount (fromResident.getAccount ())
			.withRole (authService, fromResident.getRole ())
			.withName (fromResident.getName ())
			.withPhone (fromResident.getPhone ());
		}
	}


	
}
