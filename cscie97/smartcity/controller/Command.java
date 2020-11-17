package cscie97.smartcity.controller;

/**
 * The functional interface representing a controller command
 * passes an instance of Controller to its method for execution
 */
public interface Command {
	public void execute (Controller controller) throws ControllerException;
}
