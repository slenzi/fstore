package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

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

	@Test
	@Rollback(false)
	public void getFileStore() {
		
		Path examplePath = Paths.get(getTestFileStorePath());
		
		logTestTitle("Creating sample file store at => " + examplePath.toString());
		
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test fetch operation.", false);
		} catch (DatabaseException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Have file store => " + ((fileStore != null) ? true : false));
		logger.info("Have file store root dir => " + ((fileStore.hasRootDir()) ? true : false));
		
		assertNotNull(fileStore);
		assertNotNull(fileStore.getRootDir());
		
		Long storeId = fileStore.getStoreId();
		Long rootDirId = fileStore.getRootDir().getNodeId();
		
		logger.info("Created new file store:");
		logger.info(fileStore.toString());
		
		/*
		logger.info("Fetching newly created store...");
		
		fileStore = null;
		try {
			fileStore = fileStoreRepository.getCmsStoreByStoreId(storeId);
		} catch (DatabaseException e) {
			logger.error("Failed to fetch file store by store id. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(fileStore);
		assertNotNull(fileStore.getRootDir());
		
		logger.info("Fetched file store by store id:");
		logger.info(fileStore.toString());
		
		fileStore = null;
		try {
			fileStore = fileStoreRepository.getCmsStoreByRootDirId(rootDirId);
		} catch (DatabaseException e) {
			logger.error("Failed to fetch file store by root dir id. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(fileStore);
		assertNotNull(fileStore.getRootDir());
		
		logger.info("Fetched file store by root dir id:");
		logger.info(fileStore.toString());
		*/
	}
	
	public abstract String getTestFileStorePath();
	
}
