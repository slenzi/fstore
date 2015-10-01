package org.lenzi.fstore.cms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handler for http errors 
 * 
 * @author sal
 */
@Controller
public class ErrorController {

	@InjectLogger
	private Logger logger;	
	
    @Autowired
    private ManagedProperties appProps;
    
	
	public ErrorController() {
		
	}
	
	/**
	 * Handle 404 not found
	 * 
	 * @return
	 */
	@RequestMapping("/error404")
	protected String error404(HttpServletRequest request, HttpServletResponse response) {
		
		String resourceUri = request.getRequestURL().toString() + "?" + request.getQueryString();
		
		logger.warn("Error 404, user requested resource that was not found on server. Requested resource = " + resourceUri);
		
	    return "/WEB-INF/jsp/system/error/not_found.jsp";
	    
	}	

}
