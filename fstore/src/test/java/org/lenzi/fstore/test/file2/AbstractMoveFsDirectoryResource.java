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
import java.util.List;

import org.junit.Test;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
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
public abstract class AbstractMoveFsDirectoryResource extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;
	
	
	public AbstractMoveFsDirectoryResource() {
		
	}
	
	@Test
	@Rollback(false)
	public void moveDirectoryResource() {
		
		logTestTitle("Move directory resource");
		
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
		
		assertNotNull(filePaths);
		assertTrue( filePaths.size() >= 9);
	
		FsResourceStore store = null;
		Path storePath = Paths.get(getTestStorePath());	
		try {
			store = fsResourceService.createResourceStore(storePath, "Sample Resource Store", 
					"Sample resource store description", true);
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
		FsDirectoryResource dirResource2_2 = null;
		FsDirectoryResource dirResource2_3 = null;
		FsDirectoryResource dirResource2_3_1 = null;
		FsDirectoryResource dirResource2_3_2 = null;
		
		try {
			dirResource1 = fsResourceService.addDirectoryResource(store.getRootDirectoryResource().getDirId(), "Sample directory 1");
			dirResource2 = fsResourceService.addDirectoryResource(store.getRootDirectoryResource().getDirId(), "Sample directory 2");
				dirResource2_1 = fsResourceService.addDirectoryResource(dirResource2.getDirId(), "Sample directory 2_1");
				dirResource2_2 = fsResourceService.addDirectoryResource(dirResource2.getDirId(), "Sample directory 2_2");
				dirResource2_3 = fsResourceService.addDirectoryResource(dirResource2.getDirId(), "Sample directory 2_3");
					dirResource2_3_1 = fsResourceService.addDirectoryResource(dirResource2_3.getDirId(), "Sample directory 2_3_1");
					dirResource2_3_2 = fsResourceService.addDirectoryResource(dirResource2_3.getDirId(), "Sample directory 2_3_2");
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(dirResource1);
		assertNotNull(dirResource2);
		assertNotNull(dirResource2_1);
		assertNotNull(dirResource2_2);
		assertNotNull(dirResource2_3);
		assertNotNull(dirResource2_3_1);
		assertNotNull(dirResource2_3_2);
		
		logger.info("Adding files...");
	
		List<FsFileMetaResource> fileMetaResources1 = null;
		List<FsFileMetaResource> fileMetaResources2 = null;
		List<FsFileMetaResource> fileMetaResources3 = null;
		try {
			fileMetaResources1 = fsResourceService.addFileResource(filePaths.subList(0, 3), dirResource2_2.getDirId(), true);
			fileMetaResources2 = fsResourceService.addFileResource(filePaths.subList(3, 6), dirResource2_3_1.getDirId(), true);
			fileMetaResources3 = fsResourceService.addFileResource(filePaths.subList(6, 9), dirResource2_3_2.getDirId(), true);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(fileMetaResources1);
		assertNotNull(fileMetaResources2);
		assertNotNull(fileMetaResources3);
		
		assertEquals(fileMetaResources1.size(), 3);
		assertEquals(fileMetaResources2.size(), 3);
		assertEquals(fileMetaResources3.size(), 3);
		
		final FsResourceStore finalStore = store;
		final FsDirectoryResource finalDirResource2_2 = dirResource2_2;
		final FsDirectoryResource finalDirResource2_3_1 = dirResource2_3_1;
		final FsDirectoryResource finalDirResource2_3_2 = dirResource2_3_2;
		
		fileMetaResources1.stream().forEach(meta -> {
			assertNotNull(meta.getFileResource());
			assertNotNull(meta.getFileResource().getFileMetaResource());
			assertNotNull(meta.getFileResource().getFileData());
			Path filePath = fsResourceHelper.getAbsoluteFilePath(finalStore, finalDirResource2_2, meta);
			assertNotNull(filePath);
			assertTrue(Files.exists(filePath));		
			logger.info("File was added at => " + filePath.toString());
		});
		
		fileMetaResources2.stream().forEach(meta -> {
			assertNotNull(meta.getFileResource());
			assertNotNull(meta.getFileResource().getFileMetaResource());
			assertNotNull(meta.getFileResource().getFileData());
			Path filePath = fsResourceHelper.getAbsoluteFilePath(finalStore, finalDirResource2_3_1, meta);
			assertNotNull(filePath);
			assertTrue(Files.exists(filePath));
			logger.info("File was added at => " + filePath.toString());
		});
		
		fileMetaResources3.stream().forEach(meta -> {
			assertNotNull(meta.getFileResource());
			assertNotNull(meta.getFileResource().getFileMetaResource());
			assertNotNull(meta.getFileResource().getFileData());
			Path filePath = fsResourceHelper.getAbsoluteFilePath(finalStore, finalDirResource2_3_2, meta);
			assertNotNull(filePath);
			assertTrue(Files.exists(filePath));
			logger.info("File was added at => " + filePath.toString());
		});
		
		Tree<FsPathResource> resourceTree = null;
		try {
			resourceTree = fsResourceService.getTree(store.getRootDirectoryResource().getDirId());
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		assertNotNull(resourceTree);
		
		logger.info("Tree before move test:\n" + resourceTree.printTree());
		
		logger.info("Peforming move directory test (does not involve replace/merge)...");
		
		try {
			fsResourceService.moveDirectoryResource(dirResource2.getDirId(), dirResource1.getDirId(), true);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		resourceTree = null;
		try {
			resourceTree = fsResourceService.getTree(store.getRootDirectoryResource().getDirId());
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		assertNotNull(resourceTree);
		
		logger.info("Tree after move test:\n" + resourceTree.printTree());		
		
		logger.info("Done");
		
	}
	
	public abstract String getTestStorePath();

}
