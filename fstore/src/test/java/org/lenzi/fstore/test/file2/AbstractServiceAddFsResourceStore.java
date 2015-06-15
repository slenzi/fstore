/**
 * 
 */
package org.lenzi.fstore.test.file2;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Uses the FsResourceService to create a sample resource store.
 * 
 * @author sal
 */
public abstract class AbstractServiceAddFsResourceStore extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	public AbstractServiceAddFsResourceStore() {
		
	}
	
	@Test
	@Rollback(false)
	public void createResourceStoreFromService() {
		
		Path examplePath = Paths.get(getTestStorePath());
		
		logTestTitle("Creating sample resource store at => " + examplePath.toString());
		
		FsResourceStore fsStore = null;
		
		try {
		
			fsStore = fsResourceService.createSampleResourceStore(examplePath);
			
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
		assertNotNull(fsStore);
		assertNotNull(fsStore.getRootDirectoryResource());
		
		logger.info("Sample resource store was created.");
		
		Tree<FsPathResource> resourceTree = null;
		try {
			resourceTree = fsResourceService.getTree(fsStore.getRootDirectoryResource().getDirId());
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		assertNotNull(resourceTree);
		
		logger.info("Sample Resource Store Tree:\n" + resourceTree.printTree());
		
	}
	
	public abstract String getTestStorePath();

}
