package org.lenzi.fstore.test.core.oracle;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.core.AbstractAddNodeTest;
import org.lenzi.fstore.test.core.AbstractGetParentNodeTest;
import org.lenzi.fstore.test.core.AbstractGetRootNodeTest;
import org.lenzi.fstore.test.core.setup.OracleCoreTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes=OracleCoreTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleGetRootNodeTest extends AbstractGetRootNodeTest {
	
	@Autowired
	private OracleCoreTestConfiguration configuration = null;
	
	public OracleGetRootNodeTest() {

	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}	

}
