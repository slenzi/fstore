/**
 * 
 */
package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.cms.repository.FileStoreRepository;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
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
public abstract class AbstractAddChildCmsDirectory extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private  FileStoreRepository fileStoreRepository;
	
	public AbstractAddChildCmsDirectory() {
		
	}
	
	@Test
	@Rollback(false)
	public void addChildDirectory() {
		
		Path examplePath = Paths.get(getTestFileStorePath());
		
		logTestTitle("Adding child directory test");
		
		//
		// create file store
		//
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test adding child directories.", false);
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
		// add child directory
		//
		final String subDirName = "sub_test_1";
		CmsDirectory subTest1 = null;
		try {
			subTest1 = fileStoreRepository.addDirectory(fileStore.getRootDir().getNodeId(), subDirName);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(subTest1);
		
		logger.info("Created sub dir:");
		logger.info(subTest1.toString());
		
		//
		// test get path method on new sub directory
		//
		String subDirPath = null;
		try {
			subDirPath = fileStoreRepository.getPath(subTest1.getNodeId());
		} catch (DatabaseException e) {
			logger.error("Failed to get full path for root dir of newly created file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(subDirPath);
		
		logger.info("Sub dir full path => " + subDirPath);
		
		String expectedPath = getTestFileStorePath() + File.separator + subDirName;
		
		assertEquals(Paths.get(subDirPath).toString(), Paths.get(expectedPath).toString());
		
	}
	
	public abstract String getTestFileStorePath();

}
