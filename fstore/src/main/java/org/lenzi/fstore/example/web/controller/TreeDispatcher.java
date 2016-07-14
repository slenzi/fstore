/**
 * 
 */
package org.lenzi.fstore.example.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests for trees and serves them up.
 * 
 * @author slenzi
 */
@Controller
@RequestMapping("/example/tree")
public class TreeDispatcher extends AbstractSpringController {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private TestTreeService treeService;
	
	/**
	 * 
	 */
	public TreeDispatcher() {
		
	}
	
	/**
	 * Handles request such as /tree/1/5/6/8 where 1 is a root node and is a parent node of 5, 5 is a parent of 6,
	 * and 6 is a parent of 8. If no such tree exists then an error should be thrown.
	 * 
	 */
	@RequestMapping("/**")
	public String dispatchResource(HttpServletRequest request, HttpServletResponse response, Model model){
	
		logger.info("Dispatching tree for request for URL => " + request.getRequestURL());
		
		return "/test/test.jsp";
		
	}

}
