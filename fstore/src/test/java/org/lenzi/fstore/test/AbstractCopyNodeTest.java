package org.lenzi.fstore.test;


import static org.junit.Assert.assertNotNull;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.DBTree;
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
public abstract class AbstractCopyNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractCopyNodeTest() {
		
	}
	
	public abstract NodeCopier getNodeCopier();
	
	/**
	 * Build sample tree, copy a node to under a different node in the same tree, excluding children.
	 * Print tree before and after.
	 */
	public void copyNodeWithoutChildren() throws ServiceException {
		
		logTestTitle("Copy node without children test");
		
		TreeService treeService = getTreeSerive();
		NodeCopier copier = getNodeCopier();
		
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
			
		Tree<TreeMeta> treeMeta = treeService.buildTree(dbTree);	
		assertNotNull(treeMeta);
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (excluding children)...");
		DBNode copyE = treeService.copyNode(nodeE, nodeM, false, copier);
		
		logger.info("After copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		
	}	
	
	/**
	 * Build sample tree, copy a node to under a different node in the same tree, including children.
	 * Print tree before and after.
	 */
	public void copyNodeWithChildren() throws ServiceException {
		
		logTestTitle("Copy node with children test");
		
		TreeService treeService = getTreeSerive();
		NodeCopier copier = getNodeCopier();
		
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
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (inlcuding children)...");
		
		DBNode copyE = treeService.copyNode(nodeE, nodeM, true, copier);
		
		logger.info("After copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
	}
	
	/**
	 * Build two trees, copy node from one tree to the other.
	 * print both trees before and after copy.
	 */
	public void copyNodeDifferentTree() throws ServiceException {
		
		logTestTitle("Copy node test: different tree");
		
		TreeService treeService = getTreeSerive();
		NodeCopier copier = getNodeCopier();
		
		logger.info("Creating sample tree 1");
		
		DBTree dbTree1 = treeService.addTree(
				new FSTree("Sample tree 1","Sample tree description 1."),
				new FSTestNode("A1","Node A1"));
		
		assertNotNull(dbTree1);
		assertNotNull(dbTree1.getRootNode());
		
		logger.info("Tree 1 created. root note id => " + dbTree1.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree 1...");
		
		DBNode nodeB1 = treeService.createChildNode(dbTree1.getRootNode(), new FSTestNode("B1","Node B1"));
			DBNode nodeC1 = treeService.createChildNode(nodeB1, new FSTestNode("C1","Node C1"));
				DBNode nodeD1 = treeService.createChildNode(nodeC1, new FSTestNode("D1","Node D1"));
					DBNode nodeE1 = treeService.createChildNode(nodeD1, new FSTestNode("E1","Node E1"));
						DBNode nodeF1 = treeService.createChildNode(nodeE1, new FSTestNode("F1","Node F1"));
						DBNode nodeG1 = treeService.createChildNode(nodeE1, new FSTestNode("G1","Node G1"));
					DBNode nodeH1 = treeService.createChildNode(nodeD1, new FSTestNode("H1","Node H1"));
						DBNode nodeI1 = treeService.createChildNode(nodeH1, new FSTestNode("I1","Node I1"));
						DBNode nodeJ1 = treeService.createChildNode(nodeH1, new FSTestNode("J1","Node J1"));
					DBNode nodeK1 = treeService.createChildNode(nodeD1, new FSTestNode("K1","Node K1"));
						DBNode nodeL1 = treeService.createChildNode(nodeK1, new FSTestNode("L1","Node L1"));
						DBNode nodeM1 = treeService.createChildNode(nodeK1, new FSTestNode("M1","Node M1"));
						DBNode nodeN1 = treeService.createChildNode(nodeK1, new FSTestNode("N1","Node N1"));
						DBNode nodeO1 = treeService.createChildNode(nodeK1, new FSTestNode("O1","Node O1"));
		
		logger.info("Finished adding nodes to tree 1...");
		
		logger.info("Creating sample tree 1");
		
		DBTree dbTree2 = treeService.addTree(
				new FSTree("Sample tree 2","Sample tree description 2."),
				new FSTestNode("A2","Node A2"));
		
		assertNotNull(dbTree2);
		assertNotNull(dbTree2.getRootNode());
		
		logger.info("Tree 2 created. root note id => " + dbTree2.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree 2...");
		
		DBNode nodeB2 = treeService.createChildNode(dbTree2.getRootNode(), new FSTestNode("B2","Node B2"));
			DBNode nodeC2 = treeService.createChildNode(nodeB2, new FSTestNode("C2","Node C2"));
				DBNode nodeD2 = treeService.createChildNode(nodeC2, new FSTestNode("D2","Node D2"));
					DBNode nodeE2 = treeService.createChildNode(nodeD2, new FSTestNode("E2","Node E2"));
						DBNode nodeF2 = treeService.createChildNode(nodeE2, new FSTestNode("F2","Node F2"));
						DBNode nodeG2 = treeService.createChildNode(nodeE2, new FSTestNode("G2","Node G2"));
					DBNode nodeH2 = treeService.createChildNode(nodeD2, new FSTestNode("H2","Node H2"));
						DBNode nodeI2 = treeService.createChildNode(nodeH2, new FSTestNode("I2","Node I2"));
						DBNode nodeJ2 = treeService.createChildNode(nodeH2, new FSTestNode("J2","Node J2"));
					DBNode nodeK2 = treeService.createChildNode(nodeD2, new FSTestNode("K2","Node K2"));
						DBNode nodeL2 = treeService.createChildNode(nodeK2, new FSTestNode("L2","Node L2"));
						DBNode nodeM2 = treeService.createChildNode(nodeK2, new FSTestNode("M2","Node M2"));
						DBNode nodeN2 = treeService.createChildNode(nodeK2, new FSTestNode("N2","Node N2"));
						DBNode nodeO2 = treeService.createChildNode(nodeK2, new FSTestNode("O2","Node O2"));
		
		logger.info("Finished adding nodes to tree 1...");		
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Tree 1 before...");
		treeMeta = treeService.buildTree(dbTree1);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Tree 2 before...");
		treeMeta = treeService.buildTree(dbTree2);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		
		logger.info("Copy node D1 from tree 1 to under node N2 on tree 2, inlcuding all children...");
		DBNode copyD1 = treeService.copyNode(nodeD1, nodeN2, true, copier);
		
		logger.info("Tree 1 After...");
		treeMeta = treeService.buildTree(dbTree1);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Tree 2 After...");
		treeMeta = treeService.buildTree(dbTree2);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
	}
	
	/**
	 * Build sample tree, copy a node to under a different node in the same tree.
	 * Print tree before and after.
	 */
	public void multiCopySameTree() throws ServiceException {
		
		logTestTitle("Multi-copy test: same tree");
		
		TreeService treeService = getTreeSerive();
		NodeCopier copier = getNodeCopier();
		
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
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (inlcuding children)...");
		DBNode copyE = treeService.copyNode(nodeE, nodeM, true, copier);
		
		logger.info("After copy E to M...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());			
		
		logger.info("Copying node H to node 0 (inlcuding children)...");
		DBNode copyH = treeService.copyNode(nodeH, nodeO, true, copier);
		
		logger.info("After copy H to O...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());			
		
		logger.info("Copying node D to newly created copy of H (inlcuding children)...");
		DBNode copyD = treeService.copyNode(nodeD, copyH, true, copier);
		
		logger.info("After copy D to newly create copy of H...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());	
		
	}

}
