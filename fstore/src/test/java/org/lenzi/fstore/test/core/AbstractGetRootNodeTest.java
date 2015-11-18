package org.lenzi.fstore.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.core.logging.ClosureLogger;
import org.lenzi.fstore.core.repository.tree.model.DBClosure;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

public class AbstractGetRootNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TestTreeService treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger;
	
	public AbstractGetRootNodeTest() {

	}
	
	@Test
	@Rollback(true)
	public void getRootNodeTest() throws ServiceException {

		logTestTitle("Get root node test");
		
		FSTestNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node","Sample root test value"));
			assertNotNull(rootNode);
			
			FSTestNode childNode1 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 1","Sample child test value 1"));
			assertNotNull(childNode1);
			
			FSTestNode childNode2 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 2","Sample child test value 2"));
			assertNotNull(childNode2);
			
				FSTestNode childNode3 = treeService.createChildNode(childNode2, new FSTestNode("Sample child node 3","Sample child test value 3"));
				assertNotNull(childNode3);
		
		// should all be the same
		FSTestNode rootForRoot = treeService.getRootNode(rootNode);
		FSTestNode rootForChild1 = treeService.getRootNode(childNode1);
		FSTestNode rootForChild2 = treeService.getRootNode(childNode2);
		FSTestNode rootForChild3 = treeService.getRootNode(childNode3);
				
		assertNotNull(rootForRoot);
		assertNotNull(rootForChild1);
		assertNotNull(rootForChild2);
		assertNotNull(rootForChild3);

		// all should be the same
		assertEquals(rootForRoot.getNodeId(), rootNode.getNodeId());
		assertEquals(rootForChild1.getNodeId(), rootNode.getNodeId());
		assertEquals(rootForChild2.getNodeId(), rootNode.getNodeId());
		assertEquals(rootForChild3.getNodeId(), rootNode.getNodeId());
		
		logger.info("Root of root => " + rootForRoot.toString());
		logger.info("Root of child 1 => " + rootForChild1.toString());
		logger.info("Root of child 2 => " + rootForChild2.toString());
		logger.info("Root of child 3 => " + rootForChild3.toString());
		
	}	

}
