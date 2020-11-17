package cscie97.smartcity.controller;

import cscie97.smartcity.ledger.LedgerConstants;
import cscie97.smartcity.ledger.LedgerException;
import cscie97.smartcity.ledger.Transaction;
import cscie97.smartcity.model.ModelService;

/**
 * The base class for all commands requiring interaction with the 
 * Ledger service. The addresses of the payer and receiver account
 * will be supplied by implementers
 */
public abstract class ChargeCommand implements Command, LedgerConstants {
	/**
	 * The amount (in blockchain units) to be transferred
	 */
	private final int mAmount;
	
	/**
	 * the unique index of the transaction; auto-increments
	 */
	private static Integer mCounter = 0;

	/**
	 * The payer and reciever account addresses
	 */
	public abstract String getFrom (ModelService service);
	public abstract String getTo (ModelService service);
	
	public ChargeCommand (int amount) {
		mAmount = amount;
	}
	
	public int getAmount () {
		return (mAmount);
	}
	
	@Override
	public void execute (Controller controller) throws ControllerException {
		String from = getFrom (controller.getModelService ());
		String to = getTo (controller.getModelService ());
		if (from == null || to == null) {
			return;
		}
		
		/**
		 * Create and execute transaction
		 */
		synchronized (mCounter) {
			Transaction t = new Transaction ("charge_" + (mCounter++), from, to, mAmount);
			try {
				controller.getLedger ().processTransaction (t);
			} catch (LedgerException lx) {
				throw new ControllerException (lx.getMessage ());
			}
		}
	}

}
