package cscie97.smartcity.model;

/**
 * abstracts a visitor; no additional attributes compared to a generic person
 */
public class Visitor extends Person<Visitor> {

	public Visitor (String id) {
		super (id);
	}
	
	@Override
	public String getType () {
		return (VISITOR);
	}

	public Visitor getThis () {
		return (this);
	}
}
