package cscie97.smartcity.authentication;

import java.time.Instant;
import java.util.UUID;

/**
 * The authorization token tied to a user of the auth service.
 */
public class Token {
	
	private final long EXPIRATION_MS = 5 * 1000L; // 5 seconds

	/**
	 * The id of the token itself
	 */
	private final String mId;
	
	/**
	 * The id of the user
	 */
	private final String mUserId;
	
	/**
	 * The epoch time when this token stops being valid
	 */
	private long mExpires;
	
	/**
	 * The epoch time of last update
	 */
	private long mUpdated;
	
	public Token (String userId) {
		Instant now = Instant.now ();
		
		mId = UUID.randomUUID ().toString (); // can repeat; up to caller to check and construct a new one
        mUserId = userId;
        
        mUpdated = now.toEpochMilli ();
        mExpires = now.plusMillis (EXPIRATION_MS).toEpochMilli ();
	}

	public Token withExpires (long expires) {
		mExpires = expires;
		return (this);
	}

	public long getUpdated () {
		return mUpdated;
	}

	public Token withUpdated (long updated) {
		mUpdated = updated;
		mExpires = Instant.ofEpochMilli (updated).plusMillis (EXPIRATION_MS).toEpochMilli ();
		return (this);
	}

	public String getId () {
		return mId;
	}

	public String getUserId () {
		return mUserId;
	}
	
	/**
	 * Check that this token is still valid at this moment in time
	 */
	private final boolean hasExpired () {
		return (Instant.now ().toEpochMilli () > mExpires);
	}
	
	public final boolean isValid () {
		return (!hasExpired ());
	}

}
