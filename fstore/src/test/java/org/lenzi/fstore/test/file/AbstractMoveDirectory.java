package org.lenzi.fstore.test.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.core.util.FileUtil;
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
public abstract class AbstractMoveDirectory extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsService storeService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	public AbstractMoveDirectory() {

	}

	/**
	 * fetch directory test
	 */
	@Test
	@Rollback(false)
	public void doCopyDirectory() {
		
		logTestTitle("Move directory test");
		
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
		
		// get all files to add (depth 1)
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
		FsFileStore fileStore = null;
		try {
			fileStore = storeService.createFileStore(examplePath, 
					"Example File Store", "This is an example file store to test copy directory.", true);
		} catch (FsServiceException e) {
			logger.error("Failed to create new file store. " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// add sub dir for fun
		FsDirectory dir1 = null, dir2 = null, dir3 = null, dir4 = null, dir5 = null;
		FsDirectory dir6 = null, dir7 = null, dir8 = null, dir9 = null;
		try {
			
			dir1 = storeService.addDirectory(fileStore.getRootDir().getDirId(), "dir1");
				dir2 = storeService.addDirectory(dir1.getDirId(), "dir2");
					dir3 = storeService.addDirectory(dir2.getDirId(), "dir3");
					dir4 = storeService.addDirectory(dir2.getDirId(), "dir4");
						dir5 = storeService.addDirectory(dir4.getDirId(), "dir5");
							dir6 = storeService.addDirectory(dir5.getDirId(), "dir6");
				dir7 = storeService.addDirectory(dir1.getDirId(), "dir7");
					// dir 8 has same name as dir 2 so we can test merging
					dir8 = storeService.addDirectory(dir7.getDirId(), "dir2");
				dir9 = storeService.addDirectory(dir1.getDirId(), "dir9");
			
		} catch (FsServiceException e) {
			logger.error("Failed to add child directory to dir => " + fileStore.getRootDir().getNodeId() + ". " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//
		// add some files to dir2, dir3, dir6, and dir8
		//
		List<FsFileEntry> dir2Entries = null, dir3Entries = null, dir6Entries = null, dir8Entries = null;
		try {
			dir2Entries = storeService.addFile(filePaths, dir2.getDirId(), true);
			dir3Entries = storeService.addFile(filePaths, dir3.getDirId(), true);
			dir6Entries = storeService.addFile(filePaths, dir6.getDirId(), true);
			dir8Entries = storeService.addFile(filePaths, dir8.getDirId(), true);
		} catch (FsServiceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		assertEquals(filePaths.size(), dir2Entries.size());
		assertEquals(filePaths.size(), dir3Entries.size());
		assertEquals(filePaths.size(), dir6Entries.size());
		assertEquals(filePaths.size(), dir8Entries.size());
		
		logger.info("Tree before:");
		try {
			logger.info(storeService.printTree(fileStore.getRootDir().getDirId()));
		} catch (FsServiceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//
		// do move - this copy does not require replacing files
		//
		try {
			storeService.moveDirectory(dir4.getDirId(), dir9.getDirId(), true);
		} catch (FsServiceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//
		// do move - this copy DOES require merging directories and replacing files
		//
		try {
			storeService.moveDirectory(dir2.getDirId(), dir7.getDirId(), true);
		} catch (FsServiceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}		
		
		logger.info("Tree after:");
		try {
			logger.info(storeService.printTree(fileStore.getRootDir().getDirId()));
		} catch (FsServiceException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
	
	}
	
	public abstract String getTestFileStorePath();
	
}
