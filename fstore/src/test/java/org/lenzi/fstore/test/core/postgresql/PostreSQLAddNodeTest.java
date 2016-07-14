package org.lenzi.fstore.test.core.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.core.AbstractAddNodeTest;
import org.lenzi.fstore.test.core.setup.TestConfigPostgres;
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
@ContextConfiguration(classes=TestConfigPostgres.class, loader=AnnotationConfigContextLoader.class)
//@ContextConfiguration(classes=PostgresqlCoreTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("postgresql")
@ActiveProfiles({"postgresql"})
public class PostreSQLAddNodeTest extends AbstractAddNodeTest {
	
	@Autowired
	//private PostgresqlCoreTestConfiguration configuration = null;
	private TestConfigPostgres configuration = null;
	
	public PostreSQLAddNodeTest() {

	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}

}
