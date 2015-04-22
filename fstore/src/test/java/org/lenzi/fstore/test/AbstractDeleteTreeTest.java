package org.lenzi.fstore.test;

import static org.junit.Assert.assertNotNull;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.FSNode;
import org.lenzi.fstore.repository.model.FSTree;
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
public abstract class AbstractDeleteTreeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractDeleteTreeTest() {

	}
	
	/**
	 * Create a tree that can be used to test the delete process.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public FSTree createTreeForDeletion() throws ServiceException {
		
		logger.info("Creating sample tree for testing delete process.");
		
		FSTreeService treeService = getTreeSerive();
		
		FSTree fsTree = treeService.createTree("Sample Delete Tree", "A sample tree to test deleting", "A");
		
		assertNotNull(fsTree);
		assertNotNull(fsTree.getRootNode());
		
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
						
		Tree<TreeMeta> tree = treeService.buildTree(fsTree);
		
		assertNotNull(tree);
	
		logger.info(tree.printTree());
		
		return fsTree;
		
	}
	
	/**
	 * Delete a tree. Tree should exists in database.
	 * 
	 * @param tree
	 * @throws ServiceException
	 */
	public void deleteTree(FSTree tree) throws ServiceException {
		
		logger.info("Deleting tree with id => " + tree.getTreeId());
		
		FSTreeService treeService = getTreeSerive();
		
		treeService.removeTree(tree);
		
	}
	
	/**
	 * Fetch a tree
	 * 
	 * @param treeId
	 * @return
	 * @throws ServiceException
	 */
	public FSTree getTree(Long treeId) throws ServiceException {
		
		logger.info("Getting tree with id => " + treeId);
		
		FSTreeService treeService = getTreeSerive();
		
		return treeService.getTree(treeId);
		
	}

}
