package cscie97.smartcity.authentication;

/**
 * Represents the user that is built into the auth service (a system admin)
 */
public class InternalUser extends User<InternalUser> {

	@Override
	protected InternalUser getThis () {
		return (this);
	}

	public InternalUser (String id, String name) {
		super (id, name);
	}

	public InternalUser (String id) {
		super (id);
	}

	@Override
	public String getType () {
		return ("internal_user");
	}

	@Override
	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (prefix);
		sb.append("Internal user: ").append (getId ());
		
		if (getName () != null) {
			sb.append (", name: \"")
				.append (getName ())
				.append ("\"");
		}
		
		return (sb.toString ());
	}
}
