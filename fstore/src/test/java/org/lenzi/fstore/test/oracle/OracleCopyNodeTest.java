package org.lenzi.fstore.test.oracle;


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
 * Oracle unit test, controlled by the ActiveProfiles annotation. Uses the OracleClosureRepository.
 * 
 * @author slenzi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=OracleTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleCopyNodeTest extends AbstractCopyNodeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	OracleTestConfiguration configuration = null;
	
	@Autowired
	TreeService treeService = null;
	
	@Autowired
	FSTestNodeCopier testNodeCopier = null;
	
	@Autowired
	ClosureLogger closureLogger;
	
	public OracleCopyNodeTest() {
		
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
	public void copyNodeWithoutChildrenTest(){
		try {
			copyNodeWithoutChildren();
		} catch (ServiceException e) {
			logger.error(e.getMessage());
		}
	}	
	
	@Test
	@Rollback(false)	
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
