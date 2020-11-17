package cscie97.smartcity.model.test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import cscie97.smartcity.model.CommandProcessor;
import cscie97.smartcity.model.ModelService;
import cscie97.smartcity.model.ModelServiceException;

/**
  * The testing harness for the model service command processor. 
  * With no arguments does a performance test
  * If arguments are present, interprets the first as the incoming file name, passes it
  * to the processor and prints the results onto the standard output. If a second argument
  * is specified, redirects its output into a file with the name taken from this argument
  * Usage: cscie97.smartcity.model.test.TestDriver [file name containing commands, one per line] [output file name]
 */
public class TestDriver {
	public static void main(String[] args) throws ModelServiceException {
		if (args.length == 0) {
			performance ();
			return;
		}
		
		String fileName = args[0];
		PrintStream p = System.out;
		
		if (args.length > 1) {
			try {
				p = new PrintStream (args[1], Charset.forName ("UTF-8"));
			} catch (IOException iox) {
				System.err.println ("Could not write to the output file " + args[1] + " - defaulting to standard out.");
			}
		}

		new CommandProcessor (new ModelService ()).withPrintStream (p).processCommandFile (fileName);
	}
	
	private static void performance () throws ModelServiceException {
		CommandProcessor cp = new CommandProcessor (new ModelService ());
		cp.processCommand ("define", "city", "c1", "lat", "0", "long", "0", "radius", "1");
		
		long startTime = System.currentTimeMillis ();
		
		for (int iD = 0; iD < 1000000; iD ++) {
			cp.processCommand ("define", "vehicle", "c1:" + String.valueOf (iD), "type", "car", "lat", "0", "long", "0");
		}
		System.out.println ("defined cars ");
		long endTime = System.currentTimeMillis ();
		System.out.println (endTime - startTime);
		startTime = endTime;
		
		for (int iD = 0; iD < 1000000; iD ++) {
			cp.processCommand ("define", "visitor", String.valueOf (iD), "lat", "0", "long", "0");
		}
		System.out.println ("defined visitors ");
		endTime = System.currentTimeMillis ();
		System.out.println (endTime - startTime);
		startTime = endTime;
		
		cp.processCommand ("show", "city", "c1");
		endTime = System.currentTimeMillis ();
		System.out.println ("collected city info ");
		System.out.println (endTime - startTime);
	}
}
