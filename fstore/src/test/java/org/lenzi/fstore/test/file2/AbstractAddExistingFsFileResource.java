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
public abstract class AbstractAddExistingFsFileResource extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;
	
	
	public AbstractAddExistingFsFileResource() {
		
	}
	
	@Test
	@Rollback(false)
	public void addExistingFileResource() {
		
		logTestTitle("Adding existing file resource");
		
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
			logger.error("Failed to get file resources for test." + e.getMessage());
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
		
		logger.info("Adding files to test replace...");
		
		FsFileMetaResource fileMetaResource = null;
		try {
			fileMetaResource = fsResourceService.addFileResource(sourcePath1, store.getRootDirectoryResource().getDirId(), true);
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(fileMetaResource);
		assertNotNull(fileMetaResource.getFileResource());
		assertNotNull(fileMetaResource.getFileResource().getFileMetaResource());
		assertNotNull(fileMetaResource.getFileResource().getFileData());
		
		Path filePath = fsResourceHelper.getAbsoluteFilePath(store, store.getRootDirectoryResource(), fileMetaResource);
		assertNotNull(filePath);
		assertTrue(Files.exists(filePath));
		try {
			assertEquals(Files.size(sourcePath1), Files.size(filePath));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
			return;
		}
		logger.info("File was added at => " + filePath.toString());
		
		logger.info("Peform replace by adding file with same name to existing directory...");
		
		
		FsFileMetaResource fileMetaResourceUpdated = null;
		try {
			fileMetaResourceUpdated = fsResourceService.addFileResource(sourcePath2, store.getRootDirectoryResource().getDirId(), true);
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(fileMetaResourceUpdated);
		assertNotNull(fileMetaResourceUpdated.getFileResource());
		assertNotNull(fileMetaResourceUpdated.getFileResource().getFileMetaResource());
		assertNotNull(fileMetaResourceUpdated.getFileResource().getFileData());
		
		Path filePathUpdated = fsResourceHelper.getAbsoluteFilePath(store, store.getRootDirectoryResource(), fileMetaResource);
		assertNotNull(filePathUpdated);
		assertTrue(Files.exists(filePathUpdated));
		try {
			assertEquals(Files.size(sourcePath2), Files.size(filePathUpdated));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
			return;
		}		
		logger.info("File was updated at => " + filePathUpdated.toString());		
		
	}
	
	public abstract String getTestStorePath();

}
