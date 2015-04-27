package org.lenzi.fstore.test;

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
public abstract class AbstractPrintTreeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractPrintTreeTest() {

	}
	
	/**
	 * Build sample tree and log.
	 */
	public void printTree() throws ServiceException {
		
		logTestTitle("Print tree test");
		
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
						FSNode nodeR = treeService.createNode(nodeK,"R");
							FSNode nodeS = treeService.createNode(nodeR,"S");
								FSNode nodeW = treeService.createNode(nodeS,"W");
									FSNode nodeX = treeService.createNode(nodeW,"X");
										FSNode nodeY = treeService.createNode(nodeX,"Y");
											FSNode nodeZ = treeService.createNode(nodeY,"Z");
												FSNode node0 = treeService.createNode(nodeZ,"0");
													FSNode node1 = treeService.createNode(node0,"1");
												FSNode node2 = treeService.createNode(nodeZ,"2");
											FSNode node3 = treeService.createNode(nodeY,"3");
										FSNode node4 = treeService.createNode(nodeX,"4");
									FSNode node5 = treeService.createNode(nodeW,"5");
								FSNode nodeT = treeService.createNode(nodeS,"T");
									FSNode nodeU = treeService.createNode(nodeT,"U");
										FSNode nodeV = treeService.createNode(nodeU,"V");
		FSNode nodeM = treeService.createNode(fsTree.getRootNode(), "M");
			FSNode nodeN = treeService.createNode(nodeM, "N");
				FSNode nodeO = treeService.createNode(nodeN, "O");
					FSNode nodeP = treeService.createNode(nodeO, "P");
						FSNode nodeQ = treeService.createNode(nodeP, "Q");
		
		logger.info("Finished adding nodes to tree...");
						
		Tree<TreeMeta> tree = treeService.buildTree(fsTree);
		
		assertNotNull(tree);
	
		logger.info(tree.printTree());
		
	}

}
