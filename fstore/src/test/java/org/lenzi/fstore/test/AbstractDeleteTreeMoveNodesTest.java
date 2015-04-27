package org.lenzi.fstore.test;

import static org.junit.Assert.*;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.repository.model.impl.FSTree;
import org.lenzi.fstore.service.FSTreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete tree tests
 * 
 * @author sal
 */
public abstract class AbstractDeleteTreeMoveNodesTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractDeleteTreeMoveNodesTest() {

	}
	

	/**
	 * Create two trees.  Move the root node with all children from the first tree to under a
	 * node on the second tree, then delete first tree.
	 * 
	 * @throws ServiceException
	 */
	public void deleteTreeButMoveNodes() throws ServiceException {
		/*
		FSTreeService treeService = getTreeSerive();
		
		logger.info("Creating sample tree 1");
		FSTree fsTree1 = treeService.createTree("Sample Tree 1", "A sample test tree #1", "A1");
		
		assertNotNull(fsTree1);
		assertNotNull(fsTree1.getRootNode());
		
		logger.info("Tree 1 created. root note id => " + fsTree1.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree 1...");
		
		FSNode nodeB1 = treeService.createNode(fsTree1.getRootNode(), "B1");
			FSNode nodeD1 = treeService.createNode(nodeB1,"D1");
				FSNode nodeE1 = treeService.createNode(nodeD1,"E1");
					FSNode nodeI1 = treeService.createNode(nodeE1,"I1");
					FSNode nodeJ1 = treeService.createNode(nodeE1,"J1");
				FSNode nodeF1 = treeService.createNode(nodeD1,"F1");
			FSNode nodeG1 = treeService.createNode(nodeB1,"G1");
				FSNode nodeH1 = treeService.createNode(nodeG1,"H1");
					FSNode nodeK1 = treeService.createNode(nodeH1,"K1");
					FSNode nodeL1 = treeService.createNode(nodeH1,"L1");
		
		logger.info("Finished adding nodes to tree 1...");
		
		
		logger.info("Creating sample tree 2");
		FSTree fsTree2 = treeService.createTree("Sample Tree 2", "A sample test tree #2", "A2");
		
		assertNotNull(fsTree2);
		assertNotNull(fsTree2.getRootNode());
		
		logger.info("Tree 2 created. root note id => " + fsTree2.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree 2...");
		
		FSNode nodeB2 = treeService.createNode(fsTree2.getRootNode(), "B2");
			FSNode nodeD2 = treeService.createNode(nodeB2,"D2");
				FSNode nodeE2 = treeService.createNode(nodeD2,"E2");
					FSNode nodeI2 = treeService.createNode(nodeE2,"I2");
					FSNode nodeJ2 = treeService.createNode(nodeE2,"J2");
				FSNode nodeF2 = treeService.createNode(nodeD2,"F2");
			FSNode nodeG2 = treeService.createNode(nodeB2,"G2");
				FSNode nodeH2 = treeService.createNode(nodeG2,"H2");
					FSNode nodeK2 = treeService.createNode(nodeH2,"K2");
					FSNode nodeL2 = treeService.createNode(nodeH2,"L2");
		
		logger.info("Finished adding nodes to tree 2...");

		Tree<TreeMeta> tree = null;
		
		logger.info("Tree 1 Before");
		tree = treeService.buildTree(fsTree1);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Tree 2 Before");
		tree = treeService.buildTree(fsTree2);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		Long deleteTreeId = fsTree1.getTreeId();
		
		logger.info("Moving all nodes from Tree 1 to under node H2 of Tree 2, then deleting Tree 1.");
		treeService.removeTree(fsTree1, nodeH2);
		
		logger.info("Tree 2 After");
		tree = treeService.buildTree(fsTree2);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		FSTree deletedTree = getTree(deleteTreeId);
		
		assertNull(deletedTree);
		*/
	}
	
	/**
	 * Fetch a tree
	 * 
	 * @param treeId
	 * @return
	 * @throws ServiceException
	 */
	/*
	public FSTree getTree(Long treeId) throws ServiceException {
		
		logger.info("Getting tree with id => " + treeId);
		
		FSTreeService treeService = getTreeSerive();
		
		return treeService.getTree(treeId);
		
	}
	*/	

}
