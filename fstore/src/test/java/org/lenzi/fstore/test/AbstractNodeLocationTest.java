package org.lenzi.fstore.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

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
 * Print tree test
 * 
 * @author sal
 */
public abstract class AbstractNodeLocationTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractNodeLocationTest() {

	}
	
	/**
	 * Build sample tree, a check if various nodes are parent nodes of other nodes in the tree.
	 */
	public void isParent() throws ServiceException {
		
		logTestTitle("Node Location Test: Is Parent");
		/*
		FSTreeService treeService = getTreeSerive();
		
		logger.info("Creating sample tree");
		FSTree fsTree = treeService.createTree("Sample Tree", "A sample test tree", "A");
		
		assertNotNull(fsTree);
		assertNotNull(fsTree.getRootNode());
		
		logger.info("Tree created. root note id => " + fsTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		FSNode nodeB = treeService.createNode(fsTree.getRootNode(), "B");
			FSNode nodeD = treeService.createNode(nodeB,"D");
				FSNode nodeE = treeService.createNode(nodeD,"E");
					FSNode nodeI = treeService.createNode(nodeE,"I");
					FSNode nodeJ = treeService.createNode(nodeE,"J");
				FSNode nodeF = treeService.createNode(nodeD,"F");
			FSNode nodeG = treeService.createNode(nodeB,"G");
				FSNode nodeH = treeService.createNode(nodeG,"H");
					FSNode nodeK = treeService.createNode(nodeH,"K");
					FSNode nodeL = treeService.createNode(nodeH,"L");
		FSNode nodeM = treeService.createNode(fsTree.getRootNode(), "M");
			FSNode nodeN = treeService.createNode(nodeM, "N");
				FSNode nodeO = treeService.createNode(nodeN, "O");
					FSNode nodeP = treeService.createNode(nodeO, "P");
						FSNode nodeQ = treeService.createNode(nodeP, "Q");
		
		logger.info("Finished adding nodes to tree...");
						
		Tree<TreeMeta> tree = treeService.buildTree(fsTree);
		
		assertNotNull(tree);
		
		logger.info(tree.printTree());
		
		boolean isParent = false;
		
		logger.info("Check if M is a parent of P. Expected TRUE.");
		isParent = treeService.isParent(nodeM, nodeP, true);
		logger.info(" => " + isParent);
		assertTrue(isParent);
		
		logger.info("Check if D is a parent of J. Expected TRUE.");
		isParent = treeService.isParent(nodeD, nodeJ, true);
		logger.info(" => " + isParent);
		assertTrue(isParent);
		
		logger.info("Check if K is a parent of B. Expected FALSE.");
		isParent = treeService.isParent(nodeK, nodeB, true);
		logger.info(" => " + isParent);
		assertFalse(isParent);
		*/
	}
	
	/**
	 * Build sample tree, a check if various nodes are child nodes of other nodes in the tree.
	 */
	public void isChild() throws ServiceException {
		
		logTestTitle("Node Location Test: Is Child");
		/*
		FSTreeService treeService = getTreeSerive();
		
		logger.info("Creating sample tree");
		FSTree fsTree = treeService.createTree("Sample Tree", "A sample test tree", "A");
		
		assertNotNull(fsTree);
		assertNotNull(fsTree.getRootNode());
		
		logger.info("Tree created. root note id => " + fsTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		FSNode nodeB = treeService.createNode(fsTree.getRootNode(), "B");
			FSNode nodeD = treeService.createNode(nodeB,"D");
				FSNode nodeE = treeService.createNode(nodeD,"E");
					FSNode nodeI = treeService.createNode(nodeE,"I");
					FSNode nodeJ = treeService.createNode(nodeE,"J");
				FSNode nodeF = treeService.createNode(nodeD,"F");
			FSNode nodeG = treeService.createNode(nodeB,"G");
				FSNode nodeH = treeService.createNode(nodeG,"H");
					FSNode nodeK = treeService.createNode(nodeH,"K");
					FSNode nodeL = treeService.createNode(nodeH,"L");
		FSNode nodeM = treeService.createNode(fsTree.getRootNode(), "M");
			FSNode nodeN = treeService.createNode(nodeM, "N");
				FSNode nodeO = treeService.createNode(nodeN, "O");
					FSNode nodeP = treeService.createNode(nodeO, "P");
						FSNode nodeQ = treeService.createNode(nodeP, "Q");
		
		logger.info("Finished adding nodes to tree...");
						
		Tree<TreeMeta> tree = treeService.buildTree(fsTree);
		
		assertNotNull(tree);
		
		logger.info(tree.printTree());
		
		boolean isParent = false;
		
		logger.info("Check if P is a child of M. Expected TRUE.");
		isParent = treeService.isChild(nodeP, nodeM, true);
		logger.info(" => " + isParent);
		assertTrue(isParent);
		
		logger.info("Check if J is a child of A. Expected TRUE.");
		isParent = treeService.isChild(nodeJ, fsTree.getRootNode(), true);
		logger.info(" => " + isParent);
		assertTrue(isParent);
		
		logger.info("Check if A is a child of H. Expected FALSE.");
		isParent = treeService.isChild(fsTree.getRootNode(), nodeH, true);
		logger.info(" => " + isParent);
		assertFalse(isParent);
		*/
	}
	
	/**
	 * Build two trees, a check if various nodes are in the same tree.
	 */
	public void isSameTree() throws ServiceException {
		
		logTestTitle("Node Location Test: Is Same Tree");
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
		
		logger.info("Tree 1");
		tree = treeService.buildTree(fsTree1);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		
		logger.info("Tree 2");
		tree = treeService.buildTree(fsTree2);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		boolean isSameTree = false;
		
		logger.info("Check if D1 is in same tree as H1. Expected TRUE.");
		isSameTree = treeService.isSameTree(nodeD1, nodeH1);
		logger.info(" => " + isSameTree);
		assertTrue(isSameTree);
		
		logger.info("Check if L1 is in same tree as A1. Expected TRUE.");
		isSameTree = treeService.isSameTree(nodeL1, fsTree1.getRootNode());
		logger.info(" => " + isSameTree);
		assertTrue(isSameTree);
		
		logger.info("Check if K1 is in same tree as H2. Expected FALSE.");
		isSameTree = treeService.isSameTree(nodeK1, nodeH2);
		logger.info(" => " + isSameTree);
		assertFalse(isSameTree);
		
		logger.info("Check if J2 is in same tree as I1. Expected FALSE.");
		isSameTree = treeService.isSameTree(nodeJ2, nodeI1);
		logger.info(" => " + isSameTree);
		assertFalse(isSameTree);		
		*/
	}	

}
