package org.lenzi.fstore.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.core.logging.ClosureLogger;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

public class AbstractGetChildNodesTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TestTreeService treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger;
	
	public AbstractGetChildNodesTest() {

	}
	
	@Test
	@Rollback(true)
	public void getChildNodesTest() throws ServiceException {

		logTestTitle("Get child nodes test");
		
		FSTestNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node","Sample root test value"));
			assertNotNull(rootNode);
			
			FSTestNode childNode1 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 1","Sample child test value 1"));
			assertNotNull(childNode1);
			
			FSTestNode childNode2 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 2","Sample child test value 2"));
			assertNotNull(childNode2);
			
				FSTestNode childNode3 = treeService.createChildNode(childNode2, new FSTestNode("Sample child node 3","Sample child test value 3"));
				assertNotNull(childNode3);
		
		logger.info("Getting children of root");
		List<FSTestNode> childrenOfRoot = treeService.getChildNodes(rootNode);
		logger.info("Getting children of node 2");
		List<FSTestNode> childrenOfTwo = treeService.getChildNodes(childNode2);
		logger.info("Getting children of node 1");
		List<FSTestNode> childrenOfOne = treeService.getChildNodes(childNode1);
		
		assertNotNull(childrenOfRoot);
		assertNotNull(childrenOfTwo);
		assertNull(childrenOfOne);
		
		assertEquals(childrenOfRoot.size(),2);
		assertEquals(childrenOfTwo.size(),1);
		
		for(FSTestNode n : childrenOfRoot){
			logger.info("Child of root => " + n.toString());
		}
		for(FSTestNode n : childrenOfTwo){
			logger.info("Child of two => " + n.toString());
		}		
		
	}	

}
