package org.lenzi.fstore.test.core.oracle;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.core.AbstractDeleteTreeTest;
import org.lenzi.fstore.test.core.setup.oracle.TestCoreConfigOracle;
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
@ContextConfiguration(classes=TestCoreConfigOracle.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleDeleteTreeTest extends AbstractDeleteTreeTest {
	
	@Autowired
	private TestCoreConfigOracle configuration = null;
	
	public OracleDeleteTreeTest() {

	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}

}
