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
public class PostreSQLMoveNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	PostgreSQLTestConfiguration configuration = null;
	
	@Autowired
	TreeService<FSTestNode> treeService = null;
	
	@Autowired
	ClosureLogger<FSTestNode> closureLogger;	
	
	public PostreSQLMoveNodeTest() {
		
	}	
	
	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		assertNotNull(treeService);
		
	}
	
	@Test
	@Rollback(true)	
	public void moveNodeSameTreeTest() throws ServiceException {
		/*
		logTestTitle("Move node test: same tree");
		
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
		
		logger.info("Node 0 test value => " + nodeO.getTestValue());
		
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
		*/
	}
	
	@Test
	@Rollback(true)	
	public void moveNodeDifferentTreeTest() throws ServiceException {
		/*
		logTestTitle("Move node test: different tree");
		
		logger.info("Creating sample tree 1");
		
		DBTree<FSTestNode> dbTree1 = treeService.addTree(
				new FSTree<FSTestNode>("Sample tree 1","Sample tree description 1."),
				new FSTestNode("A1","Node A1"));
		
		assertNotNull(dbTree1);
		assertNotNull(dbTree1.getRootNode());
		
		logger.info("Tree 1 created. root note id => " + dbTree1.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree 1...");
		
		FSTestNode nodeB1 = treeService.createChildNode(dbTree1.getRootNode(), new FSTestNode("B1","Node B1"));
			FSTestNode nodeC1 = treeService.createChildNode(nodeB1, new FSTestNode("C1","Node C1"));
				FSTestNode nodeD1 = treeService.createChildNode(nodeC1, new FSTestNode("D1","Node D1"));
					FSTestNode nodeE1 = treeService.createChildNode(nodeD1, new FSTestNode("E1","Node E1"));
						FSTestNode nodeF1 = treeService.createChildNode(nodeE1, new FSTestNode("F1","Node F1"));
						FSTestNode nodeG1 = treeService.createChildNode(nodeE1, new FSTestNode("G1","Node G1"));
					FSTestNode nodeH1 = treeService.createChildNode(nodeD1, new FSTestNode("H1","Node H1"));
						FSTestNode nodeI1 = treeService.createChildNode(nodeH1, new FSTestNode("I1","Node I1"));
						FSTestNode nodeJ1 = treeService.createChildNode(nodeH1, new FSTestNode("J1","Node J1"));
					FSTestNode nodeK1 = treeService.createChildNode(nodeD1, new FSTestNode("K1","Node K1"));
						FSTestNode nodeL1 = treeService.createChildNode(nodeK1, new FSTestNode("L1","Node L1"));
						FSTestNode nodeM1 = treeService.createChildNode(nodeK1, new FSTestNode("M1","Node M1"));
						FSTestNode nodeN1 = treeService.createChildNode(nodeK1, new FSTestNode("N1","Node N1"));
						FSTestNode nodeO1 = treeService.createChildNode(nodeK1, new FSTestNode("O1","Node O1"));
		
		logger.info("Finished adding nodes to tree 1...");
		
		logger.info("Creating sample tree 1");
		
		DBTree<FSTestNode> dbTree2 = treeService.addTree(
				new FSTree<FSTestNode>("Sample tree 2","Sample tree description 2."),
				new FSTestNode("A2","Node A2"));
		
		assertNotNull(dbTree2);
		assertNotNull(dbTree2.getRootNode());
		
		logger.info("Tree 2 created. root note id => " + dbTree2.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree 2...");
		
		FSTestNode nodeB2 = treeService.createChildNode(dbTree2.getRootNode(), new FSTestNode("B2","Node B2"));
			FSTestNode nodeC2 = treeService.createChildNode(nodeB2, new FSTestNode("C2","Node C2"));
				FSTestNode nodeD2 = treeService.createChildNode(nodeC2, new FSTestNode("D2","Node D2"));
					FSTestNode nodeE2 = treeService.createChildNode(nodeD2, new FSTestNode("E2","Node E2"));
						FSTestNode nodeF2 = treeService.createChildNode(nodeE2, new FSTestNode("F2","Node F2"));
						FSTestNode nodeG2 = treeService.createChildNode(nodeE2, new FSTestNode("G2","Node G2"));
					FSTestNode nodeH2 = treeService.createChildNode(nodeD2, new FSTestNode("H2","Node H2"));
						FSTestNode nodeI2 = treeService.createChildNode(nodeH2, new FSTestNode("I2","Node I2"));
						FSTestNode nodeJ2 = treeService.createChildNode(nodeH2, new FSTestNode("J2","Node J2"));
					FSTestNode nodeK2 = treeService.createChildNode(nodeD2, new FSTestNode("K2","Node K2"));
						FSTestNode nodeL2 = treeService.createChildNode(nodeK2, new FSTestNode("L2","Node L2"));
						FSTestNode nodeM2 = treeService.createChildNode(nodeK2, new FSTestNode("M2","Node M2"));
						FSTestNode nodeN2 = treeService.createChildNode(nodeK2, new FSTestNode("N2","Node N2"));
						FSTestNode nodeO2 = treeService.createChildNode(nodeK2, new FSTestNode("O2","Node O2"));
		
		logger.info("Finished adding nodes to tree 1...");
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Tree 1 before move...");
		treeMeta = treeService.buildTree(dbTree1);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Tree 2 before move...");
		treeMeta = treeService.buildTree(dbTree2);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving node D1 from tree 1 to under node K2 on tree 2");
		treeService.moveNode(nodeD1, nodeK2);
		
		logger.info("Tree 1 after move...");
		treeMeta = treeService.buildTree(dbTree1);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Tree 2 after move...");
		treeMeta = treeService.buildTree(dbTree2);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		*/
	}
	
	@Test
	@Rollback(false)	
	public void multiMoveSameTreeTest() throws ServiceException {
		
		logTestTitle("Multi-move test: same tree");
		
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
		
		logger.info("Node 0 test value => " + nodeO.getTestValue());
		
		Tree<TreeMeta> treeMeta = null;
		
		//
		// move operation returns an updated entity. Use that in future moves.
		//
		
		logger.info("Tree before move...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving F to A");
		FSTestNode updatedF = treeService.moveNode(nodeF, (FSTestNode)dbTree.getRootNode());
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving E to A");
		FSTestNode updatedE = treeService.moveNode(nodeE, (FSTestNode)dbTree.getRootNode());
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving C to A");
		FSTestNode updatedC = treeService.moveNode(nodeC, (FSTestNode)dbTree.getRootNode());
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving H to C");
		FSTestNode updatedH = treeService.moveNode(nodeH, updatedC);
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving B to I");
		FSTestNode updatedB = treeService.moveNode(nodeB, nodeI);
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving F to B");
		updatedF = treeService.moveNode(updatedF, updatedB);
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Moving E to F");
		updatedE = treeService.moveNode(updatedE, updatedF);
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
	
		logger.info("Done.");
	}		

}
