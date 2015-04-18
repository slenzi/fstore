/**
 * 
 */
package org.lenzi.fstore.controller;

import org.lenzi.fstore.properties.ManagedProperties;
import org.lenzi.fstore.repository.model.Person;
import org.lenzi.fstore.service.PersonService;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author slenzi
 *
 * Test controller to make sure Spring MVC is working.
 */
@Controller
@RequestMapping("/test")
public class TestController {
	
    @Autowired
    private ManagedProperties appProps;
    
    @Autowired
    PersonService personService;
    
    @InjectLogger
    Logger logger;

	@RequestMapping(method = RequestMethod.GET)
	public String printHello(ModelMap model) {
		
		logger.info("printHello called");
		
		StringBuffer buff = new StringBuffer();
		buff.append("Hello! This is the \"" + appProps.getAppTitle() + "\" application.");
		
		Person per = personService.getPersonById(1);
		if(per != null){
			buff.append(" Fetched " + per.getFirstName() + " " + per.getLastName() + " from the database.");
		}else{
			buff.append(" Failed to fetch person from database.");
		}
		
		model.addAttribute("message", buff.toString());
		
		return "/test/test.jsp";
	}

}
