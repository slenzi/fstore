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
			sampleTree = getSampleTree();
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
		
		String treeData = tree.printHtmlTree();
		
		StringBuffer buff = new StringBuffer();
		buff.append("Hello! This is the \"" + appProps.getAppTitle() + "\" application.");
		buff.append("<br><br>");
		buff.append(treeData);		
		
		model.addAttribute("message", buff.toString());
		
		return "/test/test.jsp";
		
	}
	
	/**
	 * Fetch sample tree, creating it if necessary
	 * 
	 * @return
	 */
	private FSTree<FSTestNode> getSampleTree() throws ServiceException {
		
		Long treeId = 1L;
		FSTree<FSTestNode> sampleTree = null;
		
		try {
			sampleTree = testTreeService.geTreeById(new FSTree<FSTestNode>(treeId));
		} catch (ServiceException e) {
			logger.error("Error getting tree for tree id => " + treeId + ". " + e.getMessage());
		}
		
		if(sampleTree == null){
		
			sampleTree = testTreeService.addTree(
					new FSTree<FSTestNode>("Sample tree", "Sample tree description."),
					new FSTestNode("Root","Sample root node"));
			
			FSTestNode nodeB = testTreeService.createChildNode(sampleTree.getRootNode(), new FSTestNode("B","Node B"));
				FSTestNode nodeC = testTreeService.createChildNode(nodeB, new FSTestNode("C","Node C"));
					FSTestNode nodeD = testTreeService.createChildNode(nodeC, new FSTestNode("D","Node D"));
						FSTestNode nodeE = testTreeService.createChildNode(nodeD, new FSTestNode("E","Node E"));
							FSTestNode nodeF = testTreeService.createChildNode(nodeE, new FSTestNode("F","Node F"));
							FSTestNode nodeG = testTreeService.createChildNode(nodeE, new FSTestNode("G","Node G"));
						FSTestNode nodeH = testTreeService.createChildNode(nodeD, new FSTestNode("H","Node H"));
							FSTestNode nodeI = testTreeService.createChildNode(nodeH, new FSTestNode("I","Node I"));
							FSTestNode nodeJ = testTreeService.createChildNode(nodeH, new FSTestNode("J","Node J"));
						FSTestNode nodeK = testTreeService.createChildNode(nodeD, new FSTestNode("K","Node K"));
							FSTestNode nodeL = testTreeService.createChildNode(nodeK, new FSTestNode("L","Node L"));
							FSTestNode nodeM = testTreeService.createChildNode(nodeK, new FSTestNode("M","Node M"));
							FSTestNode nodeN = testTreeService.createChildNode(nodeK, new FSTestNode("N","Node N"));
							FSTestNode nodeO = testTreeService.createChildNode(nodeK, new FSTestNode("O","Node O"));				

		}
		
		return sampleTree;
		
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
