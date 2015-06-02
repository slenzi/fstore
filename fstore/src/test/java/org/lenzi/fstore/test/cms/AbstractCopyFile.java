package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;


import org.lenzi.fstore.cms.repository.FileStoreRepository;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.FileStoreHelper;
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
public abstract class AbstractCopyFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FileStoreRepository fileStoreRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private FileStoreHelper fileStoreManager;	
	
	public AbstractCopyFile() {

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
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test copying files.", false);
		} catch (DatabaseException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 1
		final String subDirName1 = "copy_test1";
		CmsDirectory subTest1 = null;
		try {
			subTest1 = fileStoreRepository.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 2
		final String subDirName2 = "copy_test2";
		CmsDirectory subTest2 = null;
		try {
			subTest2 = fileStoreRepository.addDirectory(subTest1.getDirId(), subDirName2);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + subTest1.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 3
		final String subDirName3 = "copy_test3";
		CmsDirectory subTest3 = null;
		try {
			subTest3 = fileStoreRepository.addDirectory(subTest2.getDirId(), subDirName3);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + subTest2.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// persist files to 1st and 2nd directories
		CmsFileEntry fileEntry1 = null;
		CmsFileEntry fileEntry2 = null;
		try {
			
			logger.info("Test file 1 => " + sourcePath1.toString());
			logger.info("Test file Size 1 => " + Files.size(sourcePath1));
			
			logger.info("Test file 2 => " + sourcePath2.toString());
			logger.info("Test file Size 2 => " + Files.size(sourcePath2));			
			
			fileEntry1 = fileStoreRepository.addFile(sourcePath1, subTest1.getDirId(), true);
			fileEntry2 = fileStoreRepository.addFile(sourcePath2, subTest2.getDirId(), true);
			
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
		
		String dirPath1 = fileStoreManager.getAbsoluteDirectoryString(fileStore, fileEntry1.getDirectory());
		String fullFilePath1 = fileStoreManager.getAbsoluteFileString(fileStore, fileEntry1.getDirectory(), fileEntry1);
		assertNotNull(dirPath1);
		logger.info("Path of 1 => " + dirPath1);
		Path targetPath1 = Paths.get(fullFilePath1);
		logger.info("Path of cms file 1 => " + targetPath1.toString());
		assertTrue(Files.exists(targetPath1));
		
		String dirPath2 = fileStoreManager.getAbsoluteDirectoryString(fileStore, fileEntry2.getDirectory());
		String fullFilePath2 = fileStoreManager.getAbsoluteFileString(fileStore, fileEntry2.getDirectory(), fileEntry2);
		assertNotNull(dirPath2);
		logger.info("Path of 2 => " + dirPath2);
		Path targetPath2 = Paths.get(fullFilePath2);
		logger.info("Path of cms file 2 => " + targetPath2.toString());
		assertTrue(Files.exists(targetPath2));
		
		//
		// copy file in copy_test1 dir to copy_test3 dir (no replace required)
		//
		CmsFileEntry copiedEntry = null;
		copiedEntry = fileStoreRepository.copyFile(fileEntry1.getFileId(), subTest3.getDirId(), true);
		
		assertNotNull(copiedEntry);
		assertNotNull(copiedEntry.getDirectory());
		assertNotNull(copiedEntry.getDirectory().getFileEntries());
		assertNotNull(copiedEntry.getFile());
		
		logger.info("Copied CmsFileEntry 1 to dir 3:");
		logger.info(copiedEntry.toString());
		
		// check that file has been copied to dir 3
		String dirPath3 = fileStoreManager.getAbsoluteDirectoryString(fileStore, copiedEntry.getDirectory());
		String fullFilePath3 = fileStoreManager.getAbsoluteFileString(fileStore, copiedEntry.getDirectory(), copiedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3);
		Path targetPath3 = Paths.get(fullFilePath3);
		logger.info("Path of cms file 3 => " + targetPath3.toString());;
		assertTrue(Files.exists(targetPath3));
		
		//
		// copy file in copy_test2 dir to copy_test3 dir (requires a replace of existing file)
		//
		try {
			copiedEntry = fileStoreRepository.copyFile(fileEntry2.getFileId(), subTest3.getDirId(), true);
		} catch (FileAlreadyExistsException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DatabaseException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(copiedEntry);
		assertNotNull(copiedEntry.getDirectory());
		assertNotNull(copiedEntry.getDirectory().getFileEntries());
		assertNotNull(copiedEntry.getFile());
		
		logger.info("Copied CmsFileEntry 2 to dir 3:");
		logger.info(copiedEntry.toString());
		
		// check that file has been copied to dir 3
		dirPath3 = fileStoreManager.getAbsoluteDirectoryString(fileStore, copiedEntry.getDirectory());
		fullFilePath3 = fileStoreManager.getAbsoluteFileString(fileStore, copiedEntry.getDirectory(), copiedEntry);
		assertNotNull(dirPath3);
		logger.info("Path of 3 => " + dirPath3);
		targetPath3 = Paths.get(fullFilePath3);
		logger.info("Path of cms file 3 => " + targetPath3.toString());
		assertTrue(Files.exists(targetPath3));
		
	}
	
	public abstract String getTestFileStorePath();
	
}
