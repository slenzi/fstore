package org.lenzi.fstore.test.cms;

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
public abstract class AbstractAddExistingFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FileStoreRepository fileStoreRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	public AbstractAddExistingFile() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doAddExistingFile() throws DatabaseException, IOException {
		
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
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(
					examplePath, "Example File Store", "This is an example file store to test adding of files.", false);
		} catch (DatabaseException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 
		final String subDirName1 = "upload_test";
		CmsDirectory subTest1 = null;
		try {
			subTest1 = fileStoreRepository.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}		
		
		// persist file to database
		CmsFileEntry fileEntry = null;
		try {
			
			logger.info("Resource 1 => " + sourcePath1.toString());
			logger.info("Resource 1 Size => " + Files.size(sourcePath1));
			
			fileEntry = fileStoreRepository.addFile(sourcePath1, subTest1.getDirId(), true);
			
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
		
		//
		// check file on disk
		//
		String dirPath = null;
		try {
			dirPath = fileStoreRepository.getAbsoluteDirectoryPath(fileEntry.getDirectory().getDirId());
		} catch (DatabaseException e) {
			logger.error("Error getting path for cms directory. " + e.getMessage());
			e.printStackTrace();
		}
		assertNotNull(dirPath);

		logger.info("Path of directory => " + dirPath);
		
		String fullFilePath = dirPath + File.separator + fileEntry.getFileName();
		Path targetPath = Paths.get(fullFilePath);
		
		logger.info("Path of cms file => " + targetPath.toString());
		
		assertTrue(Files.exists(targetPath));
		
		
		//
		// persist second version of file with same name
		//
		//exception.expect(DatabaseException.class);
		CmsFileEntry updatedEntry = fileStoreRepository.addFile(sourcePath2, subTest1.getDirId(), true);
				
		assertNotNull(updatedEntry);
		assertNotNull(updatedEntry.getDirectory());
		assertNotNull(updatedEntry.getDirectory().getFileEntries());
		assertNotNull(updatedEntry.getFile());
		
		logger.info("Updated CmsFileEntry:");
		logger.info(updatedEntry.toString());
		
		//
		// re-check file on disk
		//
		try {
			dirPath = fileStoreRepository.getAbsoluteDirectoryPath(updatedEntry.getDirectory().getDirId());
		} catch (DatabaseException e) {
			logger.error("Error getting path for cms directory. " + e.getMessage());
			e.printStackTrace();
		}
		assertNotNull(dirPath);

		logger.info("Path of directory => " + dirPath);
		
		fullFilePath = dirPath + File.separator + updatedEntry.getFileName();
		targetPath = Paths.get(fullFilePath);
		
		logger.info("Path of cms file => " + targetPath.toString());
		
		assertTrue(Files.exists(targetPath));		
		
	}
	
	public abstract String getTestFileStorePath();
	
}
