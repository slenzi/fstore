package org.lenzi.fstore.test.postgresql;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.repository.model.impl.FSTree;
import org.lenzi.fstore.service.FSTreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractDeleteTreeTest;
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
public class PostreSQLDeleteTreeTest extends AbstractDeleteTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	PostgreSQLTestConfiguration configuration = null;
	
	@Autowired
	FSTreeService treeService = null;
	
	public PostreSQLDeleteTreeTest() {

	}
	
	@Override
	public FSTreeService getTreeSerive() {
		return treeService;
	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		assertNotNull(treeService);
		
	}
	
	@Test
	@Rollback(false)
	public void deleteTreeTest() throws Exception{
		
		logTestTitle("Delete Tree Test");
		
		FSTree treeToDelete = null;
		try {
			treeToDelete = createTreeForDeletion();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			return;
		}
		
		assertNotNull(treeToDelete);
		
		Long deleteId = treeToDelete.getTreeId();
		try {
			deleteTree(treeToDelete);
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			return;
		}
		
		FSTree deletedTree = null;
		try {
			deletedTree = getTree(deleteId);
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			return;
		}
		
		assertNull(deletedTree);
		
	}

}
