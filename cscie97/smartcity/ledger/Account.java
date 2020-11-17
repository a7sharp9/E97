package cscie97.smartcity.ledger;

/**
 * The Account class keeps a correspondence between the account address string
 * and the balance for that account.
 */
public class Account implements Cloneable {
	/**
	 * The address of the account. Immutable.
	 */
	private final String mAddress;
	
	/**
	 * The balance of this account, in whole units
	 */
	private int mBalance;
	
	public Account (String address) {
		mAddress = address;
	}
	
	/**
	 * The account object must be capable of deep-cloning itself,
	 * for use in the code that initializes a new block
	 * from the previous block in the chain.
	 */
	@Override
	protected Account clone () {
		return (new Account (mAddress).withBalance (mBalance));
	}
	
	/**
	 * Helper method that increases (or decreases, depending on sign)
	 * the balance of the account by the required number of whole units
	 * @param The increment (decrement)
	 */
	public void addToBalance (int funds) {
		mBalance += funds;
	}

	/**
	 * The string representation of the account; contains its address and balance
	 */
	@Override
	public String toString () {
		return (mAddress + ": " + mBalance);
	}
	
	/*---------------------------------------------------*/
	/* public API */
	/*---------------------------------------------------*/

	/**
	 * Builder-pattern method for setting the account balance
	 * @param balance The new balance for this account (in whole units)
	 * @return The account
	 */
	public Account withBalance (int balance) {
		mBalance = balance;
		return (this);
	}


	/**
	 * Retrieves the string address of the account
	 * @return The address
	 */
	public String getAddress () {
		return (mAddress);
	}
	
	/**
	 * Retrieves the balance on this account
	 * @return The balance (in whole units)
	 */
	public int getBalance () {
		return mBalance;
	}

}
