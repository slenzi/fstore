package org.lenzi.fstore.test;

import static org.junit.Assert.assertNotNull;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.Node;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.repository.model.impl.FSTestNode;
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
public abstract class AbstractAddNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public AbstractAddNodeTest() {

	}
	
	/**
	 * Add a root node
	 */
	public void addRootNode() throws ServiceException {
		
		logTestTitle("Add root node test");
		
		FSTreeService treeService = getTreeSerive();
		
		FSTestNode rootNode = new FSTestNode();
		rootNode.setName("Sample root node 1");
		rootNode.setTestValue("Sample root test value 1");
		
		treeService.createRootNode(rootNode);
		
	}
	
	/**
	 * Add node tree.
	 */
	public void addNodeTree() throws ServiceException {
		
		logTestTitle("Add node tree");
		
		FSTreeService treeService = getTreeSerive();
		
		FSTestNode rootNode = new FSTestNode();
		rootNode.setName("Sample root node");
		rootNode.setTestValue("Sample root test value");
		
		treeService.createRootNode(rootNode);
		
		FSTestNode childNode1 = new FSTestNode();
		childNode1.setName("Sample child node 1");
		childNode1.setTestValue("Sample child test value 1");
		
		treeService.createChildNode(rootNode, childNode1);
		
		FSTestNode childNode2 = new FSTestNode();
		childNode2.setName("Sample child node 2");
		childNode2.setTestValue("Sample child test value 2");
		
		treeService.createChildNode(rootNode, childNode2);
		
		FSTestNode childNode3 = new FSTestNode();
		childNode3.setName("Sample child node 3");
		childNode3.setTestValue("Sample child test value 3");
		
		treeService.createChildNode(childNode2, childNode3);			
		
	}	

}
