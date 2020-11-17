package cscie97.smartcity.controller;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import cscie97.smartcity.authentication.AuthenticationService;
import cscie97.smartcity.authentication.AuthenticationException;
import cscie97.smartcity.ledger.Ledger;
import cscie97.smartcity.ledger.LedgerException;
import cscie97.smartcity.model.Device;
import cscie97.smartcity.model.EventObserver;
import cscie97.smartcity.model.ModelService;
import cscie97.smartcity.model.SensorEvent;

/**
 * Represents the Controller object. Consumes model service events through a pub-sub model
 * (Observer pattern) and responds to them by constructing and executing instances of
 * commands (Command pattern).
 */
public class Controller implements EventObserver {

	/**
	 * Model service instance; used to register with it as an observer and for
	 * access to the API
	 */
	private final ModelService mModelService;
	
	/**
	 * Ledger instance; used for access to transaction API when needed by a command
	 */
	private final Ledger mLedger;
	
	/**
	 * CO2LevelMonitor instances keyed by city; used for accumulating CO2 level reports
	 */
	private final Map<String, CO2LevelMonitor> mMonitors;
	
	/**
	 * CommandFactory instance
	 */	
	private final CommandFactory mFactory;
	
	public Controller () throws ControllerException {
		mModelService = new ModelService ();
		try {
			mLedger = new Ledger ("controller", "");
		} catch (LedgerException lx) {
			throw new ControllerException (lx.getMessage ());
		}
		
		mModelService.withLedger (mLedger);
		
		try {
			mModelService.withAuthService (AuthenticationService.instance ());
		} catch (AuthenticationException ax) {
			throw new ControllerException (ax.getMessage ());
		}
		
		// register as a subscriber
		mModelService.attach (this);
		
		mFactory = CommandFactory.instance ();
		mMonitors = new HashMap<> ();
	}
	
	private PrintStream mPrintStream = System.out;
	
	public Controller withPrintStream (PrintStream ps) {
		mPrintStream = ps;
		return (this);
	}
	
	public PrintStream getPrintStream () {
		return (mPrintStream);
	}
	
	/**
	 * The callback from the model service when an event is published
	 */
	@Override
	public void event (Device<?> device, SensorEvent event) {
		Command command = mFactory.build (device, event);
		if (command != null) {
			try {
				String out = command.toString ();
				if (out != null && !out.isEmpty ()) {
					mPrintStream.println ("Executing command: " + out);
				}
				command.execute (this);
			} catch (ControllerException cx) {
				mPrintStream.println ("Error: " + cx.getMessage ());
			}
		} else {
			mPrintStream.println ("Error: unrecognized event " + event);
		}
	}

	public ModelService getModelService () {
		return mModelService;
	}

	public Ledger getLedger () {
		return mLedger;
	}
	

	public CO2LevelMonitor getMonitor (String city) {
		CO2LevelMonitor ret = mMonitors.get (city);
		if (ret == null) {
			ret = new CO2LevelMonitor ();
			mMonitors.put (city, ret);
		}
		
		return (ret);
	}
}
