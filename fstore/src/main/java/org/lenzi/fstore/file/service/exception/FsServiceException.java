package org.lenzi.fstore.file.service.exception;

public class FsServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -992834249814429285L;

	public FsServiceException() {

	}

	public FsServiceException(String message) {
		super(message);
	
	}

	public FsServiceException(Throwable cause) {
		super(cause);

	}

	public FsServiceException(String message, Throwable cause) {
		super(message, cause);

	}

	public FsServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	
	}

}
