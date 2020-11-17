package cscie97.smartcity.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Contains a collection of instances of Command interface. The execution
 * consists of executing each of the commands
 */
public class CompoundCommand implements Command {

	private final Collection<Command> mCommands;
	
	public CompoundCommand (Collection<Command> commands) {
		mCommands = commands;
	}
	
	public CompoundCommand (Command... commands) {
		this ();
		for (Command command: commands) {
			mCommands.add (command);
		}
	}
	
	public CompoundCommand () {
		mCommands = new HashSet<> ();
	}

	public void addCommand (Command command) {
		mCommands.add (command);
	}
	
	@Override
	public void execute (Controller controller) throws ControllerException {
		String msg = "";
		for (Command command: mCommands) {
			try {
				command.execute (controller);
			} catch (ControllerException cx) {
				msg += cx.getMessage ();
				msg += '\n';
			}
		}
		
		if (!msg.isEmpty ()) {
			throw new ControllerException (msg);
		}
	}
	
	public String toString () {
		String subCommands = mCommands.stream ().map (c -> c.toString ()).filter (c -> c != null && !c.isEmpty ()).collect (Collectors.joining ("\n"));
		return ("[\n" + subCommands + "\n]");
	}

}
