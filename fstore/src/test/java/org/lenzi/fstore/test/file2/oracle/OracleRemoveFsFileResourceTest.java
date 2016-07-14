/**
 * 
 */
package org.lenzi.fstore.test.file2.oracle;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.file2.setup.oracle.TestFileConfigOracle;
import org.lenzi.fstore.test.file2.AbstractRemoveFsFileResource;
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
@ContextConfiguration(classes=TestFileConfigOracle.class, loader=AnnotationConfigContextLoader.class)
@Transactional("oracle")
@ActiveProfiles({"oracle"})
public class OracleRemoveFsFileResourceTest extends AbstractRemoveFsFileResource {

	@Autowired
	private TestFileConfigOracle configuration = null;	
	
	/**
	 * 
	 */
	public OracleRemoveFsFileResourceTest() {

	}
	
	/* (non-Javadoc)
	 * @see org.lenzi.fstore.test.cms.AbstractCreateFileStore#getTestFileStorePath()
	 */
	@Override
	public String getTestStorePath() {
		
		//return "C:/temp/file_store/sample_resource_store_remove_file";
		return getProperty("test.store.test_remove_file");
		
	}

	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}	

}
