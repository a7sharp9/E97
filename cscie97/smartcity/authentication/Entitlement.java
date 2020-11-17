package cscie97.smartcity.authentication;

/**
 * Base class for any entitlement stored in the auth system
 */
public abstract class Entitlement<T extends Entitlement<T>> extends IterableItem<T> {

	public Entitlement (String id) {
		super (id);
	}

	public Entitlement (String id, String description) {
		super (id, description);
	}

	public String show (String prefix) {
		StringBuilder sb = new StringBuilder (prefix);
		sb.append (getType ())
			.append (": ")
			.append (getId ());
		
		if (getDescription () != null) {
			sb.append (", description: \"")
				.append (getDescription ())
				.append ("\"");
		}

		return (sb.toString ());
	}

}
