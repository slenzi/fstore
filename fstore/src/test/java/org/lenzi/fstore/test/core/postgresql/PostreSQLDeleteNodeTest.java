package org.lenzi.fstore.test.core.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.core.AbstractDeleteNodeTest;
import org.lenzi.fstore.test.core.setup.postgresql.TestCoreConfigPostgres;
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
@ContextConfiguration(classes=TestCoreConfigPostgres.class, loader=AnnotationConfigContextLoader.class)
//@ContextConfiguration(classes=PostgresqlCoreTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("postgresql")
@ActiveProfiles({"postgresql"})
public class PostreSQLDeleteNodeTest extends AbstractDeleteNodeTest {
	
	@Autowired
	//private PostgresqlCoreTestConfiguration configuration = null;
	private TestCoreConfigPostgres configuration = null;
	
	public PostreSQLDeleteNodeTest() {
		
	}	
	
	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}

}
