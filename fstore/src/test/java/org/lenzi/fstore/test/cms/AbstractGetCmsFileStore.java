package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lenzi.fstore.cms.repository.FileStoreRepository;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.repository.exception.DatabaseException;
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
public abstract class AbstractGetCmsFileStore extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private  FileStoreRepository fileStoreRepository;
	
	public AbstractGetCmsFileStore() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * fetch file store with store ID = 1
	 */
	@Test
	@Rollback(false)
	public void getFileStoreByStoreId() {
		
		logTestTitle("Fetching file store by store id");
		
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.getCmsStoreByStoreId(1L);
		} catch (DatabaseException e) {
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
		
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.getCmsStoreByRootDirId(1L);
		} catch (DatabaseException e) {
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
