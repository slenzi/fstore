package org.lenzi.fstore.test;

import static org.junit.Assert.assertNotNull;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
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
 * Print tree test
 * 
 * @author sal
 */
public abstract class AbstractPrintTreeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractPrintTreeTest() {

	}
	
	/**
	 * Build sample tree and log.
	 */
	public void printTree() throws ServiceException {
		
		logTestTitle("Print tree test");
		
		TreeService treeService = getTreeSerive();
		
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
						
		Tree<TreeMeta> treeMeta = treeService.buildTree(dbTree);
		
		assertNotNull(treeMeta);
	
		logger.info(treeMeta.printTree());

	}

}
