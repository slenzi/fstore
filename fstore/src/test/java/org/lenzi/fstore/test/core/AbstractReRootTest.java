/**
 * 
 */
package org.lenzi.fstore.test.core;

import org.junit.Test;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * @author sal
 *
 */
public class AbstractReRootTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TestTreeService treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger = null;		
	
	/**
	 * 
	 */
	public AbstractReRootTest() {
	
	}
	
	@Test
	@Rollback(true)
	public void reRootTest() throws ServiceException{

		logger.info("Creating sample tree");
		
		/*
		
		logger.info("Creating sample tree 1");
		DBTree<FSTestNode> fsTree1 = treeService.createTree("Sample Tree 1", "A sample test tree #1", "A1");
		
		assertNotNull(fsTree1);
		assertNotNull(fsTree1.getRootNode());
		
		logger.info("Tree 1 created. root note id => " + fsTree1.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree 1...");
		
		FSTestNode nodeB1 = treeService.createNode(fsTree1.getRootNode(), "B1");
			FSTestNode nodeD1 = treeService.createNode(nodeB1,"D1");
				FSTestNode nodeE1 = treeService.createNode(nodeD1,"E1");
					FSTestNode nodeI1 = treeService.createNode(nodeE1,"I1");
					FSTestNode nodeJ1 = treeService.createNode(nodeE1,"J1");
				FSTestNode nodeF1 = treeService.createNode(nodeD1,"F1");
			FSTestNode nodeG1 = treeService.createNode(nodeB1,"G1");
				FSTestNode nodeH1 = treeService.createNode(nodeG1,"H1");
					FSTestNode nodeK1 = treeService.createNode(nodeH1,"K1");
					FSTestNode nodeL1 = treeService.createNode(nodeH1,"L1");
		
		logger.info("Finished adding nodes to tree 1...");
		
		Tree<TreeMeta> tree = null;
		
		logger.info("Before moving node E1 to new tree");
		tree = treeService.buildTree(fsTree1);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		
		logger.info("Make node E1 the root of a new tree...");
		
		DBTree fsTree2 = treeService.createTree("Sample Tree 2", "A sample test tree #2", nodeE1);
		
		logger.info("After moving node E1 to new tree");
		tree = treeService.buildTree(fsTree1);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("New tree");
		tree = treeService.buildTree(fsTree2);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		*/
		
	}	

}
