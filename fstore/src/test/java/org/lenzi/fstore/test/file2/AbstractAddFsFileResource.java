/**
 * 
 */
package org.lenzi.fstore.test.file2;

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
public abstract class AbstractAddFsFileResource extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;
	
	
	public AbstractAddFsFileResource() {
		
	}
	
	@Test
	@Rollback(false)
	public void addFileResource() {
		
		logTestTitle("Adding file resource");
		
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
		
		logger.info("Adding files...");
		
		FsFileMetaResource fileMetaResource = null;
		try {
			fileMetaResource = fsResourceService.addFileResource(sourcePath, store.getRootDirectoryResource().getDirId(), true);
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
		logger.info("File was added at => " + filePath.toString());
		
	}
	
	public abstract String getTestStorePath();

}
