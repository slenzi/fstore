/**
 * 
 */
package org.lenzi.fstore.test.file.oracle;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lenzi.fstore.test.file.AbstractRemoveFsDirectory;
import org.lenzi.fstore.test.file.setup.OracleCmsTestConfiguration;
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
public class OracleRemoveFsDirectoryTest extends AbstractRemoveFsDirectory {

	@Autowired
	private OracleCmsTestConfiguration configuration = null;	
	
	/**
	 * 
	 */
	public OracleRemoveFsDirectoryTest() {

	}


	
	/* (non-Javadoc)
	 * @see org.lenzi.fstore.test.cms.AbstractAddFile#getTestFileStorePath()
	 */
	@Override
	public String getTestFileStorePath() {
		
		return "C:/temp/file_store/sample_remove_directory";
		
	}



	@Test
	public void testWiring(){
		
		assertNotNull(configuration);
		
	}	

}