/**
 * 
 */
package org.lenzi.fstore.test.file2;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.core.service.exception.ServiceException;
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
public abstract class AbstractAddFsResourceStore extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	public AbstractAddFsResourceStore() {
		
	}
	
	@Test
	@Rollback(false)
	public void addResourceStore() {
		
		logTestTitle("Adding resource store");
		
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
		
		logger.info("Resource Store => " + store);
		
	}
	
	public abstract String getTestStorePath();

}
