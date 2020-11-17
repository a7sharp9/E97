package cscie97.smartcity.controller;

import cscie97.smartcity.model.ModelServiceException;
import cscie97.smartcity.model.Person;

/**
 * Applies changes to some or all of the person's attributes contained in the template
 */
public class UpdatePersonCommand extends PersonCommand {
	
	private final Person<?> mTemplate;

	public UpdatePersonCommand (Person<?> template) {
		super (template.getId ());
		mTemplate = template;
	}

	@Override
	public void execute (Controller controller) throws ControllerException {
		try {
			Person<?> person = controller.getModelService ().getPerson (getPerson ());
			
			person.update (mTemplate, controller.getModelService ().getAuthService ());
		} catch (ModelServiceException msx) {
			throw new ControllerException (msx.getMessage ());
		}
	}

	@Override
	public String toString () {
		return ("changing state of person " + getPerson ());
	}

}
