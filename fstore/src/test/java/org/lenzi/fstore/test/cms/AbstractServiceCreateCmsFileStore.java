/**
 * 
 */
package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.CmsFileStoreService;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Uses the CmsFileStoreService to create a sample file store.
 * 
 * @author sal
 */
public abstract class AbstractServiceCreateCmsFileStore extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private CmsFileStoreService storeService;
	
	public AbstractServiceCreateCmsFileStore() {
		
	}
	
	@Test
	@Rollback(false)
	public void createFileStore() {
		
		Path examplePath = Paths.get(getTestFileStorePath());
		
		logTestTitle("Creating sample file store at => " + examplePath.toString());
		
		CmsFileStore store = null;
		
		try {
			
			//store = storeService.createSampleFileStore(examplePath);
			store = storeService.createSampleFileStoreAlt(examplePath);
			
		} catch (CmsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
		assertNotNull(store);
		
	}
	
	public abstract String getTestFileStorePath();

}
