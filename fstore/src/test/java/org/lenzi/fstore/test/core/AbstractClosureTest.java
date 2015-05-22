package org.lenzi.fstore.test.core;

import static org.junit.Assert.assertNotNull;

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

public class AbstractClosureTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TestTreeService treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger;	
	
	public AbstractClosureTest() {

	}
	
	@Test
	@Rollback(true)
	public void fetchClosureDataTest() throws ServiceException {

		logTestTitle("Add sample node tree");
		
		FSTestNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node","Sample root test value"));
			FSTestNode childNode1 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 1","Sample child test value 1"));
			FSTestNode childNode2 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 2","Sample child test value 2"));
				FSTestNode childNode3 = treeService.createChildNode(childNode2, new FSTestNode("Sample child node 3","Sample child test value 3"));
		
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
