package org.lenzi.fstore.test.postgresql;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.util.FSTestNodeCopier;
import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.service.TreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractCopyNodeTest;
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
public class PostreSQLCopyNodeTest extends AbstractCopyNodeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	PostgreSQLTestConfiguration configuration = null;
	
	@Autowired
	TreeService treeService = null;
	
	@Autowired
	ClosureLogger closureLogger;		
	
	@Autowired
	FSTestNodeCopier testNodeCopier = null;
	
	public PostreSQLCopyNodeTest() {
		
	}
	
	@Override
	public TreeService getTreeSerive() {
		return treeService;
	}
	
	@Override
	public ClosureLogger getClosureLogger() {
		return closureLogger;
	}	
	
	@Override
	public NodeCopier getNodeCopier() {
		return testNodeCopier;
	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		assertNotNull(treeService);
		
	}
	
	@Test
	@Rollback(true)	
	public void copyNodeButNodeChildrenTest(){
		try {
			copyNodeWithoutChildren();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}		
	
	@Test
	@Rollback(true)	
	public void copyNodeWithChildrenTest(){
		try {
			copyNodeWithChildren();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}
	
	@Test
	@Rollback(true)	
	public void copyNodeDifferentTreeTest(){
		try {
			copyNodeDifferentTree();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}
	
	@Test
	@Rollback(true)	
	public void multiCopySameTreeTest(){
		try {
			multiCopySameTree();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}	

}
