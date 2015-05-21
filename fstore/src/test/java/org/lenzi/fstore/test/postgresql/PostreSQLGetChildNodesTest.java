package org.lenzi.fstore.test.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.AbstractAddNodeTest;
import org.lenzi.fstore.test.AbstractGetChildNodesTest;
import org.lenzi.fstore.test.AbstractGetParentNodeTest;
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
@ContextConfiguration(classes=PostgreSQLTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("postgresql")
@ActiveProfiles({"postgresql"})
public class PostreSQLGetChildNodesTest extends AbstractGetChildNodesTest {
	
	@Autowired
	private PostgreSQLTestConfiguration configuration = null;
	
	public PostreSQLGetChildNodesTest() {

	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}

}