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
		
		// persist file to database
		CmsFileEntry fileEntry = null;
		try {
			
			logger.info("Test file => " + sourcePath.toString());
			logger.info("Size => " + Files.size(sourcePath));
			
			fileEntry = fileStoreRepository.addFile(sourcePath, subTest2.getDirId(), true);
			
		}catch(IOException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DatabaseException e) {
			logger.error("Error adding file. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(fileEntry);
		assertNotNull(fileEntry.getDirectory());
		assertNotNull(fileEntry.getDirectory().getFileEntries());
		assertNotNull(fileEntry.getFile());
		
		logger.info("CmsFileEntry:");
		logger.info(fileEntry.toString());
		
		String dirPath = fileStoreRepository.getAbsoluteDirectoryPath(fileStore, fileEntry.getDirectory());
		
		assertNotNull(dirPath);
		
		logger.info("Path of directory => " + dirPath);
		
		String fullFilePath = dirPath + File.separator + fileEntry.getFileName();
		Path targetPath = Paths.get(fullFilePath);
		
		logger.info("Path of cms file => " + targetPath.toString());
		
		assertTrue(Files.exists(targetPath));
		
		
		//
		// move file one directory up
		//
		CmsFileEntry movedEntry = null;
		movedEntry = fileStoreRepository.moveFile(fileEntry.getFileId(), subTest1.getDirId(), true);
		
		assertNotNull(movedEntry);
		assertNotNull(movedEntry.getDirectory());
		assertNotNull(movedEntry.getDirectory().getFileEntries());
		//assertNotNull(movedEntry.getFile());
		
		logger.info("Moved CmsFileEntry:");
		logger.info(movedEntry.toString());
		
		String newDirPath = fileStoreRepository.getAbsoluteDirectoryPath(fileStore, movedEntry.getDirectory());
		String newFilePath = fileStoreRepository.getAbsoluteFilePath(fileStore, movedEntry.getDirectory(), movedEntry);
		
		assertNotNull(newDirPath);
		
		logger.info("New directory path => " + dirPath);
		
		Path newTargetPath = Paths.get(newFilePath);
		
		logger.info("Path of cms file after move => " + newTargetPath.toString());
		
		assertTrue(Files.exists(newTargetPath));
	
	}
	
	public abstract String getTestFileStorePath();
	
}
