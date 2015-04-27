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
		
		FSTestNode sampleNode = new FSTestNode();
		sampleNode.setName("Sample node 1");
		sampleNode.setTestValue("Sample test value 1");
		
		treeService.createRootNode(sampleNode);
		
	}

}
