package cscie97.smartcity.model;
public class ModelServiceException extends Exception {
	private static final long serialVersionUID = 1L;

	private final String mOperation;
	private final String mSubject;
	
	public ModelServiceException (String operation, String subject, String reason) {
		super (reason);
		mOperation = operation;
		mSubject = subject;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder ("Error performing ");
		sb.append (mOperation);
		if (mSubject != null && !mSubject.isEmpty ()) {
			if (!mOperation.isEmpty ()) {
				sb.append (' ');
			}
			sb.append (mSubject);
		}
		sb.append (": ");

		return (sb.append (getMessage ()).toString ());
	}
}
