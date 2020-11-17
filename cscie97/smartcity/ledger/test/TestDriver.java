/**
 * 
 */
package cscie97.smartcity.ledger.test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import cscie97.smartcity.ledger.CommandProcessor;

/**
  * The testing harness for the ledger command processor. Passes the incoming file name
  * to the processor and prints the results onto the standard output. If a second argument
  * is specified, redirects its output into a file with the name taken from this argument
  * Usage: java com.cscie97.ledger.test.TestDriver <file name containing commands, one per line> [output file name]
 */
public class TestDriver {
	public static void main(String[] args) {
		String fileName = args[0];
		PrintStream p = System.out;
		
		if (args.length > 1) {
			try {
				p = new PrintStream (args[1], Charset.forName ("UTF-8"));
			} catch (IOException iox) {
				System.err.println ("Could not write to the output file " + args[1] + " - defaulting to standard out.");
			}
		}

		new CommandProcessor ().withPrintStream (p).processCommandFile (fileName);
	}
}
