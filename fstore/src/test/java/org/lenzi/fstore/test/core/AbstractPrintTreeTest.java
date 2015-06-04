package org.lenzi.fstore.test.core;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.core.logging.ClosureLogger;
import org.lenzi.fstore.core.repository.model.impl.FSTree;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.core.tree.Trees.WalkOption;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

public abstract class AbstractPrintTreeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	protected TestTreeService treeService = null;
	
	@Autowired
	protected ClosureLogger<FSTestNode> closureLogger;		
	
	public AbstractPrintTreeTest() {
		
	}
	
	@Test
	@Rollback(false)	
	public void printTree() throws ServiceException {

		logTestTitle("Print tree test");
		
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
		
		
		List<TreeNode<FSTestNode>> preList = tree.toList(WalkOption.PRE_ORDER_TRAVERSAL);
		List<TreeNode<FSTestNode>> postList = tree.toList(WalkOption.POST_ORDER_TRAVERSAL);
		
		logger.info("Pre-order node list");
		for(TreeNode<FSTestNode> tn : preList){
			logger.info(tn.getData().toString());
		}
		
		logger.info("Post-order node list");
		for(TreeNode<FSTestNode> tn : postList){
			logger.info(tn.getData().toString());
		}		

		
	}	

}
