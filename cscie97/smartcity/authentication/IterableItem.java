package cscie97.smartcity.authentication;

/**
 * The parent class for an object that holds an id and a description and can be traversed
 * by an instance of the ItemVisitor
 */
public abstract class IterableItem<T extends IterableItem<T>> {
	private final String mId;
	private String mDescription;

	public IterableItem (String id, String description) {
		mId = id;
		mDescription = description;
	}

	public IterableItem (String id) {
		this (id, null);
	}

	public final String getId () {
		return mId;
	}

	public final String getDescription () {
		return mDescription;
	}
	
	public final T withDescription (String description) {
		if (description != null) {
			mDescription = description;
		}
		
		return (getThis ());
	}
	
	@Override
	public int hashCode () {
		return mId.hashCode ();
	}
	
	@Override
	public boolean equals (Object obj) {
		if (obj instanceof IterableItem) {
			IterableItem<?> otherItem = (IterableItem<?>) obj;
			return (getId ().equals (otherItem.getId ()) &&
					(getDescription () == otherItem.getDescription () ||
						(getDescription () != null && getDescription ().equals (otherItem.getDescription ()))
						)
					);
		} else {
			return (false);
		}
	}

	public void accept (ItemVisitor visitor) {
		accept (visitor, 0);
	}
	
	/**
	 * Implement this method to provide a way to traverse the inner structure of the child object
	 */
	public abstract void accept (ItemVisitor visitor, int level);
	
	/**
	 * Polymorphic helper for builder-style methods
	 */
	protected abstract T getThis ();
	
	public abstract String getType ();
	
	/**
	 * Returns the multiline text representation of the object, with each line prepended by
	 * the supplied prefix 
	 */
	public abstract String show (String prefix);


}