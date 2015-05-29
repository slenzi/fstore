package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.util.FileUtil;
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
public abstract class AbstractBulkAddFile extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FileStoreRepository fileStoreRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractBulkAddFile() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doAddBulkFile() {
		
		logTestTitle("Add bulk file test");
		
		assertNotNull(resourceLoader);
		
		// get test file for upload to database
		Resource sourceDir = resourceLoader.getResource("classpath:image/");
		
		Path sourceDirPath = null;
		try {
			sourceDirPath = Paths.get(sourceDir.getFile().getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to get source dir resource." + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// get all files to add
		List<Path> filePaths = null;
		try {
			filePaths = FileUtil.listFilesToDepth(sourceDirPath, 1);
		} catch (IOException e) {
			logger.error("Failed to get source dir resource." + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// create file store
		Path examplePath = Paths.get(getTestFileStorePath());
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(examplePath, 
					"Example File Store", "This is an example file store to test bulk file add.", true);
		} catch (DatabaseException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir for fun
		final String subDirName = "upload_bulk";
		CmsDirectory subTest = null;
		try {
			subTest = fileStoreRepository.addDirectory(fileStore.getRootDir().getDirId(), subDirName);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		List<CmsFileEntry> fileEntries = null;
		try {
			fileEntries = fileStoreRepository.addFile(filePaths, subTest.getDirId(), true);
		} catch (DatabaseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		assertEquals(filePaths.size(), fileEntries.size());
	
	}
	
	public abstract String getTestFileStorePath();
	
}
