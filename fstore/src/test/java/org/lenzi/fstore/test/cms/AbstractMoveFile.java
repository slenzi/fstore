package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;


import org.lenzi.fstore.cms.repository.FileStoreRepository;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.repository.exception.DatabaseException;
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
	private FileStoreRepository fileStoreRepository;
	
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
		Resource sourceResource = resourceLoader.getResource("classpath:image/honey_badger.JPG");
		Path sourcePath = null;
		try {
			sourcePath = Paths.get(sourceResource.getFile().getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to get file resource." + e.getMessage());
			e.printStackTrace();
		}
		
		// create file store
		Path examplePath = Paths.get(getTestFileStorePath());
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test adding of files.", false);
		} catch (DatabaseException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 1
		final String subDirName1 = "move_test1";
		CmsDirectory subTest1 = null;
		try {
			subTest1 = fileStoreRepository.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 2
		final String subDirName2 = "move_test2";
		CmsDirectory subTest2 = null;
		try {
			subTest2 = fileStoreRepository.addDirectory(subTest1.getDirId(), subDirName2);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + subTest1.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 3
		final String subDirName3 = "move_test3";
		CmsDirectory subTest3 = null;
		try {
			subTest3 = fileStoreRepository.addDirectory(subTest2.getDirId(), subDirName3);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + subTest2.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// persist file to 1st and 2nd dir
		CmsFileEntry fileEntry1 = null;
		CmsFileEntry fileEntry2 = null;
		try {
			
			logger.info("Test file => " + sourcePath.toString());
			logger.info("Size => " + Files.size(sourcePath));
			
			fileEntry1 = fileStoreRepository.addFile(sourcePath, subTest1.getDirId(), true);
			fileEntry2 = fileStoreRepository.addFile(sourcePath, subTest2.getDirId(), true);
			
		}catch(IOException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DatabaseException e) {
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
		
		String dirPath1 = fileStoreRepository.getAbsoluteDirectoryPath(fileStore, fileEntry1.getDirectory());
		String fullFilePath1 = fileStoreRepository.getAbsoluteFilePath(fileStore, fileEntry1.getDirectory(), fileEntry1);
		assertNotNull(dirPath1);
		logger.info("Path of 1 => " + dirPath1);
		Path targetPath1 = Paths.get(fullFilePath1);
		logger.info("Path of cms file 1 => " + targetPath1.toString());
		assertTrue(Files.exists(targetPath1));
		
		String dirPath2 = fileStoreRepository.getAbsoluteDirectoryPath(fileStore, fileEntry2.getDirectory());
		String fullFilePath2 = fileStoreRepository.getAbsoluteFilePath(fileStore, fileEntry2.getDirectory(), fileEntry2);
		assertNotNull(dirPath2);
		logger.info("Path of 2 => " + dirPath2);
		Path targetPath2 = Paths.get(fullFilePath2);
		logger.info("Path of cms file 2 => " + targetPath2.toString());
		assertTrue(Files.exists(targetPath2));
		
		
		//
		// move file in move_test1 dir to move_test3 dir (no replace required)
		//
		CmsFileEntry movedEntry = null;
		movedEntry = fileStoreRepository.moveFile(fileEntry1.getFileId(), subTest3.getDirId(), true);
		
		assertNotNull(movedEntry);
		assertNotNull(movedEntry.getDirectory());
		assertNotNull(movedEntry.getDirectory().getFileEntries());
		//assertNotNull(movedEntry.getFile());
		
		logger.info("Moved CmsFileEntry 1 to dir 3:");
		logger.info(movedEntry.toString());
		
		// check that file has been moved to dir 3
		String dirPath3 = fileStoreRepository.getAbsoluteDirectoryPath(fileStore, movedEntry.getDirectory());
		String fullFilePath3 = fileStoreRepository.getAbsoluteFilePath(fileStore, movedEntry.getDirectory(), movedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3);
		Path targetPath3 = Paths.get(fullFilePath3);
		logger.info("Path of cms file 3 => " + targetPath3.toString());
		assertTrue(!Files.exists(targetPath1));
		assertTrue(Files.exists(targetPath3));
		
		//
		// move file in move_test2 dir to move_test3 dir (requires a replace of existing file)
		//
		movedEntry = fileStoreRepository.moveFile(fileEntry2.getFileId(), subTest3.getDirId(), true);
		
		assertNotNull(movedEntry);
		assertNotNull(movedEntry.getDirectory());
		assertNotNull(movedEntry.getDirectory().getFileEntries());
		//assertNotNull(movedEntry.getFile());
		
		logger.info("Moved CmsFileEntry 2 to dir 3:");
		logger.info(movedEntry.toString());
		
		// check that file has been moved to dir 3
		dirPath3 = fileStoreRepository.getAbsoluteDirectoryPath(fileStore, movedEntry.getDirectory());
		fullFilePath3 = fileStoreRepository.getAbsoluteFilePath(fileStore, movedEntry.getDirectory(), movedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3);
		targetPath3 = Paths.get(fullFilePath3);
		logger.info("Path of cms file 3 => " + targetPath3.toString());
		assertTrue(!Files.exists(targetPath2));
		assertTrue(Files.exists(targetPath3));
		
	}
	
	public abstract String getTestFileStorePath();
	
}
