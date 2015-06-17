package org.lenzi.fstore.test.file2.concurrent.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.file2.concurrent.AbstractConcurrentAdd;
import org.lenzi.fstore.test.file2.setup.PostgresqlCmsTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=PostgresqlCmsTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("postgresql")
@ActiveProfiles({"postgresql"})
public class PostgreSQLConcurrentAddTest extends AbstractConcurrentAdd {

	@Autowired
	private PostgresqlCmsTestConfiguration configuration = null;	
	
	public PostgreSQLConcurrentAddTest() {
		
	}
	
	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}

	@Override
	public String getTestStorePath() {
		
		return "/Users/slenzi/Programming/file_store/concurrent/sample_store";
		
	}

}
