/**
 * 
 */
package org.lenzi.fstore.test.cms.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.cms.AbstractCreateFileStore;
import org.lenzi.fstore.test.cms.setup.PostgresqlCmsTestConfiguration;
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
public class PostgreSQLCreateFileStoreTest extends AbstractCreateFileStore {

	@Autowired
	private PostgresqlCmsTestConfiguration configuration = null;	
	
	/**
	 * 
	 */
	public PostgreSQLCreateFileStoreTest() {

	}
	
	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}	

}
