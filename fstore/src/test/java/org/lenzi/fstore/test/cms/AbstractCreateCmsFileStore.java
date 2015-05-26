/**
 * 
 */
package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertEquals;
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
 * @author sal
 */
public abstract class AbstractCreateCmsFileStore extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private  FileStoreRepository fileStoreRepository;
	
	public AbstractCreateCmsFileStore() {
		
	}
	
	@Test
	@Rollback(false)
	public void createFileStore() {
		
		Path examplePath = Paths.get(getTestFileStorePath());
		
		logTestTitle("Creating sample file store at => " + examplePath.toString());
		
		//
		// create file store
		//
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test create operation.", false);
		} catch (DatabaseException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Have file store => " + ((fileStore != null) ? true : false));
		logger.info("Have root dir => " + ((fileStore != null) ? fileStore.hasRootDir() : false));		
		
		assertNotNull(fileStore);
		assertNotNull(fileStore.getRootDir());
		
		logger.info("");
		logger.info("Newly created file store:");
		logger.info(fileStore.toString());
		logger.info("");
		
		//
		// test get path method on root dir of file store
		//
		String rootDirPath = null;
		try {
			rootDirPath = fileStoreRepository.getPath(fileStore.getNodeId());
		} catch (DatabaseException e) {
			logger.error("Failed to get full path for root dir of newly created file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(rootDirPath);
		
		logger.info("Root dir full path => " + rootDirPath);
		
		String expectedPath = getTestFileStorePath();
		
		assertEquals(Paths.get(rootDirPath).toString(), Paths.get(expectedPath).toString());
		
	}
	
	public abstract String getTestFileStorePath();

}
