package org.lenzi.fstore.file2.web.rs;

import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;

/**
 * Base class for jax-rs resources
 * 
 * @author slenzi
 */
public abstract class AbstractResource {

	public AbstractResource() {
	
	}
	
	public abstract Logger getLogger();
	
	/**
	 * Handle error
	 * 
	 * @param message
	 * @param type
	 * @param e
	 * @throws WebServiceException
	 */
	public void handleError(String message, WebExceptionType type, Throwable e) throws WebServiceException {
		
		e.printStackTrace();
		getLogger().error(message + ", " + e.getMessage(), e);		
		throw new WebServiceException(type, message + ", " + e.getMessage());
		
	}

}
