package org.lenzi.fstore.test.postgresql;


import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.repository.model.DBClosure;
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
public class PostreSQLAddNodeTest extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	PostgreSQLTestConfiguration configuration = null;
	
	@Autowired
	TreeService<FSTestNode> treeService = null;
	
	@Autowired
	ClosureLogger<FSTestNode> closureLogger;
	
	public PostreSQLAddNodeTest() {

	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		assertNotNull(treeService);
		
	}
	
	@Test
	@Rollback(true)
	public void addRootNodeTest() throws ServiceException {
		
		logTestTitle("Add root node test");
		
		FSTestNode rootNode = treeService.createRootNode( new FSTestNode("Sample root node 1","Sample root test value 1"));
		assertNotNull(rootNode);
		
		List<DBClosure<FSTestNode>> closureList = treeService.getClosure(rootNode);
		assertNotNull(closureList);
		
		logger.info("Closure:");
		closureLogger.logClosure(closureList);
		
		Tree<TreeMeta> tree = treeService.buildTreeOld(closureList);
		assertNotNull(tree);
		
		logger.info("Tree:");
		logger.info(tree.printTree());	
		
	}
	
	@Test
	@Rollback(true)
	public void addNodeTreeTest() throws ServiceException {

		logTestTitle("Add node tree");
		
		FSTestNode rootNode = treeService.createRootNode(new FSTestNode("Sample root node","Sample root test value"));
			assertNotNull(rootNode);
			
			FSTestNode childNode1 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 1","Sample child test value 1"));
			assertNotNull(childNode1);
			
			FSTestNode childNode2 = treeService.createChildNode(rootNode, new FSTestNode("Sample child node 2","Sample child test value 2"));
			assertNotNull(childNode2);
			
				FSTestNode childNode3 = treeService.createChildNode(childNode2, new FSTestNode("Sample child node 3","Sample child test value 3"));
				assertNotNull(childNode3);
				
		List<DBClosure<FSTestNode>> closureList = treeService.getClosure(rootNode);
		assertNotNull(closureList);
		
		logger.info("Closure:");
		closureLogger.logClosure(closureList);
		
		Tree<TreeMeta> tree = treeService.buildTreeOld(closureList);
		assertNotNull(tree);
		
		logger.info("Tree:");
		logger.info(tree.printTree());
		
	}

}
