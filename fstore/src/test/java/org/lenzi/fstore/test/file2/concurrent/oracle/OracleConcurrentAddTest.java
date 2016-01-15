package org.lenzi.fstore.test.file2.concurrent.oracle;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.file2.concurrent.AbstractConcurrentAdd;
import org.lenzi.fstore.test.file2.setup.OracleCmsTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=OracleCmsTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleConcurrentAddTest extends AbstractConcurrentAdd {

	@Autowired
	private OracleCmsTestConfiguration configuration = null;	
	
	public OracleConcurrentAddTest() {
		
	}
	
	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}

	@Override
	public String getTestStorePath() {
		
		//return "C:/temp/file_store/concurrent/sample_store";
		return getProperty("test.store.test_concurrent_add");
		
	}

}
