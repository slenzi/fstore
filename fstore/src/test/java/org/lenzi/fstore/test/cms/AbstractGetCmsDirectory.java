package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.service.CmsFileStoreService;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * 
 * @author slenzi
 *
 */
public abstract class AbstractGetCmsDirectory extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private CmsFileStoreService storeService;
	
	public AbstractGetCmsDirectory() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void getCmsDirectoryById() {
		
		logTestTitle("Fetching cms directory by id");
		
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = storeService.getCmsDirectoryById(1L, CmsDirectoryFetch.FILE_META);
		} catch (CmsServiceException e) {
			logger.error("Failed to fetch cms directory. " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Have cms directory => " + ((cmsDirectory != null) ? true : false));
		
		assertNotNull(cmsDirectory);
		
		logger.info("");
		logger.info("Fetched cms directory:");
		logger.info(cmsDirectory.toString());
		logger.info("");
	
	}
	
}
