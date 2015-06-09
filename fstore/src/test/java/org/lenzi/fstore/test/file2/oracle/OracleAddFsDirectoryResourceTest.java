/**
 * 
 */
package org.lenzi.fstore.test.file2.oracle;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.file2.setup.OracleCmsTestConfiguration;
import org.lenzi.fstore.test.file2.AbstractAddFsDirectoryResource;
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
@ContextConfiguration(classes=OracleCmsTestConfiguration.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleAddFsDirectoryResourceTest extends AbstractAddFsDirectoryResource {

	@Autowired
	private OracleCmsTestConfiguration configuration = null;	
	
	/**
	 * 
	 */
	public OracleAddFsDirectoryResourceTest() {

	}
	
	/* (non-Javadoc)
	 * @see org.lenzi.fstore.test.cms.AbstractCreateFileStore#getTestFileStorePath()
	 */
	@Override
	public String getTestStorePath() {
		
		return "C:/temp/file_store/sample_resource_store_add_dir";
		
	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}	

}
