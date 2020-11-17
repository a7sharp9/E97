package cscie97.smartcity.ledger;

/**
 * This class representc a single transaction - that is, a request to
 * transfre funds from one account to another. It is written as simply a
 * container for the necessary information; all verification and processing
 * is performed at the level of the block to which the transaction is
 * being appended
 */
public class Transaction implements LedgerConstants {
	/**
	 * The string identifier; must be unique within the ledger
	 */
	private final String mId;
	
	/**
	 * The amount of funds to be transferred (in whole units)
	 */
	private final int mAmount;
	
	/**
	 * The fee due to the Master account for performing this transaction
	 * Cannot be less than {@LedgerConstants#MINIMUM_FEE}
	 */
	private int mFee = MINIMUM_FEE;
	
	/**
	 * The optional string description of the transaction (check memo)
	 */
	private String mPayload;
	
	/**
	 * The account from which the funds originate. Immutable.
	 */
	private final String mPayer;
	
	/**
	 * The account to which the funds are routed. Immutable.
	 */
	private final String mReceiver;
	
	public Transaction (String id, String from, String to, int amount) {
		mId = id;
		mPayer = from;
		mReceiver = to;
		mAmount = amount;
	}
	
	/**
	 * Builder-pattern method for setting the transaction fee
	 * @param fee The fee for the transaction (in whole units)
	 * @return The transaction
	 */
	public Transaction withFee (Integer fee) {
		if (fee != null) mFee = fee;
		return (this);
	}
	
	/**
	 * Builder-pattern method for setting the optional transaction memo
	 * @param payload The memo for the transaction
	 * @return The transaction
	 */
	public Transaction withPayload (String payload) {
		mPayload = payload;
		return (this);
	}
	
	public String getId () {
		return mId;
	}

	public int getAmount () {
		return mAmount;
	}

	public int getFee () {
		return mFee;
	}

	public String getPayer () {
		return mPayer;
	}

	public String getReceiver () {
		return mReceiver;
	}

	/**
	 * The string representation of the transaction; contains its identifier,
	 * amount, the accounts between which the funds are transferred,
	 * the fee (if present) and the optional memo
	 */
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder ("transaction ");
		sb.append (mId).append (": ").append (mAmount);
		sb.append (" from ").append (mPayer).append (" to ").append (mReceiver);
		if (mFee != 0) sb.append (", fee ").append (mFee);
		if (mPayload != null) sb.append (", memo ").append (mPayload);
		
		return (sb.toString ());
	}
}
