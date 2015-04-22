package org.lenzi.fstore.test.oracle;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.service.FSTreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.test.AbstractMoveNodeTest;
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
public class OracleMoveNodeTest extends AbstractMoveNodeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	OracleTestConfiguration configuration = null;
	
	@Autowired
	FSTreeService treeService = null;
	
	public OracleMoveNodeTest() {
		
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
	@Rollback(true)	
	public void moveNodeSameTreeTest(){
		try {
			moveNodeSameTree();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}
	
	@Test
	@Rollback(true)	
	public void moveNodeDifferentTreeTest(){
		try {
			moveNodeDifferentTree();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}
	
	@Test
	@Rollback(true)	
	public void multiMoveSameTreeTest(){
		try {
			multiMoveSameTree();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}	
		

}
