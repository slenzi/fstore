package org.lenzi.fstore.test;


import static org.junit.Assert.assertNotNull;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.model.util.FSTestNodeCopier;
import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.repository.model.DbNode;
import org.lenzi.fstore.repository.model.DbTree;
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
		
		FSTree tree = new FSTree();
		tree.setName("Sample tree");
		tree.setDescription("Sample tree description.");
		
		DbTree dbTree = treeService.addTree(tree, new FSTestNode("A","Node A"));
		
		assertNotNull(dbTree);
		assertNotNull(dbTree.getRootNode());
		
		logger.info("Tree created. root note id => " + dbTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		DbNode nodeB = treeService.createChildNode(dbTree.getRootNode(), new FSTestNode("B","Node B"));
			DbNode nodeC = treeService.createChildNode(nodeB, new FSTestNode("C","Node C"));
				DbNode nodeD = treeService.createChildNode(nodeC, new FSTestNode("D","Node D"));
					DbNode nodeE = treeService.createChildNode(nodeD, new FSTestNode("E","Node E"));
						DbNode nodeF = treeService.createChildNode(nodeE, new FSTestNode("F","Node F"));
						DbNode nodeG = treeService.createChildNode(nodeE, new FSTestNode("G","Node G"));
					DbNode nodeH = treeService.createChildNode(nodeD, new FSTestNode("H","Node H"));
						DbNode nodeI = treeService.createChildNode(nodeH, new FSTestNode("I","Node I"));
						DbNode nodeJ = treeService.createChildNode(nodeH, new FSTestNode("I","Node J"));
					DbNode nodeK = treeService.createChildNode(nodeD, new FSTestNode("K","Node K"));
						DbNode nodeL = treeService.createChildNode(nodeK, new FSTestNode("L","Node L"));
						DbNode nodeM = treeService.createChildNode(nodeK, new FSTestNode("M","Node M"));
						DbNode nodeN = treeService.createChildNode(nodeK, new FSTestNode("N","Node N"));
						DbNode nodeO = treeService.createChildNode(nodeK, new FSTestNode("O","Node O"));
		
		logger.info("Finished adding nodes to tree...");
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (excluding children)...");
		DbNode copyE = treeService.copyNode(nodeE, nodeM, false, copier);
		
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
		
		FSTree tree = new FSTree();
		tree.setName("Sample tree");
		tree.setDescription("Sample tree description.");
		
		DbTree dbTree = treeService.addTree(tree, new FSTestNode("A","Node A"));
		
		assertNotNull(dbTree);
		assertNotNull(dbTree.getRootNode());
		
		logger.info("Tree created. root note id => " + dbTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		DbNode nodeB = treeService.createChildNode(dbTree.getRootNode(), new FSTestNode("B","Node B"));
			DbNode nodeC = treeService.createChildNode(nodeB, new FSTestNode("C","Node C"));
				DbNode nodeD = treeService.createChildNode(nodeC, new FSTestNode("D","Node D"));
					DbNode nodeE = treeService.createChildNode(nodeD, new FSTestNode("E","Node E"));
						DbNode nodeF = treeService.createChildNode(nodeE, new FSTestNode("F","Node F"));
						DbNode nodeG = treeService.createChildNode(nodeE, new FSTestNode("G","Node G"));
					DbNode nodeH = treeService.createChildNode(nodeD, new FSTestNode("H","Node H"));
						DbNode nodeI = treeService.createChildNode(nodeH, new FSTestNode("I","Node I"));
						DbNode nodeJ = treeService.createChildNode(nodeH, new FSTestNode("I","Node J"));
					DbNode nodeK = treeService.createChildNode(nodeD, new FSTestNode("K","Node K"));
						DbNode nodeL = treeService.createChildNode(nodeK, new FSTestNode("L","Node L"));
						DbNode nodeM = treeService.createChildNode(nodeK, new FSTestNode("M","Node M"));
						DbNode nodeN = treeService.createChildNode(nodeK, new FSTestNode("N","Node N"));
						DbNode nodeO = treeService.createChildNode(nodeK, new FSTestNode("O","Node O"));
		
		logger.info("Finished adding nodes to tree...");
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (excluding children)...");
		
		DbNode copyE = treeService.copyNode(nodeE, nodeM, true, copier);
		
		logger.info("After copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
	}
	
	/**
	 * Build two trees, copy node from one tree to the other.
	 * print both trees before and after move.
	 */
	public void copyNodeDifferentTree() throws ServiceException {
		/*
		logTestTitle("Copy node test: different tree");
		
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
		
		logger.info("Tree 1 before");
		tree = treeService.buildTree(fsTree1);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		
		logger.info("Tree 2 before");
		tree = treeService.buildTree(fsTree2);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		
		logger.info("Copying node E1 from tree 1 to under node G2 on tree 2...");
		treeService.copyNode(nodeE1, nodeG2, true);
		
		logger.info("Tree 1 after");
		tree = treeService.buildTree(fsTree1);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		
		logger.info("Tree 2 after");
		tree = treeService.buildTree(fsTree2);
		assertNotNull(tree);
		logger.info(tree.printTree());	
		
		logger.info("Done.");
		*/
	}
	
	/**
	 * Build sample tree, copy a node to under a different node in the same tree.
	 * Print tree before and after.
	 */
	public void multiCopySameTree() throws ServiceException {
		/*
		logTestTitle("Multi-copy test: same tree");
		
		FSTreeService treeService = getTreeSerive();
		
		logger.info("Creating sample tree");
		FSTree fsTree = treeService.createTree("Sample Tree", "A sample test tree", "A");
		
		assertNotNull(fsTree);
		assertNotNull(fsTree.getRootNode());
		
		logger.info("Tree created. root note id => " + fsTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		FSNode nodeB = treeService.createNode(fsTree.getRootNode(), "B");
			FSNode nodeD = treeService.createNode(nodeB,"D");
				FSNode nodeK = treeService.createNode(nodeD,"K");
					FSNode nodeL = treeService.createNode(nodeK,"L");
						FSNode nodeM = treeService.createNode(nodeL,"M");
							FSNode nodeN = treeService.createNode(nodeM,"N");
								FSNode nodeO = treeService.createNode(nodeN,"O");
									FSNode nodeP = treeService.createNode(nodeO,"P");
			FSNode nodeE = treeService.createNode(nodeB,"E");
		FSNode nodeC = treeService.createNode(fsTree.getRootNode(), "C");
			FSNode nodeF = treeService.createNode(nodeC,"F");
				FSNode nodeH = treeService.createNode(nodeF,"H");
					FSNode nodeI = treeService.createNode(nodeH,"I");
					FSNode nodeJ = treeService.createNode(nodeH,"J");
			FSNode nodeG = treeService.createNode(nodeC,"G");
				FSNode nodeQ = treeService.createNode(nodeG,"Q");
					FSNode nodeR = treeService.createNode(nodeQ,"R");
						FSNode nodeS = treeService.createNode(nodeR,"S");
							FSNode nodeT = treeService.createNode(nodeS,"T");
								FSNode nodeU = treeService.createNode(nodeT,"U");
									FSNode nodeV = treeService.createNode(nodeU,"V");
		
		logger.info("Finished adding nodes to tree...");
		
		Tree<TreeMeta> tree = null;
		
		logger.info("Before copies...");
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Copy C to N");
		treeService.copyNode(nodeC, nodeN, true);
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Copy G to E");
		treeService.copyNode(nodeG, nodeE, true);
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
	
		logger.info("Done.");
		*/
	}

}
