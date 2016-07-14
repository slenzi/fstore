/**
 * 
 */
package org.lenzi.fstore.web.controller;

import org.lenzi.fstore.web.constants.WebConstants;
import org.slf4j.Logger;
import org.springframework.ui.ModelMap;

/**
 * Base spring controller class.
 * 
 * @author slenzi
 */
public abstract class AbstractSpringController {

	/**
	 * 
	 */
	public AbstractSpringController() {
		
	}
	
	/**
	 * Handle error
	 * 
	 * @param logger
	 * @param message
	 * @param model
	 */
	protected void handleError(Logger logger, String message, ModelMap model){
		
		logger.error(message);
		
		model.addAttribute(WebConstants.APP_REQUEST_ERROR_MESSAGE, message);
		
	}
	
	/**
	 * handle error
	 * 
	 * @param logger
	 * @param message
	 * @param model
	 * @param e
	 */
	protected void handleError(Logger logger, String message, ModelMap model, Throwable e){
		
		logger.error(message + " => " + e.getMessage(), e);
		
		model.addAttribute(WebConstants.APP_REQUEST_ERROR_MESSAGE, message + " => " + e.getMessage());
		
	}

}
