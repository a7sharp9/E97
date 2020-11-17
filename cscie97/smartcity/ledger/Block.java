package cscie97.smartcity.ledger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a block of transactions, which becomes a link in the blockchain as
 * soon as the number of transactions reaches a predefined number (@see 
 */
public class Block implements LedgerConstants {

	/**
	 * The 1-based identifier of this block, assigned consecutively
	 */
	private final int mNumber;
	
	/**
	 * Reference to the previous block in the chain
	 */
	private final Block mPrevBlock;
	
	/**
	 * Local copy of the list of all accounts, keyed by account address
	 * Deep-cloned, so changes to objects do not propagate to other block
	 */
	private final Map<String, Account>	mAccountMap;
	
	/**
	 * List of all transaction accepted for this block. There will be
	 * at most {@link LedgerConstants.TRANSACTIONS_IN_BLOCK} of them;
	 * once that number is achieved, the block is linked to the chain and
	 * does not change
	 */
	private final Map<String, Transaction> mTransactions;
	
	/**
	 * Copy of the initial ledger seed
	 */
	private final String mSeed;
	
	/**
	 * Cached copy of the hash of the previous block (if present)
	 */
	private final String mPrevHash;
	
	/**
	 * base64 string representing a Merkle-tree hash of this block. 
	 * Includes hashes for the initial seed, the block number,
	 * the accounts and the transactions.
	 * Computed wher the block is committed to the chain
	 */
	private String mHash;
	
	public Block (Block prevBlock, String seed) {
		mAccountMap = new HashMap <> ();
		mTransactions = new HashMap<> ();
		mSeed = (seed != null) ? seed : "";
		
		if (prevBlock != null) {
			mNumber = prevBlock.mNumber + 1;
			mPrevBlock = prevBlock;
			mPrevHash = prevBlock.getHash ();
			Iterator<Account> accIter = mPrevBlock.mAccountMap.values ().iterator ();
			while (accIter.hasNext ()) {
				Account acc = accIter.next ();
				mAccountMap.put (acc.getAddress (), acc.clone ());
			}
		} else {
			mNumber = 1;
			mPrevBlock = null;
			mPrevHash = "";
		}
	}
	
	/**
	 * Computes a Merkle-tree string hash in base64 of the contents of this block.
	 * Includes the initial seed, identifier, the hash of all transactions and
	 * the hash of all accounts.
	 */
	private String computeHash () {
		return (Hash.hash (
					mSeed,
					Hash.hash (mNumber, getPreviousHash ()),
					Hash.hash (mTransactions.values ()), 
					Hash.hash (mAccountMap.values ())
					)
				);
	}
	
	public void updateHash () {
		mHash = computeHash ();
	}
	
	/**
	 * Accessor method for the base6 hash of this block.
	 * @return The hash of this block.
	 */
	public String getHash () {
		return (mHash);
	}
	
	/**
	 * Accessor method for the base6 hash of the previous block in the chain.
	 * Returns empty string is this is the genesis block. 
	 * @return The hash of the previous block.
	 */
	public String getPreviousHash () {
		return (mPrevHash);
	}
	
	/**
	 * This method adds a new account to the map of accounts stored in this block.
	 * @param acct The account to be added
	 * @throws LedgerException if an account with this address already exists
	 */
	public void addAccount (Account acct) throws LedgerException {
		String address = acct.getAddress ();
		if (mAccountMap.containsKey (address)) {
			throw new LedgerException (OP_CREATE_ACCOUNT, "Account " + address + " already exists");
		}
			
		mAccountMap.put (address, acct);		
	}
	
	private final void validateTransaction (Transaction transaction) throws LedgerException {
		// verify that no transaction with this id exists in any of the blocks
		for (Block b = this; b != null; b = b.mPrevBlock) {
			if (b.getTransaction (transaction.getId ()) != null) {
				throw new LedgerException (OP_TRANSACTION, 
						"Invalid transaction " + transaction.getId () + ": duplicate identifier");
			}
		}
		
		// verify that payer account exists
		String	from = transaction.getPayer ();
		if (!mAccountMap.containsKey (from)) {
			throw new LedgerException (OP_TRANSACTION,
					"Invalid transaction " + transaction.getId () + ": payer account " +
							from + " does not exist.");
		}
		
		// verify that receiving account exists
		String	to = transaction.getReceiver ();
		if (!mAccountMap.containsKey (to)) {
			throw new LedgerException (OP_TRANSACTION,
					"Invalid transaction " + transaction.getId () + ": receiver account " +
							to + " does not exist.");
		}
		
		int amount = transaction.getAmount ();
		int fee = transaction.getFee ();
		
		// verify that the fee is at least the defined minimum fee
		if (fee < MINIMUM_FEE) {
			throw new LedgerException (OP_TRANSACTION,
					"Invalid transaction " + transaction.getId () + 
					": the fee amount is less than minimum allowed");
		}
		
		// verify that the payer account has at least amount+fee in funds
		if (mAccountMap.get (from).getBalance () < amount + 
				(from.equalsIgnoreCase (MASTER_ACCOUNT) ? -fee : fee)) {
			throw new LedgerException (OP_TRANSACTION,
					"Invalid transaction " + transaction.getId () + ": payer account " +
					from + " does not have enough funds.");
		}
		
		// Verify that the receiver account will not end up with more money than exists
		if (mAccountMap.get (to).getBalance () + amount +
				(to.equalsIgnoreCase (MASTER_ACCOUNT) ? fee : -fee) < 0) { // overflow 
			throw new LedgerException (OP_TRANSACTION,
					"Invalid transaction " + transaction.getId () + ": receiver account " +
					to + " cannot have more funds than are available in the world.");
		}
	
	}
	
	private void transferFunds (Transaction transaction) throws LedgerException {
		// verify that the transfer request is valid
		validateTransaction (transaction);
		
		int amount = transaction.getAmount ();
		int fee = transaction.getFee ();
		
		// The payer gets the amount of the transaction and the fee deducted from balance
		mAccountMap.get (transaction.getPayer ()).addToBalance (-amount - fee);
		
		// The payee receives the transaction amount
		mAccountMap.get (transaction.getReceiver ()).addToBalance (amount);
		
		// The fee goes back to master account
		// Note: if it's a funding request, that is, the payer is master,
		// then no fee is due - and the next line will restore the fee amount deducted
		// during the first balance adjustment in this method
		mAccountMap.get (MASTER_ACCOUNT).addToBalance (fee);
	}
	
	/**
	 * Accepts a previously created transaction for processing and inclusion in the block.
	 * If the transaction is valid, the funds are transferred and the transaction is recorded
	 * @param transaction the transaction to be processed
	 * @return a boolean flag indicating if the block still did not reach the threshold number 
	 * of transactions for it to be linked to the chain
	 * @throws LedgerException if the transaction is invalid
	 */
	public boolean processTransaction (Transaction transaction) throws LedgerException {
		transferFunds (transaction);
		
		mTransactions.put (transaction.getId (), transaction);
		return (mTransactions.size () < TRANSACTIONS_IN_BLOCK);
	}
	
	/**
	 * Inquires of the balance of the account with the given address
	 * as recorded in this block
	 * @param address The account address
	 * @return the account balance (in whole units)
	 * @throws LedgerException if there is no account with this address
	 */
	public int getAccountBalance (String address) throws LedgerException {
		Account acct = mAccountMap.get (address);
		if (acct != null) {
			return (acct.getBalance ());
		} else {
			throw new LedgerException (OP_GET_BALANCE, "Account " + address + " does not exist");
		}
	}
	
	/**
	 * Collects balances of all accounts in the account map, as recorded in this block
	 * @return the map of account balances (in whole units) keyed by the account address
	 */
	public Map<String, Integer> getAccountBalances () {
		Map<String, Integer> ret = new HashMap<> ();
		for (Account acct: mAccountMap.values ()) {
			ret.put (acct.getAddress (), acct.getBalance ());
		}
		
		return (ret);
	}
	
	/**
	 * Looks up the transaction withthe given identifier in this block
	 * @param transaction identifier
	 * @return the transaction, if present in this block; null otherwise
	 */
	public Transaction getTransaction (String id) {
		return (mTransactions.get (id));
	}
	
	/**
	 * Accessor method for the seqiential number of this block in the chain (1-based)
	 * @return the block number
	 */
	public int getNumber () {
		return mNumber;
	}

	/**
	 * The string representation of this block. Includes: the number, the hash,
	 * the account map and the transaction list
	 */
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder ();
		sb.append ("Block ").append (mNumber).append (System.lineSeparator ());
		sb.append ("  Hash: ").append (mHash).append (System.lineSeparator ());
		sb.append ("  Accounts:");
		for (Account acct: mAccountMap.values ()) {
			sb.append (System.lineSeparator ()).append ("    ").append (acct.toString ());
		}
		sb.append (System.lineSeparator ()).append ("  Transactions:");
		for (Transaction transaction: mTransactions.values ()) {
			sb.append (System.lineSeparator ()).append ("    ").append (transaction.toString ());
		}
		
		return (sb.toString ());
	}
	
	/**
	 * Verifies that the stored hash of the previous block coincides with the one that's computed
	 * at the time of the call to this method.
	 * @throws LedgerException if the verification failed; does nothing if hashes are equal
	 */
	public void validate () throws LedgerException {
		if (mPrevBlock != null) {
			String computedHash = mPrevBlock.computeHash ();
			if (!mPrevHash.equals (computedHash)) {
				throw new LedgerException (OP_VALIDATE, "Verification of block " + mPrevBlock.getNumber () +
						"failed: expected hash " +
						mPrevHash + ", but computed " + computedHash);
			}
		}
	}
	
}
