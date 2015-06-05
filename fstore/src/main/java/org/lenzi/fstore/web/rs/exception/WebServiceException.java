/**
 * 
 */
package org.lenzi.fstore.web.rs.exception;

/**
 * @author slenzi
 *
 */
public class WebServiceException extends Exception {

	public static final int CODE_UNKNOWN = 0;
	public static final int CODE_NOT_FOUND = 1;
	public static final int CODE_MISSING_REQUIRED_INPUT = 2;
	public static final int CODE_INVALID_INPUT = 3;
	public static final int CODE_DATABSE_ERROR = 4;
	
	private int errorCode = WebServiceException.CODE_UNKNOWN;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1429984561375153208L;

	/**
	 * 
	 */
	public WebServiceException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode; 
	}

	/**
	 * @param cause
	 */
	public WebServiceException(int errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WebServiceException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public WebServiceException(int errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
