package cscie97.smartcity.ledger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Accepts and processes commands, either individually or collected in a file,
 * to perform ledger operations.
 */
public class CommandProcessor implements LedgerConstants {

	/**
	 * The instance of the Ledger class on whic all operations are performed. Needs to be
	 * created with the first command
	 */
	private Ledger mLedger = null;

	/**
	 * The output stream where all messages arising from the operations being accepted or
	 * rejected are directed. Default to the system output.
	 */
	private PrintStream mPrintStream = System.out;
	
	/**
	 * Builder-pattern method for changing the output stream
	 * @param stream The new output stream
	 * @return this processor
	 */
	public CommandProcessor withPrintStream (PrintStream stream) {
		mPrintStream = stream;
		return (this);
	}

	/**
	 * The common functional interface representing a single ledger command.
	 */
	private interface Command {
		/**
		 * @param args a sequence of string arguments to be passed to the
		 * implementation
		 * @return the string representing the result of the command
		 * @throws LedgerException if the command is not valid
		 */
		public String doIt (String... args) throws LedgerException;
	}

	/**
	 * The instance of the command interface that invokes the creation of the
	 * ledger and caches it. Needs to be execited before all other commands.
	 */
	private final Command mCreateLedgerCommand = (args) -> {
		mLedger = parseLedger (args);
		return ("created " + mLedger);
	};

	/**
	 * The instance of the command interface that invokes the creation of a new account
	 */
	private final Command mCreateAccountCommand = (args) -> {
		if (mLedger == null) {
			throw new LedgerException (OP_CREATE_ACCOUNT, "no ledger");
		}
		
		if (args.length <= 0) {
			throw new LedgerException (OP_CREATE_ACCOUNT, "no address supplied for account creation");
		}
		
		Account created = mLedger.createAccount (args[0]);
		return ("created account " + created);
	};

	/**
	 * The instance of the command interface that inquires of the balance of 
	 * an account and formats it for printing
	 */
	private final Command mGetAccountBalanceCommand = (args) -> {
		if (mLedger == null) {
			throw new LedgerException (OP_GET_BALANCE, "no ledger");
		}
		
		if (args.length <= 0) {
			throw new LedgerException (OP_GET_BALANCE, "no address supplied for account inquiry");
		}
		
		String address = args[0];
		return ("balance for " + address + ": " + String.valueOf (mLedger.getAccountBalance (address)));
	};

	/**
	 * The instance of the command interface that collects balances of 
	 * all known accounts and formats them for printing
	 */
	private final Command mGetAccountBalancesCommand = (args) -> {
		if (mLedger == null) {
			throw new LedgerException (OP_GET_ALL_BALANCES, "no ledger");
		}
		
		Map<String, Integer> balances = mLedger.getAccountBalances ();

		StringBuilder sb = new StringBuilder ();		
		sb.append ("account balances:");
		
		for (Map.Entry<String, Integer> entry: balances.entrySet ()) {
			sb.append (System.lineSeparator ()).append ("  ")
			.append (entry.getKey ()).append (": ").append (entry.getValue ());
		}
		
		return (sb.toString ());
	};

	/**
	 * The instance of the command interface that parses incoming
	 * parameters for creating a new transaction and invokes the method
	 * to process it in the ledger
	 */
	private final Command mProcessTransactionCommand = (args) -> {
		if (mLedger == null) {
			throw new LedgerException (OP_GET_TRANSACTION, "no ledger");
		}
		
		Transaction transaction = parseTransaction (args);
		mLedger.processTransaction (transaction);
		return ("processed " + transaction.toString ());
	};
	
	/**
	 * The instance of the command interface that tries to find a transaction 
	 * with a given identifier in the linked blocks and, if found,
	 * formats the result for printing
	 */
	private Command mGetTransactionCommand  = (args) -> {
		if (mLedger == null) {
			throw new LedgerException (OP_GET_TRANSACTION, "no ledger");
		}
		
		return (mLedger.getTransaction (args[0]).toString ());
	};

