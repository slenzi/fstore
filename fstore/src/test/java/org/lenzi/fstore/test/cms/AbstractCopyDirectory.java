package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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
public abstract class AbstractCopyDirectory extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FileStoreRepository fileStoreRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractCopyDirectory() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doCopyDirectory() {
		
		logTestTitle("Copy directory test");
		
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
					"Example File Store", "This is an example file store to test copy directory.", true);
		} catch (DatabaseException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir for fun
		CmsDirectory dir1 = null, dir2 = null, dir3 = null, dir4 = null, dir5 = null, dir6 = null;
		try {
			
			dir1 = fileStoreRepository.addDirectory(fileStore.getRootDir().getDirId(), "dir1");
				dir2 = fileStoreRepository.addDirectory(dir1.getDirId(), "dir2");
					dir3 = fileStoreRepository.addDirectory(dir2.getDirId(), "dir3");
					dir4 = fileStoreRepository.addDirectory(dir2.getDirId(), "dir4");
				dir5 = fileStoreRepository.addDirectory(dir1.getDirId(), "dir5");
					dir6 = fileStoreRepository.addDirectory(dir5.getDirId(), "dir6");
			
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		List<CmsFileEntry> dir2Entries = null, dir6Entries = null;
		try {
			dir2Entries = fileStoreRepository.addFile(filePaths, dir2.getDirId(), true);
			dir6Entries = fileStoreRepository.addFile(filePaths, dir6.getDirId(), true);
		} catch (DatabaseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		assertEquals(filePaths.size(), dir2Entries.size());
		assertEquals(filePaths.size(), dir6Entries.size());
		
		logger.info("Tree before:");
		try {
			logger.info(fileStoreRepository.printTree(fileStore.getRootDir().getDirId()));
		} catch (DatabaseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		//
		// do copy
		//
		try {
			fileStoreRepository.copyDirectory(dir2.getDirId(), dir5.getDirId(), true);
		} catch (FileAlreadyExistsException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DatabaseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		logger.info("Tree after:");
		try {
			logger.info(fileStoreRepository.printTree(fileStore.getRootDir().getDirId()));
		} catch (DatabaseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	
	}
	
	public abstract String getTestFileStorePath();
	
}
