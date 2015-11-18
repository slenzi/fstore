/**
 * 
 */
package org.lenzi.fstore.example.web.controller;

import org.lenzi.fstore.core.repository.tree.model.impl.FSTree;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author slenzi
 *
 * Controller for testing our tree service which builds trees of FSTestNode objects.
 */
@Controller
@RequestMapping("/example/test")
public class TestController extends AbstractSpringController {
	
    @InjectLogger
    private Logger logger;
	
    @Autowired
    private ManagedProperties appProps;
    
    @Autowired
    private TestTreeService testTreeService; 

    /**
     * Fetch sample tree (creating it if necessary) and print it in HTML format.
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
			handleError(logger, "error getting sample tree", model, e);
			return "/test/test.jsp";
		}
		
		if(sampleTree == null){
			handleError(logger, "FSTree object is null. Cannot continue.", model);
			return "/test/test.jsp";
		}
		
		Tree<FSTestNode> tree = null;
		try {
			tree = testTreeService.buildTree(sampleTree.getRootNode());
		} catch (ServiceException e) {
			handleError(logger, "Error building tree for FSTree with id " + sampleTree.getTreeId(), model, e);
			return "/test/test.jsp";
		}
		
		String treeData = tree.printHtmlTree(
				n -> { return n.getName() + ": " + n.getTestValue() ; });
		
		StringBuffer buff = new StringBuffer();
		buff.append("Hello! This is the \"" + appProps.getAppTitle() + "\" application.");
		buff.append("<br><br>");
		buff.append(treeData);		
		
		model.addAttribute("test-data", buff.toString());
		
		return "/test/test.jsp";
		
	}

}
