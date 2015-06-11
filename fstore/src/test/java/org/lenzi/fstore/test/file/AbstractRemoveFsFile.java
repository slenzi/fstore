package org.lenzi.fstore.test.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
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
public abstract class AbstractRemoveFsFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsService storeService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractRemoveFsFile() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doRemoveFile() {
		
		logTestTitle("Remove file test");
		
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
		FsFileStore fileStore = null;
		try {
			fileStore = storeService.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test removing files.", false);
		} catch (FsServiceException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 
		final String subDirName = "upload_test";
		FsDirectory subTest = null;
		try {
			subTest = storeService.addDirectory(fileStore.getRootDir().getDirId(), subDirName);
		} catch (FsServiceException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}		
		
		// persist file to database
		FsFileEntry fileEntry = null;
		try {
			
			logger.info("Test file => " + sourcePath.toString());
			logger.info("Size => " + Files.size(sourcePath));
			
			fileEntry = storeService.addFile(sourcePath, subTest.getDirId(), true);
			
		}catch(IOException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (FsServiceException e) {
			logger.error("Error adding file. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(fileEntry);
		assertNotNull(fileEntry.getDirectory());
		assertNotNull(fileEntry.getDirectory().getFileEntries());
		assertNotNull(fileEntry.getFile());
		
		logger.info("CmsFileEntry:");
		logger.info(fileEntry.toString());
		
		Path dirPath = null;
		try {
			dirPath = storeService.getAbsoluteDirectoryPath(fileEntry.getDirectory().getDirId());
		} catch (FsServiceException e) {
			logger.error("Error getting path for cms directory. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(dirPath);
		logger.info("Path of directory => " + dirPath.toString());
		String fullFilePath = dirPath + File.separator + fileEntry.getFileName();
		Path targetPath = Paths.get(fullFilePath);
		logger.info("Path of cms file => " + targetPath.toString());
		assertTrue(Files.exists(targetPath));
		
		//
		// perform delete
		//
		try {
			storeService.removeFile(fileEntry.getFileId());
		} catch (FsServiceException e) {
			logger.error("Error removing file id => " + fileEntry.getFileId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		assertTrue(!Files.exists(targetPath));
	
	}
	
	public abstract String getTestFileStorePath();
	
}