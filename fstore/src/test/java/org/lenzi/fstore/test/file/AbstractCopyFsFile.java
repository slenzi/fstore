package org.lenzi.fstore.test.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;


import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsService;
import org.lenzi.fstore.file.service.exception.FsServiceException;
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
public abstract class AbstractCopyFsFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsService storeService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractCopyFsFile() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doCopyFile() throws Exception {
		
		logTestTitle("Copy file test");
		
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
		FsFileStore fileStore = null;
		try {
			fileStore = storeService.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test copying files.", false);
		} catch (FsServiceException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 1
		final String subDirName1 = "copy_test1";
		FsDirectory subTest1 = null;
		try {
			subTest1 = storeService.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (FsServiceException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 2
		final String subDirName2 = "copy_test2";
		FsDirectory subTest2 = null;
		try {
			subTest2 = storeService.addDirectory(subTest1.getDirId(), subDirName2);
		} catch (FsServiceException e) {
			logger.error("Failed to add child directory to dir => " + subTest1.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 3
		final String subDirName3 = "copy_test3";
		FsDirectory subTest3 = null;
		try {
			subTest3 = storeService.addDirectory(subTest2.getDirId(), subDirName3);
		} catch (FsServiceException e) {
			logger.error("Failed to add child directory to dir => " + subTest2.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// persist files to 1st and 2nd directories
		FsFileEntry fileEntry1 = null;
		FsFileEntry fileEntry2 = null;
		try {
			
			logger.info("Test file 1 => " + sourcePath1.toString());
			logger.info("Test file Size 1 => " + Files.size(sourcePath1));
			
			logger.info("Test file 2 => " + sourcePath2.toString());
			logger.info("Test file Size 2 => " + Files.size(sourcePath2));			
			
			fileEntry1 = storeService.addFile(sourcePath1, subTest1.getDirId(), true);
			fileEntry2 = storeService.addFile(sourcePath2, subTest2.getDirId(), true);
			
		}catch(IOException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (FsServiceException e) {
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
		
		logger.info("FsFileEntry 1:");
		logger.info(fileEntry1.toString());
		
		logger.info("FsFileEntry 2:");
		logger.info(fileEntry2.toString());
		
		Path dirPath1 = storeService.getAbsoluteDirectoryPath(fileStore, fileEntry1.getDirectory());
		Path fullFilePath1 = storeService.getAbsoluteFilePath(fileStore, fileEntry1.getDirectory(), fileEntry1);
		assertNotNull(dirPath1);
		logger.info("Path of 1 => " + dirPath1.toString());
		logger.info("Path of file 1 => " + fullFilePath1.toString());
		assertTrue(Files.exists(fullFilePath1));
		
		Path dirPath2 = storeService.getAbsoluteDirectoryPath(fileStore, fileEntry2.getDirectory());
		Path fullFilePath2 = storeService.getAbsoluteFilePath(fileStore, fileEntry2.getDirectory(), fileEntry2);
		assertNotNull(dirPath2);
		logger.info("Path of 2 => " + dirPath2.toString());
		logger.info("Path of file 2 => " + fullFilePath2.toString());
		assertTrue(Files.exists(fullFilePath2));
		
		//
		// copy file in copy_test1 dir to copy_test3 dir (no replace required)
		//
		FsFileEntry copiedEntry = null;
		try {
			copiedEntry = storeService.copyFile(fileEntry1.getFileId(), subTest3.getDirId(), true);
		} catch (FsServiceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(copiedEntry);
		assertNotNull(copiedEntry.getDirectory());
		assertNotNull(copiedEntry.getDirectory().getFileEntries());
		assertNotNull(copiedEntry.getFile());
		
		logger.info("Copied FsFileEntry 1 to dir 3:");
		logger.info(copiedEntry.toString());
		
		// check that file has been copied to dir 3
		Path dirPath3 = storeService.getAbsoluteDirectoryPath(fileStore, copiedEntry.getDirectory());
		Path fullFilePath3 = storeService.getAbsoluteFilePath(fileStore, copiedEntry.getDirectory(), copiedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3.toString());
		logger.info("Path of file 3 => " + fullFilePath3.toString());
		assertTrue(Files.exists(fullFilePath3));
		
		//
		// copy file in copy_test2 dir to copy_test3 dir (requires a replace of existing file)
		//
		try {
			copiedEntry = storeService.copyFile(fileEntry2.getFileId(), subTest3.getDirId(), true);
		} catch (FsServiceException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(copiedEntry);
		assertNotNull(copiedEntry.getDirectory());
		assertNotNull(copiedEntry.getDirectory().getFileEntries());
		assertNotNull(copiedEntry.getFile());
		
		logger.info("Copied FsFileEntry 2 to dir 3:");
		logger.info(copiedEntry.toString());
		
		// check that file has been copied to dir 3
		dirPath3 = storeService.getAbsoluteDirectoryPath(fileStore, copiedEntry.getDirectory());
		fullFilePath3 = storeService.getAbsoluteFilePath(fileStore, copiedEntry.getDirectory(), copiedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3.toString());
		logger.info("Path of file 3 => " + fullFilePath3.toString());
		assertTrue(Files.exists(fullFilePath3));
		
	}
	
	public abstract String getTestFileStorePath();
	
}