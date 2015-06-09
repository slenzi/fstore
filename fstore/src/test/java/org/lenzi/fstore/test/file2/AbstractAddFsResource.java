/**
 * 
 */
package org.lenzi.fstore.test.file2;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * @author sal
 */
public abstract class AbstractAddFsResource extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsResourceService fsResourceService;
	
	public AbstractAddFsResource() {
		
	}
	
	@Test
	@Rollback(false)
	public void addRootDirectoryResource() {
		
		logTestTitle("Adding root directory resource");
		
		FsDirectoryResource rootDirResource = null;
		
		try {
			rootDirResource = fsResourceService.addRootDirectory("Sample_directory_resource");
		} catch (FsServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(rootDirResource);
		
		logger.info("Root directory resource => " + rootDirResource);
		
	}
	
	public abstract String getTestFileStorePath();

}
