package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.CmsFileStoreService;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.Rollback;

/**
 * 
 * @author slenzi
 *
 */
public abstract class AbstractMoveFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private CmsFileStoreService storeService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractMoveFile() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doMoveFile() throws Exception {
		
		logTestTitle("Move file test");
		
		assertNotNull(resourceLoader);
		
		// get test file for upload to database
		Resource sourceResource1 = resourceLoader.getResource("classpath:image/honey_badger.JPG");
		Resource sourceResource2 = resourceLoader.getResource("classpath:image/other/honey_badger.JPG");
		Path sourcePath1 = null, sourcePath2 = null;
		try {
			sourcePath1 = Paths.get(sourceResource1.getFile().getAbsolutePath());
			sourcePath2 = Paths.get(sourceResource2.getFile().getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to get file resources." + e.getMessage());
			e.printStackTrace();
		}
		
		// create file store
		Path examplePath = Paths.get(getTestFileStorePath());
		CmsFileStore fileStore = null;
		try {
			fileStore = storeService.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test moving files.", false);
		} catch (CmsServiceException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 1
		final String subDirName1 = "move_test1";
		CmsDirectory subTest1 = null;
		try {
			subTest1 = storeService.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (CmsServiceException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 2
		final String subDirName2 = "move_test2";
		CmsDirectory subTest2 = null;
		try {
			subTest2 = storeService.addDirectory(subTest1.getDirId(), subDirName2);
		} catch (CmsServiceException e) {
			logger.error("Failed to add child directory to dir => " + subTest1.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 3
		final String subDirName3 = "move_test3";
		CmsDirectory subTest3 = null;
		try {
			subTest3 = storeService.addDirectory(subTest2.getDirId(), subDirName3);
		} catch (CmsServiceException e) {
			logger.error("Failed to add child directory to dir => " + subTest2.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// persist files to 1st and 2nd directories
		CmsFileEntry fileEntry1 = null;
		CmsFileEntry fileEntry2 = null;
		try {
			
			logger.info("Test file 1 => " + sourcePath1.toString());
			
			logger.info("Test file 2 => " + sourcePath2.toString());		
			
			fileEntry1 = storeService.addFile(sourcePath1, subTest1.getDirId(), true);
			fileEntry2 = storeService.addFile(sourcePath2, subTest2.getDirId(), true);
			
		} catch (CmsServiceException e) {
			logger.error("Error adding file. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(fileEntry1);
		assertNotNull(fileEntry1.getDirectory());
		assertNotNull(fileEntry1.getDirectory().getFileEntries());
		assertNotNull(fileEntry1.getFile());
		
		assertNotNull(fileEntry2);
		assertNotNull(fileEntry2.getDirectory());
		assertNotNull(fileEntry2.getDirectory().getFileEntries());
		assertNotNull(fileEntry2.getFile());
		
		logger.info("CmsFileEntry 1:");
		logger.info(fileEntry1.toString());
		
		logger.info("CmsFileEntry 2:");
		logger.info(fileEntry2.toString());
		
		Path dirPath1 = storeService.getAbsoluteDirectoryPath(fileStore, fileEntry1.getDirectory());
		Path fullFilePath1 = storeService.getAbsoluteFilePath(fileStore, fileEntry1.getDirectory(), fileEntry1);
		assertNotNull(dirPath1);
		logger.info("Path of 1 => " + dirPath1.toString());
		logger.info("Path of cms file 1 => " + fullFilePath1.toString());
		assertTrue(Files.exists(fullFilePath1));
		
		Path dirPath2 = storeService.getAbsoluteDirectoryPath(fileStore, fileEntry2.getDirectory());
		Path fullFilePath2 = storeService.getAbsoluteFilePath(fileStore, fileEntry2.getDirectory(), fileEntry2);
		assertNotNull(dirPath2);
		logger.info("Path of 2 => " + dirPath2.toString());
		logger.info("Path of cms file 2 => " + fullFilePath2.toString());
		assertTrue(Files.exists(fullFilePath2));
		
		//
		// move file in move_test1 dir to move_test3 dir (no replace required)
		//
		CmsFileEntry movedEntry = null;
		try {
			movedEntry = storeService.moveFile(fileEntry1.getFileId(), subTest3.getDirId(), true);
		} catch (CmsServiceException e){
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}		
		
		assertNotNull(movedEntry);
		assertNotNull(movedEntry.getDirectory());
		assertNotNull(movedEntry.getDirectory().getFileEntries());
		//assertNotNull(movedEntry.getFile());
		
		logger.info("Moved CmsFileEntry 1 to dir 3:");
		logger.info(movedEntry.toString());
		
		// check that file has been moved to dir 3
		Path dirPath3 = storeService.getAbsoluteDirectoryPath(fileStore, movedEntry.getDirectory());
		Path fullFilePath3 = storeService.getAbsoluteFilePath(fileStore, movedEntry.getDirectory(), movedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3.toString());
		logger.info("Path of cms file 3 => " + fullFilePath3.toString());
		assertTrue(!Files.exists(fullFilePath1));
		assertTrue(Files.exists(fullFilePath3));
		
		//
		// move file in move_test2 dir to move_test3 dir (requires a replace of existing file)
		//
		try {
			movedEntry = storeService.moveFile(fileEntry2.getFileId(), subTest3.getDirId(), true);
		} catch (CmsServiceException e){
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		assertNotNull(movedEntry);
		assertNotNull(movedEntry.getDirectory());
		assertNotNull(movedEntry.getDirectory().getFileEntries());
		//assertNotNull(movedEntry.getFile());
		
		logger.info("Moved CmsFileEntry 2 to dir 3:");
		logger.info(movedEntry.toString());
		
		// check that file has been moved to dir 3
		dirPath3 = storeService.getAbsoluteDirectoryPath(fileStore, movedEntry.getDirectory());
		fullFilePath3 = storeService.getAbsoluteFilePath(fileStore, movedEntry.getDirectory(), movedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3.toString());
		logger.info("Path of cms file 3 => " + fullFilePath3.toString());
		assertTrue(!Files.exists(fullFilePath2));
		assertTrue(Files.exists(fullFilePath3));
		
	}
	
	public abstract String getTestFileStorePath();
	
}
