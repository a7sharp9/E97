package cscie97.smartcity.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is the top-level repository of the information about the
 * distribution and flow of the currency, contained in a blockchain. The administrator
 * of the Ledger is capable of creating and funding accounts, while the users are
 * allowed to create and submit transactions which result in transfers of funds,
 * subject to the available balances and other conditions.
 */
public class Ledger implements LedgerConstants {
	
	/**
	 * The name of this ledger; immutable.
	 */
	private final String mName;
	
	/**
	 * The initial seed string for this ledger; it will be a part
	 * of hashes of all blocks that are committed to it.
	 */
	public final String mSeed;
	
	/**
	 * The description of this ledger; optional
	 */
	private String mDescription = "";
	
	/**
	 * The list of all committed blocks.
	 */
	private final List<Block> mBlockMap;
	
	/**
	 * The "working" block to add transactions and account to.
	 * Once the number of transactions reaches the threshold
	 * ( @see LedgerConstants.TRANSACTIONS_IN_BLOCK ),
	 * it is committed to the chain and a new block is cloned
	 * from it, becoming the working copy.
	 */
	private Block mCurrentBlock;
	
	public Ledger (String name, String seed) throws LedgerException {
		mName = name;
		mSeed = seed;
		mBlockMap = new ArrayList <> ();

		// This is the "genesis" block; it has no predecessor
		mCurrentBlock = new Block (null, seed);
		
		// Create all the currency in the world and put it into the master account
		createAccount (MASTER_ACCOUNT).withBalance (Integer.MAX_VALUE);
	}
		
	/**
	 * Builder-pattern setter of the optional description field
	 * @param description The ledger description
	 * @return The ledger
	 */
	public Ledger withDescription (String description) {
		mDescription = description;
		return (this);
	}
		
	/*---------------------------------------------------*/
	/* public API */
	/*---------------------------------------------------*/
	
	/**
	 * Creates an account and places it in the current (uncommitted) block
	 * The initial balance is 0.
	 * @param address The string address of the account
	 * @return The created account object
	 * @throws LedgerException if an account with such address already exists
	 */
	public Account createAccount (String address) throws LedgerException {
		Account acct = new Account (address);
		mCurrentBlock.addAccount (acct);
		return (acct);	
	}

	/**
	 * Inquires of the last committed balance of the account with the given address
	 * @param address The string address of the account
	 * @return The balance on the account
	 * @throws LedgerException if an account with such address does not exist,
	 * or had been created, but never committed, or there are no committed blocks
	 */
	public int getAccountBalance (String address) throws LedgerException {
		int lastBlockIdx = mBlockMap.size () - 1;
		if (lastBlockIdx >= 0) {
			return (mBlockMap.get (lastBlockIdx).getAccountBalance (address));
		} else {
			throw new LedgerException (OP_GET_BALANCE, "There are no committed blocks.");
		}
	}
	
	/**
	 * Inquires of the last committed balance of all accounts
	 * @return The map of pairs of account address and the corresponding balance
	 * @throws LedgerException if no accounts had been committed
	 */
	public Map<String, Integer> getAccountBalances () throws LedgerException {
		int lastBlockIdx = mBlockMap.size () - 1;
		if (lastBlockIdx >= 0) {
			return (mBlockMap.get (lastBlockIdx).getAccountBalances ());
		} else {
			throw new LedgerException (OP_GET_ALL_BALANCES, "There are no committed blocks.");
		}
	}

	/**
	 * Verifies, performs and records a transaction. If verification is
	 * successful, the funds are moved according to the instructions
	 * within the transaction.
	 * The transaction is added to the uncommitted working block.
	 * If this transaction brings their number in the block to the threshold
	 * ( @see LedgerConstants.TRANSACTIONS_IN_BLOCK ),
	 * the block is committed to the chain and a new block is cloned
	 * from it, becoming the working copy.
	 * @param The transaction to be executed
	 * @throws LedgerException if the transaction fails verification
	 */
	public void processTransaction (Transaction t) throws LedgerException {
		boolean fitInCurrent = mCurrentBlock.processTransaction (t);
		
		// see if it was the last transaction that could fit into this block
		// (this is indicated by the flag returned from the call in the previous line)
		if (!fitInCurrent) {
			// attach the current map to the chain
			mBlockMap.add (mCurrentBlock);
			
			// Compute and store the hash for this block
			mCurrentBlock.updateHash ();
			
			// clone the accounts and set as the current block
			mCurrentBlock = new Block (mCurrentBlock, mSeed);
		}
	}
	
	/**
	 * Checks all committed blocks for a transaction with this identifier
	 * @param Transaction id
	 * @return The found transaction
	 * @throws LedgerException if no such transaction exists
	 */
	public Transaction getTransaction (String id) throws LedgerException {
		Transaction ret = null;
		for (int iB = 0; iB < mBlockMap.size () && ret == null; iB ++) {
			ret = mBlockMap.get (iB).getTransaction (id); 
		}
		
		if (ret == null) {
			throw new LedgerException (OP_GET_TRANSACTION, "Transaction " + id + " does not exist in any of the committed blocks.");
		}
		
		return (ret);
	}
	
	/**
	 * Looks for the block with this number among the committed blocks
	 * @param Block number (1-based)
	 * @return The found committed block
	 * @throws LedgerException if the block with this number has not been committed
	 */
	public Block getBlock (int id) throws LedgerException {
		if (id <= 0 || id > mBlockMap.size ()) {
			throw new LedgerException (OP_GET_BLOCK, "Block " + id + " has not been committed.");			
		}
		
		Block ret = mBlockMap.get (id - 1);
		
		return (ret);
	}
	
	/**
	 * Traverse committed blocks and recompute hashes
	 * Throws an exception if any computation result is different
	 * from the hash stored in the block
	 * @throws LedgerException if a verification error is encountered
	 */
	public void validate () throws LedgerException {
		for (Block block: mBlockMap) {
			block.validate ();
		}
	}
	
	/**
	 * The string representation of the ledger; contains its name, seed and description
	 */
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder ();
		sb.append ("Ledger ").append (mName);
		if (mSeed != null && !mSeed.isEmpty ()) {
			sb.append (", seed ").append (mSeed);
		}
		if (mDescription != null && !mDescription.isEmpty ()) {
			sb.append (", description ").append (mDescription);
		}
		
		return (sb.toString ());
	}
}
