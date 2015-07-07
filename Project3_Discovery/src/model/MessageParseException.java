package model;

/**
 * @author panwang
 *
 */
public class MessageParseException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4748695780096763105L;

	public MessageParseException() {
		super();
	}

	public MessageParseException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MessageParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageParseException(String message) {
		super(message);
	}

	public MessageParseException(Throwable cause) {
		super(cause);
	}
	
}
