package org.lenzi.fstore.test.oracle;


import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.repository.model.impl.FSTree;
import org.lenzi.fstore.service.TreeService;
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
 * Oracle unit test, controlled by the ActiveProfiles annotation. Uses the OracleClosureRepository.
 * 
 * @author slenzi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=OracleTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleDeleteTreeTest extends AbstractDeleteTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	OracleTestConfiguration configuration = null;
	
	@Autowired
	TreeService treeService = null;
	
	@Autowired
	ClosureLogger closureLogger;	
	
	public OracleDeleteTreeTest() {

	}
	
	@Override
	public TreeService getTreeSerive() {
		return treeService;
	}
	
	@Override
	public ClosureLogger getClosureLogger() {
		return closureLogger;
	}	

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		assertNotNull(treeService);
		
	}
	
	@Test
	@Rollback(false)
	public void deleteTreeTest() {
		/*
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
		*/
	}

}
