/**
 * 
 */
package org.lenzi.fstore.test.file2;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * @author sal
 */
public abstract class AbstractAddFsDirectoryResource extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	public AbstractAddFsDirectoryResource() {
		
	}
	
	@Test
	@Rollback(false)
	public void addResourceStore() {
		
		logTestTitle("Adding directory resource");
		
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
		
	}
	
	public abstract String getTestStorePath();

}
