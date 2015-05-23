/**
 * 
 */
package org.lenzi.fstore.test.cms;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.FileStoreRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * @author sal
 *
 */
public abstract class AbstractCreateFileStore extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private  FileStoreRepository fileStoreRepository;
	
	public AbstractCreateFileStore() {
		
	}
	
	@Test
	@Rollback(false)
	public void createFileStore() {
		
		Path examplePath = Paths.get(getTestFileStorePath());
		
		logTestTitle("Creating sample file store at => " + examplePath.toString());
		
		CmsFileStore fileStore = null;
		try {
			fileStore = fileStoreRepository.createFileStore(examplePath, "Example File Store", "This is an example file store.");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertNotNull(fileStore);
		
		logger.info("Created new file store:");
		logger.info(fileStore.toString());		
		
	}
	
	public abstract String getTestFileStorePath();

}
