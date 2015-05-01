package org.lenzi.fstore.test;


import static org.junit.Assert.assertNotNull;

import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.DBTree;
import org.lenzi.fstore.repository.model.impl.FSNode;
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
public abstract class AbstractMoveNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractMoveNodeTest() {
		
	}
	
	/**
	 * Build sample tree, move a node to under a different node in the same tree.
	 * Print tree before and after.
	 */
	public void moveNodeSameTree() throws ServiceException {
		
		logTestTitle("Move node test: same tree");
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
		
		FSTestNode testNode = (FSTestNode)nodeO;
		logger.info("Node 0 test value => " + testNode.getTestValue());
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Tree before move...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Move node K to under node G...");
		treeService.moveNode(nodeK, nodeG);
		
		logger.info("Tree after move...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		
	}
	
	/**
	 * Build two trees, move node from one tree to the other.
	 * print both trees before and after move.
	 */
	public void moveNodeDifferentTree() throws ServiceException {
		
		logTestTitle("Move node test: different tree");
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
		
		logger.info("Tree 1 before");
		tree = treeService.buildTree(fsTree1);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		
		logger.info("Tree 2 before");
		tree = treeService.buildTree(fsTree2);
		assertNotNull(tree);
		logger.info(tree.printTree());		
		
		logger.info("Moving node E1 from tree 1 to under node G2 on tree 2...");
		treeService.moveNode(nodeE1, nodeG2);
		
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
	 * Build sample tree, move a node to under a different node in the same tree.
	 * Print tree before and after.
	 */
	public void multiMoveSameTree() throws ServiceException {
		
		logTestTitle("Multi-move test: same tree");
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
			FSNode nodeE = treeService.createNode(nodeB,"E");
		FSNode nodeC = treeService.createNode(fsTree.getRootNode(), "C");
			FSNode nodeF = treeService.createNode(nodeC,"F");
				FSNode nodeH = treeService.createNode(nodeF,"H");
					FSNode nodeI = treeService.createNode(nodeH,"I");
					FSNode nodeJ = treeService.createNode(nodeH,"J");
			FSNode nodeG = treeService.createNode(nodeC,"G");		
		
		logger.info("Finished adding nodes to tree...");
		
		Tree<TreeMeta> tree = null;
		
		logger.info("Before moves...");
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving C to E");
		treeService.moveNode(nodeC, nodeE);
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving F to A");
		treeService.moveNode(nodeF, fsTree.getRootNode());
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving E to A");
		treeService.moveNode(nodeE, fsTree.getRootNode());
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving C to A");
		treeService.moveNode(nodeC, fsTree.getRootNode());
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving H to C");
		treeService.moveNode(nodeH, nodeC);
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving B to I");
		treeService.moveNode(nodeB, nodeI);
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving F to B");
		treeService.moveNode(nodeF, nodeB);
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Moving E to F");
		treeService.moveNode(nodeE, nodeF);
		tree = treeService.buildTree(fsTree);
		assertNotNull(tree);
		logger.info(tree.printTree());
	
		logger.info("Done.");
		*/
	}	

}
