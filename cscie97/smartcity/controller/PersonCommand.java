package cscie97.smartcity.controller;

/**
 * A parent class of all commands directed at a person object
 */
public abstract class PersonCommand implements Command {

	private final String mPerson;
	public PersonCommand (String person) {
		mPerson = person;
	}

	public String getPerson () {
		return (mPerson);
	}
}
