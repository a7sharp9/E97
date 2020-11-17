package cscie97.smartcity.ledger;

/**
 * Defines constants common to the classes in the package.
 * To make use of them, implement this interface.
 */
public interface LedgerConstants {
	
	// The threshold for the number of transactions in a block; when reached,
	// the block is committed to the chain and a new block is cloned from it
	public final int	TRANSACTIONS_IN_BLOCK = 1;
	
	// The minimum fee (in whole units) for a transaction payable to the
	// master account; if it is specified as less than this number, the
	// transaction is rejected as invalid
	public final int	MINIMUM_FEE = 10;
	
	
	public final int INITIAL_PERSON_BALANCE = 200;
	public final int INITIAL_DEVICE_BALANCE = 100;
	public final int INITIAL_CITY_BALANCE = 1000;
	
	// The name of the master account
	public final String	MASTER_ACCOUNT = "master";
	
	// The valid command keywords
	public final String OP_CREATE_LEDGER = "create-ledger";
	public final String OP_CREATE_ACCOUNT = "create-account";
	public final String OP_GET_ALL_BALANCES = "get-account-balances";
	public final String OP_GET_BALANCE = "get-account-balance";
	public final String OP_TRANSACTION = "process-transaction";
	public final String OP_GET_BLOCK = "get-block";
	public final String OP_GET_TRANSACTION = "get-transaction";
	public final String OP_VALIDATE = "validate";
	
	// The valid option words for a transaction command
	public final String TRANSACTION_ID = "id";
	public final String TRANSACTION_PAYER = "payer";
	public final String TRANSACTION_RECEIVER = "receiver";
	public final String TRANSACTION_AMOUNT = "amount";
	public final String TRANSACTION_FEE = "fee";
	public final String TRANSACTION_NOTE = "note";
	
	// The valid option words for a ledger creation command
	public final String LEDGER_SEED = "seed";
	public final String LEDGER_DESCRIPTION = "description";
}
