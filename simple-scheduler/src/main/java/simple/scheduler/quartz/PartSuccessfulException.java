package simple.scheduler.quartz;

/**
 *
 */
public class PartSuccessfulException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 112312312312L;

	public PartSuccessfulException() {
		super();
	}

	public PartSuccessfulException(String message, Throwable cause) {
		super(message, cause);
	}

	public PartSuccessfulException(String message) {
		super(message);
	}

	public PartSuccessfulException(Throwable cause) {
		super(cause);
	}
	
}
