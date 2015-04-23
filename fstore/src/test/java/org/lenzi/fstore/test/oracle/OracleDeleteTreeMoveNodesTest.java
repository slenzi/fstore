package org.lenzi.fstore.test.oracle;


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
 * Oracle unit test, controlled by the ActiveProfiles annotation. Uses the OracleClosureRepository.
 * 
 * @author slenzi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=OracleTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleDeleteTreeMoveNodesTest extends AbstractDeleteTreeMoveNodesTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	OracleTestConfiguration configuration = null;
	
	@Autowired
	FSTreeService treeService = null;
	
	public OracleDeleteTreeMoveNodesTest() {

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
