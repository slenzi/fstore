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
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
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
public abstract class AbstractAddFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FileStoreRepository fileStoreRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractAddFile() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doAddFile() {
		
		logTestTitle("Add file test");
		
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
		
		// persist file to database
		CmsFileEntry fileEntry = null;
		try {
			
			logger.info("Test file => " + sourcePath.toString());
			logger.info("Size => " + Files.size(sourcePath));
			
			fileEntry = fileStoreRepository.addFile(sourcePath, 1L);
			
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
		
		String dirPath = null;
		try {
			dirPath = fileStoreRepository.getAbsoluteDirPath(fileEntry.getDirectory().getNodeId());
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
	
	}
	
}
