package cscie97.smartcity.authentication;

/**
 * The base class for all resources of the smart city. Is extended by
 * the city object itself and the hierarchy of IoT devices.
 */
public abstract class Resource<T extends IterableItem<T>> extends IterableItem<T> {

	@Override
	public void accept (ItemVisitor visitor, int level) {
		visitor.visit (this, level);
	}

	public Resource (String id) {
		super (id);
	}

	public Resource (String id, String description) {
		super (id, description);
	}
	
	public abstract String getCombinedId ();

}