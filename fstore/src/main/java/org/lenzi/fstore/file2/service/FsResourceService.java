package org.lenzi.fstore.file2.service;

import java.nio.file.Path;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.lenzi.fstore.file2.repository.FsDirectoryResourceAdder;
import org.lenzi.fstore.file2.repository.FsResourceStoreAdder;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author sal
 *
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsResourceService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceStoreAdder fsResourceStoreAdder;
	
	@Autowired
	private FsDirectoryResourceAdder fsResourcAdder;
	
	public FsResourceService() {
		
	}
	
	/**
	 * Create new resource store
	 * 
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws FsServiceException
	 */
	public FsResourceStore createResourceStore(Path storePath, String name, String description, boolean clearIfExists) throws FsServiceException {
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreAdder.createResourceStore(storePath, name, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error creating store. " + e.getMessage(), e);
		}
		return store;
		
	}
	
	/**
	 * Add new directory
	 * 
	 * @param parentDirId
	 * @param name
	 * @return
	 * @throws FsServiceException
	 */
	public FsDirectoryResource addDirectoryResource(Long parentDirId, String name) throws FsServiceException {
		
		FsDirectoryResource dirResource = null;
		try {
			dirResource = fsResourcAdder.addDirectoryResource(parentDirId, name);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error adding directory to parent directory, id => " + parentDirId, e);
		}
		return dirResource;
		
	}
	
	// TODO - test method. remove later.
	/**
	 * Add directory 
	 * 
	 * @deprecated - remove
	 * 
	 * @param dirName
	 * @return
	 * @throws FsServiceException
	 */
	public FsDirectoryResource addRootDirectory(String dirName) throws FsServiceException {
		
		FsDirectoryResource dirResource = null;
		
		try {
			dirResource = fsResourcAdder.addRootDirectoryResource(dirName);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error adding root directory", e);
		}
		
		return dirResource;
		
	}

}
