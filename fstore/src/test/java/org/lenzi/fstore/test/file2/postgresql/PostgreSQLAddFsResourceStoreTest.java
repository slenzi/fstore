/**
 * 
 */
package org.lenzi.fstore.test.file2.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.file2.AbstractAddFsResourceStore;
import org.lenzi.fstore.test.file2.setup.PostgresqlCmsTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=PostgresqlCmsTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("postgresql")
@ActiveProfiles({"postgresql"})
public class PostgreSQLAddFsResourceStoreTest extends AbstractAddFsResourceStore {

	@Autowired
	private PostgresqlCmsTestConfiguration configuration = null;	
	
	/**
	 * 
	 */
	public PostgreSQLAddFsResourceStoreTest() {

	}
	
	/* (non-Javadoc)
	 * @see org.lenzi.fstore.test.cms.AbstractCreateFileStore#getTestFileStorePath()
	 */
	@Override
	public String getTestStorePath() {
		
		return "/Users/slenzi/Programming/file_store/sample_resource_store";
		
	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}	

}
