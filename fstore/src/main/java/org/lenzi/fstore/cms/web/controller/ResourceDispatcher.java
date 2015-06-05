/**
 * 
 */
package org.lenzi.fstore.cms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.example.service.TestTreeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests and serves up resources from fstore.
 * 
 * @author slenzi
 */
@Controller
@RequestMapping("/res")
public class ResourceDispatcher {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private TestTreeService treeService;
	
	/**
	 * 
	 */
	public ResourceDispatcher() {
		
	}
	
	@RequestMapping("/**")
	public String dispatchResource(HttpServletRequest request, HttpServletResponse response, Model model){
	
		logger.info("Dispatching request for URL => " + request.getRequestURL());
		
		return "/test/test.jsp";
		
	}

}
