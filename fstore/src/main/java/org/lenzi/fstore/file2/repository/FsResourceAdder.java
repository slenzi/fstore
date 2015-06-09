/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.File;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsResourceAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7552781703574519309L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsDirectoryResourceRepository fsDirResRepo;

	/**
	 * 
	 */
	public FsResourceAdder() {
		
	}
	
	/**
	 * Add root directory resource
	 * 
	 * @param dirName
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectoryResource addRootDirectoryResource(String dirName) throws DatabaseException {
		
		if(dirName == null){
			throw new DatabaseException("Directory name param is null.");
		}
		
		FsDirectoryResource dirResource = null;
		try {
			
			dirResource = (FsDirectoryResource) treeRepository.addRootNode(
					new FsDirectoryResource(dirName, "Other value"));
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create new root directory resource.", e);
		}
		
		return dirResource;
		
	}

}
