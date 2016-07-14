/**
 * 
 */
package org.lenzi.fstore.test.file2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
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
public abstract class AbstractRemoveFsFileResource extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;
	
	
	public AbstractRemoveFsFileResource() {
		
	}
	
	@Test
	@Rollback(false)
	public void removeFileResource() {
		
		logTestTitle("Remove file resource");
		
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
		} catch (ServiceException e) {
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
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(dirResource1);
		assertNotNull(dirResource2);
		assertNotNull(dirResource2_1);
		assertNotNull(dirResource3);
		
		logger.info("Adding files fore test...");
		
		FsFileMetaResource fileMetaResource1 = null;
		FsFileMetaResource fileMetaResource2 = null;
		try {
			fileMetaResource1 = fsResourceService.addFileResource(sourcePath, dirResource1.getDirId(), true);
			fileMetaResource2 = fsResourceService.addFileResource(sourcePath, dirResource2.getDirId(), true);
		} catch (ServiceException e) {
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
		
		Path filePath1 = fsResourceHelper.getAbsoluteFilePath(store, dirResource1, fileMetaResource1);
		Path filePath2 = fsResourceHelper.getAbsoluteFilePath(store, dirResource2, fileMetaResource2);
		
		assertNotNull(filePath1);
		assertNotNull(filePath2);
		
		assertTrue(Files.exists(filePath1));
		assertTrue(Files.exists(filePath2));
		
		try {
			assertEquals(Files.size(sourcePath), Files.size(filePath1));
			assertEquals(Files.size(sourcePath), Files.size(filePath2));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
			return;
		}
		
		logger.info("File was added at => " + filePath1.toString());
		logger.info("File was added at => " + filePath2.toString());
		
		logger.info("Performing remove operation...");
		
		FsFileMetaResource fetch1 = null;
		FsFileMetaResource fetch2 = null;
		try {
			fetch1 = fsResourceService.getFileResourceById(fileMetaResource1.getFileId(), FsFileResourceFetch.FILE_META);
			fetch2 = fsResourceService.getFileResourceById(fileMetaResource2.getFileId(), FsFileResourceFetch.FILE_META);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
		}
		assertNotNull(fetch1);
		assertNotNull(fetch2);		
		
		try {
			fsResourceService.removeFileResource(fileMetaResource1.getFileId());
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		
		assertFalse(Files.exists(filePath1));
		
		fetch1 = null;
		try {
			fetch1 = fsResourceService.getFileResourceById(fileMetaResource1.getFileId(), FsFileResourceFetch.FILE_META);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error("Error checking if file sizes are the same, " + e.getMessage(), e);
		}
		
		assertNull(fetch1);
		
	}
	
	public abstract String getTestStorePath();

}
