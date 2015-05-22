package org.lenzi.fstore.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

public class AbstractGetParentNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TestTreeService treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger;
	
	public AbstractGetParentNodeTest() {

	}
	
	@Test
	@Rollback(true)
	public void getParentNodeTest() throws ServiceException {

		logTestTitle("Get parent node test");
		
		FSTestNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node","Sample root test value"));
			assertNotNull(rootNode);
			
			FSTestNode childNode1 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 1","Sample child test value 1"));
			assertNotNull(childNode1);
			
			FSTestNode childNode2 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 2","Sample child test value 2"));
			assertNotNull(childNode2);
			
				FSTestNode childNode3 = treeService.createChildNode(childNode2, new FSTestNode("Sample child node 3","Sample child test value 3"));
				assertNotNull(childNode3);
		
		// get parent of node 3
		FSTestNode parentOfThree = treeService.getParentNode(childNode3);
		// get parent of node 2
		FSTestNode parentOfTwo = treeService.getParentNode(childNode2);
		// get parent of root node
		FSTestNode parentOfRoot = treeService.getParentNode(rootNode);	
		
		assertNotNull(parentOfThree);
		assertNotNull(parentOfTwo);
		assertNull(parentOfRoot);
		
		// parent of node 3 should be node 2
		assertEquals(parentOfThree.getNodeId(), childNode2.getNodeId());
		// parent of node 2 should be root node
		assertEquals(parentOfTwo.getNodeId(), rootNode.getNodeId());		
		
		logger.info("Parent of 3 => " + parentOfThree.toString());
		logger.info("Parent of 2 => " + parentOfTwo.toString());
		
	}	

}
