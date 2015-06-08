package org.lenzi.fstore.test.file;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
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
public abstract class AbstractGetFsFileStore extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsService storeService;
	
	public AbstractGetFsFileStore() {
		
	}

	/**
	 * fetch file store with store ID = 1
	 */
	@Test
	@Rollback(false)
	public void getFileStoreByStoreId() {
		
		logTestTitle("Fetching file store by store id");
		
		FsFileStore fileStore = null;
		try {
			fileStore = storeService.getFsStoreByStoreId(1L);
		} catch (FsServiceException e) {
			logger.error("Failed to fetch file store by store id. " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Have file store => " + ((fileStore != null) ? true : false));
		logger.info("Have root dir => " + ((fileStore != null) ? fileStore.hasRootDir() : false));
		
		assertNotNull(fileStore);
		assertNotNull(fileStore.getRootDir());
		
		logger.info("");
		logger.info("Fetched file store by store id:");
		logger.info(fileStore.toString());
		logger.info("");
	
	}
	
	/**
	 * fetch file store with root dir ID = 1
	 */
	@Test
	@Rollback(false)
	public void getFileStoreByRootDirId() {
		
		logTestTitle("Fetching file store by root dir id");
		
		FsFileStore fileStore = null;
		try {
			fileStore = storeService.getFsStoreByRootDirId(1L);
		} catch (FsServiceException e) {
			logger.error("Failed to fetch file store by root dir id. " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Have file store => " + ((fileStore != null) ? true : false));
		logger.info("Have root dir => " + ((fileStore != null) ? fileStore.hasRootDir() : false));
		
		assertNotNull(fileStore);
		assertNotNull(fileStore.getRootDir());
		
		logger.info("");
		logger.info("\nFetched file store by root dir id:");
		logger.info(fileStore.toString());
		logger.info("");
		
	}
	
	public abstract String getTestFileStorePath();
	
}
