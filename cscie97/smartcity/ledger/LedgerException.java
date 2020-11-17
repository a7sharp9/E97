package cscie97.smartcity.ledger;

/**
 * An exception that may arise in various ledger operations. Contains 
 * the "reason" string that is stored as the message field in the
 * parent exception class.
 */
public class LedgerException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final String mOperation;

	public LedgerException (String operation, String cause) {
		super (cause);
		mOperation = operation;
	}
	
	/**
	 * The string representation of this exception; contains only the
	 * reason string that was used in its construction
	 */
	@Override
	public String toString () {
		return (mOperation + " failed: " + getMessage ());
	}

}
