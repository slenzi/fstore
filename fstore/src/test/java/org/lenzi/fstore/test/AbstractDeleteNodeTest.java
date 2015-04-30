package org.lenzi.fstore.test;


import static org.junit.Assert.assertNotNull;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.DBTree;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.repository.model.impl.FSTestNode;
import org.lenzi.fstore.repository.model.impl.FSTree;
import org.lenzi.fstore.service.TreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle unit test, controlled by the ActiveProfiles annotation. Uses the OracleClosureRepository.
 * 
 * @author slenzi
 */
public abstract class AbstractDeleteNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractDeleteNodeTest() {
		
	}
	
	/**
	 * Build sample tree, print before, delete leaf node, then print after.
	 */
	public void deleteLeafNode() throws ServiceException {
		
		logTestTitle("Delete leaf node test");
		
		TreeService treeService = getTreeSerive();
		
		logger.info("Creating sample tree");
		
		DBTree dbTree = treeService.addTree(
				new FSTree("Sample tree","Sample tree description."),
				new FSTestNode("A","Node A"));
		
		assertNotNull(dbTree);
		assertNotNull(dbTree.getRootNode());
		
		logger.info("Tree created. root note id => " + dbTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		DBNode nodeB = treeService.createChildNode(dbTree.getRootNode(), new FSTestNode("B","Node B"));
			DBNode nodeC = treeService.createChildNode(nodeB, new FSTestNode("C","Node C"));
				DBNode nodeD = treeService.createChildNode(nodeC, new FSTestNode("D","Node D"));
					DBNode nodeE = treeService.createChildNode(nodeD, new FSTestNode("E","Node E"));
						DBNode nodeF = treeService.createChildNode(nodeE, new FSTestNode("F","Node F"));
						DBNode nodeG = treeService.createChildNode(nodeE, new FSTestNode("G","Node G"));
					DBNode nodeH = treeService.createChildNode(nodeD, new FSTestNode("H","Node H"));
						DBNode nodeI = treeService.createChildNode(nodeH, new FSTestNode("I","Node I"));
						DBNode nodeJ = treeService.createChildNode(nodeH, new FSTestNode("J","Node J"));
					DBNode nodeK = treeService.createChildNode(nodeD, new FSTestNode("K","Node K"));
						DBNode nodeL = treeService.createChildNode(nodeK, new FSTestNode("L","Node L"));
						DBNode nodeM = treeService.createChildNode(nodeK, new FSTestNode("M","Node M"));
						DBNode nodeN = treeService.createChildNode(nodeK, new FSTestNode("N","Node N"));
						DBNode nodeO = treeService.createChildNode(nodeK, new FSTestNode("O","Node O"));
		
		logger.info("Finished adding nodes to tree...");
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Tree before...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Delete node N");
		treeService.removeNode(nodeN);
		
		logger.info("Tree after...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		
	}
	
	/**
	 * Build sample tree, print before, delete non leaf node, then print after.
	 */
	public void deleteNonLeafNode() throws ServiceException {
		
		logTestTitle("Delete non-leaf node test");
		
		TreeService treeService = getTreeSerive();
		
		logger.info("Creating sample tree");
		
		DBTree dbTree = treeService.addTree(
				new FSTree("Sample tree","Sample tree description."),
				new FSTestNode("A","Node A"));
		
		assertNotNull(dbTree);
		assertNotNull(dbTree.getRootNode());
		
		logger.info("Tree created. root note id => " + dbTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		DBNode nodeB = treeService.createChildNode(dbTree.getRootNode(), new FSTestNode("B","Node B"));
			DBNode nodeC = treeService.createChildNode(nodeB, new FSTestNode("C","Node C"));
				DBNode nodeD = treeService.createChildNode(nodeC, new FSTestNode("D","Node D"));
					DBNode nodeE = treeService.createChildNode(nodeD, new FSTestNode("E","Node E"));
						DBNode nodeF = treeService.createChildNode(nodeE, new FSTestNode("F","Node F"));
						DBNode nodeG = treeService.createChildNode(nodeE, new FSTestNode("G","Node G"));
					DBNode nodeH = treeService.createChildNode(nodeD, new FSTestNode("H","Node H"));
						DBNode nodeI = treeService.createChildNode(nodeH, new FSTestNode("I","Node I"));
						DBNode nodeJ = treeService.createChildNode(nodeH, new FSTestNode("J","Node J"));
					DBNode nodeK = treeService.createChildNode(nodeD, new FSTestNode("K","Node K"));
						DBNode nodeL = treeService.createChildNode(nodeK, new FSTestNode("L","Node L"));
						DBNode nodeM = treeService.createChildNode(nodeK, new FSTestNode("M","Node M"));
						DBNode nodeN = treeService.createChildNode(nodeK, new FSTestNode("N","Node N"));
						DBNode nodeO = treeService.createChildNode(nodeK, new FSTestNode("O","Node O"));
		
		logger.info("Finished adding nodes to tree...");
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Tree before...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Delete node K");
		treeService.removeNode(nodeK);
		
		logger.info("Tree after...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		
	}	

}
