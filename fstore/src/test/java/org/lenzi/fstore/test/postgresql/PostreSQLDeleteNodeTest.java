package org.lenzi.fstore.test.postgresql;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.DBTree;
import org.lenzi.fstore.repository.model.impl.FSTree;
import org.lenzi.fstore.service.TreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostgreSQL unit test, controlled by the ActiveProfiles annotation. Uses the PostgresClosureRepository.
 * 
 * @author slenzi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=PostgreSQLTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("postgresql")
@ActiveProfiles({"postgresql"})
public class PostreSQLDeleteNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	PostgreSQLTestConfiguration configuration = null;
	
	@Autowired
	TreeService<FSTestNode> treeService = null;
	
	@Autowired
	ClosureLogger<FSTestNode> closureLogger;
	
	public PostreSQLDeleteNodeTest() {
		
	}	
	
	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		assertNotNull(treeService);
		
	}
	
	@Test
	@Rollback(false)	
	public void deleteLeafNodeTest() throws ServiceException {

		logTestTitle("Delete leaf node test");
		
		logger.info("Creating sample tree");
		
		DBTree<FSTestNode> dbTree = treeService.addTree(
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
		
		Tree<FSTestNode> treeMeta = null;
		
		logger.info("Tree before...");
		treeMeta = treeService.buildTree(dbTree.getRootNode());
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Delete node N");
		treeService.removeNode(nodeN);
		
		logger.info("Tree after...");
		treeMeta = treeService.buildTree(dbTree.getRootNode());
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
	}
	
	@Test
	@Rollback(false)	
	public void deleteNonLeafNodeTest() throws ServiceException {

		logTestTitle("Delete non-leaf node test");
		
		logger.info("Creating sample tree");
		
		DBTree<FSTestNode> dbTree = treeService.addTree(
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
		
		Tree<FSTestNode> treeMeta = null;
		
		logger.info("Tree before...");
		treeMeta = treeService.buildTree(dbTree.getRootNode());
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Delete node K");
		treeService.removeNode(nodeK);
		
		logger.info("Tree after...");
		treeMeta = treeService.buildTree(dbTree.getRootNode());
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
	}

}