	/**
	 * The instance of the command interface that tries to find a block 
	 * with a given number in the linked blocks and, if found,
	 * formats the result for printing
	 */
	private Command mGetBlockCommand  = (args) -> {
		if (mLedger == null) {
			throw new LedgerException (OP_GET_BLOCK, "no ledger");
		}
		
		if (args.length <= 0) {
			throw new LedgerException (OP_GET_BLOCK, "no number supplied for block inquiry");
		}
		
		return (mLedger.getBlock (Integer.valueOf (args[0])).toString ());
	};
	
	/**
	 * The instance of the command interface that traverses all
	 * blocks in the chain and checks that their hashes have been
	 * cached correctly. 
	 */
	private Command mValidateCommand = (args) -> {
		if (mLedger == null) {
			throw new LedgerException (OP_VALIDATE, "no ledger");
		}
		
		mLedger.validate ();
		return ("committed blocks validated successfully.");
	};

	/**
	 * The map of all valid commands, keyed by their string identifiers
	 */
	private final Map<String, Command> mCommands = Map.of (
			OP_CREATE_LEDGER, mCreateLedgerCommand,
			OP_CREATE_ACCOUNT, mCreateAccountCommand,
			OP_GET_BALANCE, mGetAccountBalanceCommand,
			OP_GET_ALL_BALANCES, mGetAccountBalancesCommand,
			OP_TRANSACTION, mProcessTransactionCommand,
			OP_GET_TRANSACTION, mGetTransactionCommand,
			OP_GET_BLOCK, mGetBlockCommand,
			OP_VALIDATE, mValidateCommand
			);

	/**
	 * Performs simple parsing of command lines;
	 * is capable of recognizing a token as an option word and collect all tokens
	 * that follow it into a list until next option word is encountered.
	 * @param args the list of command tokens
	 * @param the index of the argument in the list to start processing from
	 * @param the collection of tokens to be treated as option word
	 * @return a map of lists of non-option tokens keyed by the option word which 
	 * they followed in the argument list 
	 */
	private static Map<String, ArrayList<String>> parseArgs (String [] args, int fromArg, Set<String> options) {
		Map<String, ArrayList<String>>	parsed = new HashMap<> ();
		
		ArrayList<String> curOpts = null;
		for (int iArg = fromArg; iArg < args.length; iArg ++) {
			String arg = args[iArg];
			if (options.contains (arg)) {
				curOpts = new ArrayList<> ();
				parsed.put (arg, curOpts);
			} else if (curOpts != null) {
				curOpts.add (arg);
			}
		}
		
		return (parsed);
	}

	/**
	 * A helper method that parses a ledger command
	 */
	private static Ledger parseLedger (String... args) throws LedgerException {
		if (args.length <= 0) {
			throw new LedgerException (OP_CREATE_LEDGER, "no name suppied for ledger");
		}
		
		String name = args[0];
		
		// The list of recognized option words
		Set<String> options = Set.of (
				LEDGER_SEED,
				LEDGER_DESCRIPTION
		);
		
		// parse the parameters of the incoming command
		Map<String, ArrayList<String>>	parsed = parseArgs (args, 1, options);
		
		// retrieve the seed string
		String seed = "";
		ArrayList<String> seedArr = parsed.get (LEDGER_SEED);
		if (seedArr != null && seedArr.size () == 1) {
			seed = seedArr.get (0);
		}
		
		// create new ledger object from name and seed
		Ledger	ret = new Ledger (name, seed);
		
		// set description, if present in the command line
		ArrayList<String> descrArr = parsed.get (LEDGER_DESCRIPTION);
		if (descrArr != null && descrArr.size () > 0) {
			ret.withDescription (String.join (" ", descrArr));
		}
		
		return (ret);
	}
	
