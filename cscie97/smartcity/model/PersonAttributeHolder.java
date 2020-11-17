package cscie97.smartcity.model;

public class PersonAttributeHolder extends Person<PersonAttributeHolder> {

	public PersonAttributeHolder (String id) {
		super (id);
	}
	
	@Override
	public PersonAttributeHolder getThis () {
		return (this);
	}

	@Override
	public String getType () {
		return null;
	}

}
