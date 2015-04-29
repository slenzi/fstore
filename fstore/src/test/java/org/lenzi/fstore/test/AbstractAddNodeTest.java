package org.lenzi.fstore.test;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.impl.FSTestNode;
import org.lenzi.fstore.service.TreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Print tree test
 * 
 * @author sal
 */
public abstract class AbstractAddNodeTest extends AbstractTreeTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractAddNodeTest() {

	}
	
	/**
	 * Add a root node
	 */
	public void addRootNode() throws ServiceException {
		
		logTestTitle("Add root node test");
		
		TreeService treeService = getTreeSerive();
		ClosureLogger closureLogger = getClosureLogger();
		
		DBNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node 1","Sample root test value 1"));
		assertNotNull(rootNode);
		
		List<DBClosure> closureList = treeService.getClosure(rootNode);
		assertNotNull(closureList);
		
		logger.info("Closure:");
		closureLogger.logClosure(closureList);
		
		Tree<TreeMeta> tree = treeService.buildTree(closureList);
		assertNotNull(tree);
		
		logger.info("Tree:");
		logger.info(tree.printTree());			
		
	}
	
	/**
	 * Add node tree.
	 */
	public void addNodeTree() throws ServiceException {
		
		logTestTitle("Add node tree");
		
		TreeService treeService = getTreeSerive();
		ClosureLogger closureLogger = getClosureLogger();
		
		DBNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node","Sample root test value"));
			assertNotNull(rootNode);
			DBNode childNode1 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 1","Sample child test value 1"));
			assertNotNull(childNode1);
			DBNode childNode2 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 2","Sample child test value 2"));
			assertNotNull(childNode2);
				DBNode childNode3 = treeService.createChildNode(childNode2, new FSTestNode("Sample child node 3","Sample child test value 3"));
				assertNotNull(childNode3);
				
		List<DBClosure> closureList = treeService.getClosure(rootNode);
		assertNotNull(closureList);
		
		logger.info("Closure:");
		closureLogger.logClosure(closureList);
		
		Tree<TreeMeta> tree = treeService.buildTree(closureList);
		assertNotNull(tree);
		
		logger.info("Tree:");
		logger.info(tree.printTree());			
		
	}	

}
