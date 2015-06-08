/**
 * 
 */
package org.lenzi.fstore.file.web.controller;

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
 * Handles requests and serves up resources from fstore.
 * 
 * @author slenzi
 */
@Controller
@RequestMapping("/file/res")
public class FsResourceDispatcher extends AbstractSpringController {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private TestTreeService treeService;
	
	/**
	 * 
	 */
	public FsResourceDispatcher() {
		
	}
	
	@RequestMapping("/**")
	public String dispatchResource(HttpServletRequest request, HttpServletResponse response, Model model){
	
		logger.info("Dispatching request for URL => " + request.getRequestURL());
		
		return "/test/filetest/test.jsp";
		
	}

}
