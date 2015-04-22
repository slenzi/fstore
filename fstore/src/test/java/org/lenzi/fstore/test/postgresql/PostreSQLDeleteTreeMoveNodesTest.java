package org.lenzi.fstore.test.postgresql;


import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.service.FSTreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractDeleteTreeMoveNodesTest;
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
public class PostreSQLDeleteTreeMoveNodesTest extends AbstractDeleteTreeMoveNodesTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	PostgreSQLTestConfiguration configuration = null;
	
	@Autowired
	FSTreeService treeService = null;
	
	public PostreSQLDeleteTreeMoveNodesTest() {

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
	public void doTest() {
		
		logTestTitle("Delete Tree but Move Nodes Test");
		
		try {
			deleteTreeButMoveNodes();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			return;
		}

		
	}

}
