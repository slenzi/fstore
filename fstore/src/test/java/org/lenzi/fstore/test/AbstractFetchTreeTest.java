package org.lenzi.fstore.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.repository.model.impl.FSTree;
import org.lenzi.fstore.service.TreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

public abstract class AbstractFetchTreeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TreeService<FSTestNode> treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger;		
	
	public AbstractFetchTreeTest() {
		
	}
	
	@Test
	@Rollback(false)	
	public void fetchTree() throws ServiceException {

		logTestTitle("Fetch tree test");
		
		logger.info("Creating sample tree");
		
		FSTree<FSTestNode> dbTree = treeService.addTree(
				new FSTree<FSTestNode>("Sample tree","Sample tree description."),
				new FSTestNode("A","Node A"));
		
		assertNotNull(dbTree);
		assertNotNull(dbTree.getRootNode());
		
		logger.info("Tree created. root note id => " + dbTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		FSTestNode nodeB = treeService.createChildNode(dbTree.getRootNode(), new FSTestNode("B","Node B"));
			FSTestNode nodeC = treeService.createChildNode(nodeB, new FSTestNode("C","Node C"));
				FSTestNode nodeD = treeService.createChildNode(nodeC, new FSTestNode("D","Node D"));
					FSTestNode nodeE = treeService.createChildNode(nodeD, new FSTestNode("E","Node E"));
						FSTestNode nodeF = treeService.createChildNode(nodeE, new FSTestNode("F","Node F"));
						FSTestNode nodeG = treeService.createChildNode(nodeE, new FSTestNode("G","Node G"));
					FSTestNode nodeH = treeService.createChildNode(nodeD, new FSTestNode("H","Node H"));
						FSTestNode nodeI = treeService.createChildNode(nodeH, new FSTestNode("I","Node I"));
						FSTestNode nodeJ = treeService.createChildNode(nodeH, new FSTestNode("J","Node J"));
					FSTestNode nodeK = treeService.createChildNode(nodeD, new FSTestNode("K","Node K"));
						FSTestNode nodeL = treeService.createChildNode(nodeK, new FSTestNode("L","Node L"));
						FSTestNode nodeM = treeService.createChildNode(nodeK, new FSTestNode("M","Node M"));
						FSTestNode nodeN = treeService.createChildNode(nodeK, new FSTestNode("N","Node N"));
						FSTestNode nodeO = treeService.createChildNode(nodeK, new FSTestNode("O","Node O"));
		
		logger.info("Finished adding nodes to tree...");
		
		logger.info("Tree :");
		Tree<FSTestNode> tree = treeService.buildTree(dbTree.getRootNode());
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Now, fetch the tree...");
		
		FSTree<FSTestNode> fetchedTree = null;
		fetchedTree = treeService.geTreeById(dbTree);
		assertNotNull(fetchedTree);
		assertNotNull(fetchedTree.getTreeId());
		assertNotNull(fetchedTree.getRootNode());
		
		logger.info("Fetched tree " + fetchedTree.getTreeId());
		logger.info("Root node => " + fetchedTree.getRootNode().toString());

		logger.info("Fetched tree :");
		Tree<FSTestNode> ftree = treeService.buildTree(fetchedTree.getRootNode());
		assertNotNull(ftree);
		logger.info(ftree.printTree());
		
	}
	
	@Test
	@Rollback(false)	
	public void fetchTreeWithNodeType() throws ServiceException {

		logTestTitle("Fetch tree with node type test");
		
		logger.info("Creating sample tree");
		
		FSTree<FSTestNode> dbTree = treeService.addTree(
				new FSTree<FSTestNode>("Sample tree","Sample tree description."),
				new FSTestNode("A","Node A"));
		
		assertNotNull(dbTree);
		assertNotNull(dbTree.getRootNode());
		
		logger.info("Tree created. root note id => " + dbTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		FSTestNode nodeB = treeService.createChildNode(dbTree.getRootNode(), new FSTestNode("B","Node B"));
			FSTestNode nodeC = treeService.createChildNode(nodeB, new FSTestNode("C","Node C"));
				FSTestNode nodeD = treeService.createChildNode(nodeC, new FSTestNode("D","Node D"));
					FSTestNode nodeE = treeService.createChildNode(nodeD, new FSTestNode("E","Node E"));
						FSTestNode nodeF = treeService.createChildNode(nodeE, new FSTestNode("F","Node F"));
						FSTestNode nodeG = treeService.createChildNode(nodeE, new FSTestNode("G","Node G"));
					FSTestNode nodeH = treeService.createChildNode(nodeD, new FSTestNode("H","Node H"));
						FSTestNode nodeI = treeService.createChildNode(nodeH, new FSTestNode("I","Node I"));
						FSTestNode nodeJ = treeService.createChildNode(nodeH, new FSTestNode("J","Node J"));
					FSTestNode nodeK = treeService.createChildNode(nodeD, new FSTestNode("K","Node K"));
						FSTestNode nodeL = treeService.createChildNode(nodeK, new FSTestNode("L","Node L"));
						FSTestNode nodeM = treeService.createChildNode(nodeK, new FSTestNode("M","Node M"));
						FSTestNode nodeN = treeService.createChildNode(nodeK, new FSTestNode("N","Node N"));
						FSTestNode nodeO = treeService.createChildNode(nodeK, new FSTestNode("O","Node O"));
		
		logger.info("Finished adding nodes to tree...");
		
		logger.info("Tree :");
		Tree<FSTestNode> tree = treeService.buildTree(dbTree.getRootNode());
		assertNotNull(tree);
		logger.info(tree.printTree());
		
		logger.info("Now, fetch the tree...");
		
		FSTree<FSTestNode> fetchedTree = null;
		fetchedTree = treeService.geTreeById(dbTree, FSTestNode.class);
		assertNotNull(fetchedTree);
		assertNotNull(fetchedTree.getTreeId());
		assertNotNull(fetchedTree.getRootNode());
		
		logger.info("Fetched tree " + fetchedTree.getTreeId());
		logger.info("Root node => " + fetchedTree.getRootNode().toString());

		logger.info("Fetched tree :");
		Tree<FSTestNode> ftree = treeService.buildTree(fetchedTree.getRootNode());
		assertNotNull(ftree);
		logger.info(ftree.printTree());
		
	}	

}
