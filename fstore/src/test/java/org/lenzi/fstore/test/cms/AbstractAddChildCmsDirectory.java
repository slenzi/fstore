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
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.CmsFileStoreService;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
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
	private CmsFileStoreService storeService;
	
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
			fileStore = storeService.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test adding child directories.", false);
		} catch (CmsServiceException e) {
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
		// add child directory 1
		//
		final String subDirName1 = "sub_test_1";
		CmsDirectory subTest1 = null;
		try {
			subTest1 = storeService.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (CmsServiceException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(subTest1);
		
		logger.info("Created sub dir 1:");
		logger.info(subTest1.toString());
		
		//
		// validate sub dir 1 path
		//
		Path subDirPath1 = null;
		try {
			subDirPath1 = storeService.getAbsoluteDirectoryPath(subTest1.getDirId());
		} catch (CmsServiceException e) {
			logger.error("Failed to get full path for root dir of newly created file store. " + e.getMessage());
			e.printStackTrace();
		}
		assertNotNull(subDirPath1);
		logger.info("Sub dir full path 1 => " + subDirPath1.toString());
		String expectedPath1 = getTestFileStorePath() + File.separator + subDirName1;
		assertEquals(subDirPath1.toString(), Paths.get(expectedPath1).toString());		
		
		//
		// add child directory 2
		//
		final String subDirName2 = "sub_test_2";
		CmsDirectory subTest2 = null;
		try {
			subTest2 = storeService.addDirectory(subTest1.getNodeId(), subDirName2);
		} catch (CmsServiceException e) {
			logger.error("Failed to add child directory to dir => " + subTest1.getDirId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(subTest2);
		
		logger.info("Created sub dir 2:");
		logger.info(subTest2.toString());
		
		//
		// validate sub dir 2 path
		//
		Path subDirPath2 = null;
		try {
			subDirPath2 = storeService.getAbsoluteDirectoryPath(subTest2.getDirId());
		} catch (CmsServiceException e) {
			logger.error("Failed to get full path for root dir of newly created file store. " + e.getMessage());
			e.printStackTrace();
		}
		assertNotNull(subDirPath2);
		logger.info("Sub dir full path 2 => " + subDirPath2.toString());
		String expectedPath2 = getTestFileStorePath() + File.separator + subDirName1 + File.separator + subDirName2;
		assertEquals(subDirPath2.toString(), Paths.get(expectedPath2).toString());		
		

		
	}
	
	public abstract String getTestFileStorePath();

}
