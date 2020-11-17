package cscie97.smartcity.controller;

/**
 * An exception thrown by the package objects. Contains a message explaining the cause.
 */
public class ControllerException extends Exception {
	private static final long serialVersionUID = 1L;

	ControllerException (String reason) {
		super (reason);
	}

}
