/**
 * 
 */
package org.lenzi.fstore.test.cms.postgresql;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.cms.AbstractBulkAddFile;
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
public class PostgreSQLBulkAddFileTest extends AbstractBulkAddFile {

	@Autowired
	private PostgresqlCmsTestConfiguration configuration = null;	
	
	/**
	 * 
	 */
	public PostgreSQLBulkAddFileTest() {

	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.test.cms.AbstractAddFile#getTestFileStorePath()
	 */
	@Override
	public String getTestFileStorePath() {

		return "/Users/slenzi/Programming/file_store/sample_upload_bulk";
		
	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}	

}
