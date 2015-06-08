package org.lenzi.fstore.test.file;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.service.FsService;
import org.lenzi.fstore.file.service.exception.FsServiceException;
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
	private FsService storeService;
	
	public AbstractGetCmsDirectory() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void getCmsDirectoryById() {
		
		logTestTitle("Fetching directory by id");
		
		FsDirectory cmsDirectory = null;
		try {
			cmsDirectory = storeService.getFsDirectoryById(1L, FsDirectoryFetch.FILE_META);
		} catch (FsServiceException e) {
			logger.error("Failed to fetch directory. " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Have directory => " + ((cmsDirectory != null) ? true : false));
		
		assertNotNull(cmsDirectory);
		
		logger.info("");
		logger.info("Fetched directory:");
		logger.info(cmsDirectory.toString());
		logger.info("");
	
	}
	
}
