package cscie97.smartcity.controller;

/**
 * Encapsulates the decision-making process in response to accumulating
 * CO2 level notifications
 */
public class CO2LevelMonitor {
	/**
	 * The running count of elevated reading warnings
	 */
	private int mCO2LevelNotifications = 0;
	
	/**
	 * Keeps the current state of the vehicles
	 */
	private boolean mDisabled = false;

	private final static int MAX_CO2_LEVEL = 1000;
	private final static int MAX_CO2_NOTIFICATIONS = 3;
	
	public synchronized void notifyCO2 (int level) {
		if (level > MAX_CO2_LEVEL) {
			// add one more warning, unless already over the threshold
			mCO2LevelNotifications = Math.min (MAX_CO2_NOTIFICATIONS, mCO2LevelNotifications + 1); 
		} else {
			// subtract a warning, unless already at 0
			mCO2LevelNotifications = Math.max (0, mCO2LevelNotifications - 1);
		}
	}
	
	public boolean needEnable () {
		return (mDisabled && mCO2LevelNotifications <= 0);
	}
	
	public boolean needDisable () {
		return (!mDisabled && mCO2LevelNotifications >= MAX_CO2_NOTIFICATIONS);
	}
	
	public synchronized void setDisabled (boolean flag) {
		mDisabled = flag;
	}
	
	public boolean isDisabled () {
		return (mDisabled);
	}
}