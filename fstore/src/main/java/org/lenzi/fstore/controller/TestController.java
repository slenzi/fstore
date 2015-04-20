/**
 * 
 */
package org.lenzi.fstore.controller;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.properties.ManagedProperties;
import org.lenzi.fstore.repository.model.FSTree;
import org.lenzi.fstore.repository.model.Person;
import org.lenzi.fstore.service.FSTreeService;
import org.lenzi.fstore.service.PersonService;
import org.lenzi.fstore.service.exception.ServiceException;
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
    private PersonService personService;
    
    @Autowired
    private FSTreeService treeService; 
    
    @InjectLogger
    Logger logger;

	@RequestMapping(method = RequestMethod.GET)
	public String printHello(ModelMap model) {
		
		logger.info("printHello called");
		
		logger.info("Closure type => " + treeService.getClosureRepoType());
		
		Long treeId = 1L;
		FSTree fsTree = null;
		try {
			fsTree = treeService.getTree(treeId);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error("Error getting FSTree for id " + treeId);
		}

		Tree<TreeMeta> tree = null;
		try {
			tree = treeService.buildTree(fsTree);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error("Error builder tree for FSTree with id " + treeId);
		}
		
		String treeData = tree.printHtmlTree();
		
		StringBuffer buff = new StringBuffer();
		buff.append("Hello! This is the \"" + appProps.getAppTitle() + "\" application.");
		buff.append("<br><br>");
		buff.append(treeData);
		
		/*
		Person per = personService.getPersonById(1);
		if(per != null){
			buff.append(" Fetched " + per.getFirstName() + " " + per.getLastName() + " from the database.");
		}else{
			buff.append(" Failed to fetch person from database.");
		}
		*/
		
		model.addAttribute("message", buff.toString());
		
		return "/test/test.jsp";
	}

}
