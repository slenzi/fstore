package org.lenzi.fstore.test.core.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.core.AbstractPrintTreeTest;
import org.lenzi.fstore.test.core.setup.PostgresqlCoreTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes=PostgresqlCoreTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("postgresql")
@ActiveProfiles({"postgresql"})
public class PostreSQLPrintTreeTest extends AbstractPrintTreeTest {
	
	@Autowired
	private PostgresqlCoreTestConfiguration configuration = null;
	
	public PostreSQLPrintTreeTest() {

	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}

}
