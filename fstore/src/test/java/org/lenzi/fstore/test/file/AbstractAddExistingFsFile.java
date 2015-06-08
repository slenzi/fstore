package org.lenzi.fstore.test.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
public abstract class AbstractAddExistingFsFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsService storeService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	public AbstractAddExistingFsFile() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doAddExistingFile() throws FsServiceException, IOException {
		
		logTestTitle("Add existing file test");
		
		assertNotNull(resourceLoader);
		
		Resource sourceResource1 = resourceLoader.getResource("classpath:image/honey_badger.JPG");
		Resource sourceResource2 = resourceLoader.getResource("classpath:image/other/honey_badger.JPG");
		
		// get test files
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
					examplePath, "Example File Store", "This is an example file store to test adding of files.", false);
		} catch (FsServiceException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 
		final String subDirName1 = "upload_test";
		FsDirectory subTest1 = null;
		try {
			subTest1 = storeService.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (FsServiceException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}		
		
		// persist file to database
		FsFileEntry fileEntry = null;
		try {
			
			logger.info("Resource 1 => " + sourcePath1.toString());
			
			fileEntry = storeService.addFile(sourcePath1, subTest1.getDirId(), true);
			
		} catch (FsServiceException e) {
			logger.error("Error adding file. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(fileEntry);
		assertNotNull(fileEntry.getDirectory());
		assertNotNull(fileEntry.getDirectory().getFileEntries());
		assertNotNull(fileEntry.getFile());
		
		logger.info("FsFileEntry:");
		logger.info(fileEntry.toString());
		
		//
		// check file on disk
		//
		Path dirPath = null;
		try {
			dirPath = storeService.getAbsoluteDirectoryPath(fileEntry.getDirectory().getDirId());
		} catch (FsServiceException e) {
			logger.error("Error getting path for directory. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(dirPath);
		logger.info("Path of directory => " + dirPath.toString());
		String fullFilePath = dirPath.toString() + File.separator + fileEntry.getFileName();
		Path targetPath = Paths.get(fullFilePath);
		logger.info("Path of file => " + targetPath.toString());
		assertTrue(Files.exists(targetPath));
		
		//
		// persist second version of file with same name
		//
		//exception.expect(DatabaseException.class);
		FsFileEntry updatedEntry = storeService.addFile(sourcePath2, subTest1.getDirId(), true);
				
		assertNotNull(updatedEntry);
		assertNotNull(updatedEntry.getDirectory());
		assertNotNull(updatedEntry.getDirectory().getFileEntries());
		assertNotNull(updatedEntry.getFile());
		
		logger.info("Updated FsFileEntry:");
		logger.info(updatedEntry.toString());
		
		//
		// re-check file on disk
		//
		try {
			dirPath = storeService.getAbsoluteDirectoryPath(updatedEntry.getDirectory().getDirId());
		} catch (FsServiceException e) {
			logger.error("Error getting path for directory. " + e.getMessage());
			e.printStackTrace();
		}
		
		assertNotNull(dirPath);
		logger.info("Path of directory => " + dirPath.toString());
		fullFilePath = dirPath.toString() + File.separator + updatedEntry.getFileName();
		targetPath = Paths.get(fullFilePath);
		logger.info("Path of file => " + targetPath.toString());
		assertTrue(Files.exists(targetPath));		
		
	}
	
	public abstract String getTestFileStorePath();
	
}
