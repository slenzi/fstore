package org.lenzi.fstore.test.core;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.core.logging.ClosureLogger;
import org.lenzi.fstore.core.repository.model.DBClosure;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

public class AbstractAddNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TestTreeService treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger;
	
	public AbstractAddNodeTest() {

	}
	
	@Test
	@Rollback(true)
	public void addRootNodeTest() throws ServiceException {
		
		logTestTitle("Add root node test");
		
		FSTestNode rootNode = treeService.createRootNode( new FSTestNode("Sample root node 1","Sample root test value 1"));
		assertNotNull(rootNode);
		
		List<DBClosure<FSTestNode>> closureList = treeService.getClosure(rootNode);
		assertNotNull(closureList);
		
		logger.info("Closure:");
		closureLogger.logClosure(closureList);
		
		Tree<FSTestNode> tree = treeService.buildTree(rootNode);
		assertNotNull(tree);
		
		logger.info("Tree:");
		logger.info(tree.printTree());	
		
	}
	
	@Test
	@Rollback(true)
	public void addNodeTreeTest() throws ServiceException {

		logTestTitle("Add node tree");
		
		FSTestNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node","Sample root test value"));
			assertNotNull(rootNode);
			
			FSTestNode childNode1 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 1","Sample child test value 1"));
			assertNotNull(childNode1);
			
			FSTestNode childNode2 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 2","Sample child test value 2"));
			assertNotNull(childNode2);
			
				FSTestNode childNode3 = treeService.createChildNode(childNode2, new FSTestNode("Sample child node 3","Sample child test value 3"));
				assertNotNull(childNode3);
				
		List<DBClosure<FSTestNode>> closureList = treeService.getClosure(rootNode);
		assertNotNull(closureList);
		
		logger.info("Closure:");
		closureLogger.logClosure(closureList);
		
		Tree<FSTestNode> tree = treeService.buildTree(rootNode);
		assertNotNull(tree);
		
		logger.info("Tree:");
		logger.info(tree.printTree());
		
	}	

}
