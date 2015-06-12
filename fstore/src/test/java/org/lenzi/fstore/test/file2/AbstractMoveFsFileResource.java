/**
 * 
 */
package org.lenzi.fstore.test.file2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.Rollback;

/**
 * @author sal
 */
public abstract class AbstractMoveFsFileResource extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;
	
	
	public AbstractMoveFsFileResource() {
		
	}
	
	@Test
	@Rollback(false)
	public void moveFileResource() {
		
		logTestTitle("Move file resource");
		
		assertNotNull(resourceLoader);
		
		// get test file for upload to database
		Resource sourceResource1 = resourceLoader.getResource("classpath:image/honey_badger.JPG");
		Resource sourceResource2 = resourceLoader.getResource("classpath:image/other/honey_badger.JPG");
		Path sourcePath1 = null;
		Path sourcePath2 = null;
		try {
			sourcePath1 = Paths.get(sourceResource1.getFile().getAbsolutePath());
			sourcePath2 = Paths.get(sourceResource2.getFile().getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to get file resource." + e.getMessage());
			e.printStackTrace();
		}		
		
		assertNotNull(sourcePath1);
		assertNotNull(sourcePath2);
		
		FsResourceStore store = null;
		
		Path storePath = Paths.get(getTestStorePath());
		
		try {
			store = fsResourceService.createResourceStore(storePath, "Sample Resource Store", "Sample resource store description", true);
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(store);
		assertNotNull(store.getRootDirectoryResource());
		
		logger.info("Resource Store => " + store);
		
		FsDirectoryResource dirResource1   = null;
		FsDirectoryResource dirResource2   = null;
		FsDirectoryResource dirResource2_1 = null;
		FsDirectoryResource dirResource3   = null;
		
		try {
			dirResource1 = fsResourceService.addDirectoryResource(store.getRootDirectoryResource().getDirId(), "Sample directory 1");
			dirResource2 = fsResourceService.addDirectoryResource(store.getRootDirectoryResource().getDirId(), "Sample directory 2");
				dirResource2_1 = fsResourceService.addDirectoryResource(dirResource2.getDirId(), "Sample directory 2_1");
			dirResource3 = fsResourceService.addDirectoryResource(store.getRootDirectoryResource().getDirId(), "Sample directory 3");
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(dirResource1);
		assertNotNull(dirResource2);
		assertNotNull(dirResource2_1);
		assertNotNull(dirResource3);
		
		logger.info("Adding 3 files...");
		
		FsFileMetaResource fileMetaResource1 = null;
		FsFileMetaResource fileMetaResource2 = null;
		FsFileMetaResource fileMetaResource3 = null;
		try {
			fileMetaResource1 = fsResourceService.addFileResource(sourcePath1, store.getRootDirectoryResource().getDirId(), true);
			fileMetaResource2 = fsResourceService.addFileResource(sourcePath1, dirResource1.getDirId(), true);
			fileMetaResource3 = fsResourceService.addFileResource(sourcePath2, dirResource3.getDirId(), true);
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(fileMetaResource1);
		assertNotNull(fileMetaResource1.getFileResource());
		assertNotNull(fileMetaResource1.getFileResource().getFileMetaResource());
		assertNotNull(fileMetaResource1.getFileResource().getFileData());
		
		assertNotNull(fileMetaResource2);
		assertNotNull(fileMetaResource2.getFileResource());
		assertNotNull(fileMetaResource2.getFileResource().getFileMetaResource());
		assertNotNull(fileMetaResource2.getFileResource().getFileData());
		
		assertNotNull(fileMetaResource3);
		assertNotNull(fileMetaResource3.getFileResource());
		assertNotNull(fileMetaResource3.getFileResource().getFileMetaResource());
		assertNotNull(fileMetaResource3.getFileResource().getFileData());		
		
		Path filePath1 = fsResourceHelper.getAbsoluteFilePath(store, store.getRootDirectoryResource(), fileMetaResource1);
		Path filePath2 = fsResourceHelper.getAbsoluteFilePath(store, dirResource1, fileMetaResource2);
		Path filePath3 = fsResourceHelper.getAbsoluteFilePath(store, dirResource3, fileMetaResource3);
		
		assertNotNull(filePath1);
		assertNotNull(filePath2);
		assertNotNull(filePath3);
		assertTrue(Files.exists(filePath1));
		assertTrue(Files.exists(filePath2));
		assertTrue(Files.exists(filePath3));
		try {
			assertEquals(Files.size(sourcePath1), Files.size(filePath1));
			assertEquals(Files.size(sourcePath1), Files.size(filePath2));
			assertEquals(Files.size(sourcePath2), Files.size(filePath3));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
			return;
		}		
		logger.info("File was added at => " + filePath1.toString());
		logger.info("File was added at => " + filePath2.toString());
		logger.info("File was added at => " + filePath3.toString());
		
		logger.info("Peforming 1 move operations (does not involve a replace)...");
		
		FsFileMetaResource moveFileMetaResource1 = null;
		try {
			
			moveFileMetaResource1 = fsResourceService.moveFileResource(fileMetaResource1.getFileId(), dirResource2_1.getDirId(), true);
			
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(moveFileMetaResource1);
		//assertNotNull(moveFileMetaResource1.getFileResource());
		//assertNotNull(moveFileMetaResource1.getFileResource().getFileMetaResource());
		//assertNotNull(moveFileMetaResource1.getFileResource().getFileData());	
		
		Path moveFilePath1 = fsResourceHelper.getAbsoluteFilePath(store, dirResource2_1, moveFileMetaResource1);
		assertNotNull(moveFilePath1);
		assertTrue(Files.exists(moveFilePath1));
		try {
			assertEquals(Files.size(sourcePath1), Files.size(moveFilePath1));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
			return;
		}
		logger.info("File was moved to => " + moveFilePath1.toString());
	
		logger.info("Peforming one move operation that involves needing to replace existing file...");
		
		FsFileMetaResource moveFileMetaResource3 = null;
		try {
			
			moveFileMetaResource3 = fsResourceService.moveFileResource(fileMetaResource3.getFileId(), dirResource1.getDirId(), true);
			
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(moveFileMetaResource3);
		//assertNotNull(moveFileMetaResource3.getFileResource());
		//assertNotNull(moveFileMetaResource3.getFileResource().getFileMetaResource());
		//assertNotNull(moveFileMetaResource3.getFileResource().getFileData());
		
		Path moveFilePath3 = fsResourceHelper.getAbsoluteFilePath(store, dirResource1, moveFileMetaResource3);
		assertNotNull(moveFilePath3);
		assertTrue(Files.exists(moveFilePath3));
		try {
			assertEquals(Files.size(sourcePath2), Files.size(moveFilePath3));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
			return;
		}
		logger.info("File was moved to => " + moveFilePath3.toString());		
		
	}
	
	public abstract String getTestStorePath();

}