	/**
	 * A helper method that parses a transaction command
	 */
	private static Transaction parseTransaction (String... args)
			throws LedgerException
	{
		if (args.length <= 0) {
			throw new LedgerException (OP_TRANSACTION, "no identifier suppied for transaction");
		}
		
		String id = args[0];
		
		// the list of recognized option words
		Set<String> options = Set.of (
				TRANSACTION_ID,
				TRANSACTION_PAYER,
				TRANSACTION_RECEIVER,
				TRANSACTION_AMOUNT,
				TRANSACTION_FEE,
				TRANSACTION_NOTE
		);
		
		// parse the parameters of the incoming command
		Map<String, ArrayList<String>>	parsed = parseArgs (args, 1, options);
		
		Transaction ret = null;
		
		// create the transaction from the mandatory keywords
		try {
			ret = new Transaction (id, 
					parsed.get (TRANSACTION_PAYER).get (0), 
					parsed.get (TRANSACTION_RECEIVER).get (0),
					Integer.parseUnsignedInt (parsed.get (TRANSACTION_AMOUNT).get (0))
					);
		} catch (NullPointerException npx) { // one of the required options was not present
			throw new LedgerException (OP_TRANSACTION, "one of the required transaction fields is missing.");
		} catch (NumberFormatException nfx) { // could not parse the amount
			throw new LedgerException (OP_TRANSACTION, "the amount is not valid.");
		}
		
		// set the fee, if present in the command line
		ArrayList<String> feeArr = parsed.get (TRANSACTION_FEE);
		if (feeArr != null && feeArr.size () == 1) {
			try {
				ret.withFee (Integer.parseUnsignedInt (feeArr.get (0)));
			} catch (NumberFormatException nfx) { // could not parse the fee
				throw new LedgerException (OP_TRANSACTION, "the fee is not valid.");
			}
		}
				
		// set the note, if present in the command line
		ArrayList<String> notesArr = parsed.getOrDefault (TRANSACTION_NOTE, null);
		if (notesArr != null && notesArr.size () > 0) { 
			ret.withPayload (String.join (" ", notesArr));
		}
				
		return (ret);
	}
	
	/*-------------------------------------------------------------*/
	/* PUBLIC API */
	/*-------------------------------------------------------------*/
	
	/**
	 * Processes a single command, represented by a list of command tokens
	 * @param cmd command tokens
	 * @return the formatted string representing the result of the command execution
	 * @throws LedgerException if the command is not recognized or could not be processed
	 */
	public final String processCommand (String... cmd) throws LedgerException {
		String cmdString = cmd[0];
		
		// find the instance of the command interface corresponding to this command keyword
		Command command = mCommands.get (cmdString.toLowerCase ());

		if (command == null) { // no such command
			throw new LedgerException (cmdString, "command not recognized");
		}

		// collect the arguments, less the command keyword itself
		String[] args = Arrays.copyOfRange (cmd, 1, cmd.length);
		
		// perform the command processing
		return (command.doIt (args));
	}

	/**
	 * Processes commands collected in a text file one by one. Empty lines and lines beginning
	 * with the '#' symbol are ignored. If processing one of the commands causes an
	 * exception, it is logged to the print stream and the processing continues
	 * @param fileName the name of the file with the 
	 */
	public final void processCommandFile (String fileName) {
		int lineNumber = 0;
		try (BufferedReader rd = new BufferedReader (new FileReader (new File (fileName), Charset.forName ("UTF-8")))) {
			for (String line = rd.readLine (); line != null; line = rd.readLine ()) {
				lineNumber ++;
				line.trim ();
				
				if (line.isEmpty () || line.charAt (0) == '#') { // comment or blank line
					mPrintStream.println (line);
					continue;
				}
				
				// collect command tokens
				StringTokenizer st = new StringTokenizer (line);
				ArrayList<String> argsArr = new ArrayList<> ();
				while (st.hasMoreTokens ()) {
					argsArr.add (st.nextToken ());
				}

				// call the single command processor
				try {
					String result = processCommand (argsArr.toArray (String[]::new));
					mPrintStream.println ("At line " + lineNumber + " " + result);
				} catch (LedgerException lx) {
					mPrintStream.println ("At line " + lineNumber + " " + lx);
				}
			}
		} catch (IOException iox) {
			mPrintStream.println ("Could not read commands from file " + fileName);
		}
	}

}
