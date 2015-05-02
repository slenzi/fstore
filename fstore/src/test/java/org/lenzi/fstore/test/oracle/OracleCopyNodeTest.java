package org.lenzi.fstore.test.oracle;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.model.util.FSTestNodeCopier;
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
 * Oracle unit test, controlled by the ActiveProfiles annotation. Uses the OracleClosureRepository.
 * 
 * @author slenzi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=OracleTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleCopyNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	OracleTestConfiguration configuration = null;
	
	@Autowired
	TreeService<FSTestNode> treeService = null;
	
	@Autowired
	FSTestNodeCopier testNodeCopier = null;
	
	@Autowired
	ClosureLogger<FSTestNode> closureLogger;
	
	public OracleCopyNodeTest() {
		
	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		assertNotNull(treeService);
		
	}
	
	@Test
	@Rollback(true)	
	public void copyNodeWithoutChildrenTest() throws ServiceException {

		/*
		logTestTitle("Copy node without children test");
		
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
			
		Tree<TreeMeta> treeMeta = treeService.buildTree(dbTree);	
		assertNotNull(treeMeta);
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (excluding children)...");
		FSTestNode copyE = treeService.copyNode(nodeE, nodeM, false, testNodeCopier);
		
		logger.info("After copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		*/
		
	}	
	
	@Test
	@Rollback(false)	
	public void copyNodeWithChildrenTest() throws ServiceException {

		logTestTitle("Copy node with children test");
		
		logger.info("Creating sample tree");
		
		DBTree<FSTestNode> dbTree = treeService.addTree(
				new FSTree<FSTestNode>("Sample tree","Sample tree description."),
				new FSTestNode("A","Node A"));
		
		assertNotNull(dbTree);
		assertNotNull(dbTree.getRootNode());
		
		logger.info("Tree created. root note id => " + dbTree.getRootNode().getNodeId());
		
		logger.info("Adding additional nodes to tree...");
		
		// TODO - Think about creating FSTestTree so you don't have to cast
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
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (including children)...");
		
		FSTestNode copyE = treeService.copyNode(nodeE, nodeM, true, testNodeCopier);
		
		logger.info("After copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
	}
	
	@Test
	@Rollback(true)	
	public void copyNodeDifferentTreeTest() throws ServiceException {
		/*
		logTestTitle("Copy node test: different tree");
		
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
		
		logger.info("Tree 1 before...");
		treeMeta = treeService.buildTree(dbTree1);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Tree 2 before...");
		treeMeta = treeService.buildTree(dbTree2);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());		
		
		logger.info("Copy node D1 from tree 1 to under node N2 on tree 2, including all children...");
		FSTestNode copyD1 = treeService.copyNode(nodeD1, nodeN2, true, testNodeCopier);
		
		logger.info("Tree 1 After...");
		treeMeta = treeService.buildTree(dbTree1);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Tree 2 After...");
		treeMeta = treeService.buildTree(dbTree2);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		*/
	}
	
	@Test
	@Rollback(true)	
	public void multiCopySameTreeTest() throws ServiceException {
		/*
		logTestTitle("Multi-copy test: same tree");
		
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
		
		Tree<TreeMeta> treeMeta = null;
		
		logger.info("Before copy...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());
		
		logger.info("Copying node E to node M (including children)...");
		FSTestNode copyE = treeService.copyNode(nodeE, nodeM, true, testNodeCopier);
		
		logger.info("After copy E to M...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());			
		
		logger.info("Copying node H to node 0 (including children)...");
		FSTestNode copyH = treeService.copyNode(nodeH, nodeO, true, testNodeCopier);
		
		logger.info("After copy H to O...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());			
		
		logger.info("Copying node D to newly created copy of H (including children)...");
		FSTestNode copyD = treeService.copyNode(nodeD, copyH, true, testNodeCopier);
		
		logger.info("After copy D to newly create copy of H...");
		treeMeta = treeService.buildTree(dbTree);
		assertNotNull(treeMeta);
		logger.info(treeMeta.printTree());	
		*/
	}	
		
}
