/**
 * 
 */
package org.lenzi.fstore.example.web.controller;

import org.lenzi.fstore.core.repository.model.impl.FSTree;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.main.properties.ManagedProperties;
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
    private TestTreeService testTreeService; 
    
    @InjectLogger
    private Logger logger;

    /**
     * 
     * 
     * @param model
     * @return
     */
	@RequestMapping(method = RequestMethod.GET)
	public String showSampleTree(ModelMap model) {
		
		logger.info("showSampleTree called");
		
		FSTree<FSTestNode> sampleTree = null;
		
		try {
			sampleTree = testTreeService.getSampleTree(1L);
		} catch (ServiceException e) {
			handleError(model, "error getting sample tree", e);
			return "/test/test.jsp";
		}
		
		if(sampleTree == null){
			handleError(model, "fs tree object is null. cannot continue.");
			return "/test/test.jsp";
		}
		
		Tree<FSTestNode> tree = null;
		try {
			tree = testTreeService.buildTree(sampleTree.getRootNode());
		} catch (ServiceException e) {
			handleError(model, "Error building tree for FSTree with id " + sampleTree.getTreeId(), e);
			return "/test/test.jsp";
		}
		
		String treeData = tree.printHtmlTree(
				n -> { return n.getName() + ": " + n.getTestValue() ; });
		
		StringBuffer buff = new StringBuffer();
		buff.append("Hello! This is the \"" + appProps.getAppTitle() + "\" application.");
		buff.append("<br><br>");
		buff.append(treeData);		
		
		model.addAttribute("message", buff.toString());
		
		return "/test/test.jsp";
		
	}
	
	private void handleError(ModelMap model, String message){
		logger.error(message);
		model.addAttribute("message", message);
	}	
	
	private void handleError(ModelMap model, String message, Throwable t){
		logger.error(message + ". " + t.getMessage());
		t.printStackTrace();
		model.addAttribute("message", message + ". " + t.getMessage());		
	}	

}
