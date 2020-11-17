package cscie97.smartcity.test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import cscie97.smartcity.controller.Controller;
import cscie97.smartcity.controller.ControllerException;
import cscie97.smartcity.model.CommandProcessor;

/**
  * The testing harness for the model service command processor. 
  * With no arguments does a performance test
  * If arguments are present, interprets the first as the incoming file name, passes it
  * to the processor and prints the results onto the standard output. If a second argument
  * is specified, redirects its output into a file with the name taken from this argument
  * Usage: cscie97.smartcity.model.test.TestDriver [file name containing commands, one per line] [output file name]
 */
public class TestDriver {
	public static void main(String[] args) throws ControllerException {
		String fileName = args[0];
		PrintStream p = System.out;
		
		if (args.length > 1) {
			try {
				p = new PrintStream (args[1], Charset.forName ("UTF-8"));
			} catch (IOException iox) {
				System.err.println ("Could not write to the output file " + args[1] + " - defaulting to standard out.");
			}
		}

		Controller controller = new Controller ().withPrintStream (p);
		
		new CommandProcessor (controller.getModelService ()).withPrintStream (p).processCommandFile (fileName);
	}
	
}
