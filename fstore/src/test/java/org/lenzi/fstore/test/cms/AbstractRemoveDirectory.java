package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
public abstract class AbstractRemoveDirectory extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FileStoreRepository fileStoreRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractRemoveDirectory() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doRemoveDirectory() throws Exception {
		
		logTestTitle("Remove directory test");
		
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
		
		// add sub dir 1
		final String subDirName1 = "sub1";
		CmsDirectory subTest1 = null;
		try {
			subTest1 = fileStoreRepository.addDirectory(fileStore.getRootDir().getDirId(), subDirName1);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		// add sub dir 2
		final String subDirName2 = "sub2";
		CmsDirectory subTest2 = null;
		try {
			subTest2 = fileStoreRepository.addDirectory(subTest1.getDirId(), subDirName2);
		} catch (DatabaseException e) {
			logger.error("Failed to add child directory to dir => " + subTest1.getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		if(filePaths.size() < 4){
			throw new Exception("Need at least 4 files to run this test. Check " + sourceDirPath.toString());
		}
		
		List<Path> firstTwo = new ArrayList<Path>();
		firstTwo.add(filePaths.get(0));
		firstTwo.add(filePaths.get(1));
		
		//
		// add first two files to first directory
		//
		List<CmsFileEntry> fileEntriesGroup1 = null;
		try {
			fileEntriesGroup1 = fileStoreRepository.addFile(firstTwo, subTest1.getDirId(), true);
		} catch (DatabaseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		//
		// add all files to second directory
		//
		List<CmsFileEntry> fileEntriesGroup2 = null;
		try {
			fileEntriesGroup2 = fileStoreRepository.addFile(filePaths, subTest2.getDirId(), true);
		} catch (DatabaseException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		assertEquals(firstTwo.size(), fileEntriesGroup1.size());
		assertEquals(filePaths.size(), fileEntriesGroup2.size());
		
		//
		// Perform delete on first directory
		//
		fileStoreRepository.removeDirectory(subTest1.getDirId());
	
	}
	
	public abstract String getTestFileStorePath();
	
}
