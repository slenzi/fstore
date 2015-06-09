package org.lenzi.fstore.file2.service;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.lenzi.fstore.file2.repository.FsResourceAdder;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
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
	private FsResourceAdder fsResourcAdder;
	
	public FsResourceService() {
		
	}
	
	/**
	 * Add directory 
	 * 
	 * @param dirName
	 * @return
	 * @throws FsServiceException
	 */
	public FsDirectoryResource addRootDirectory(String dirName) throws FsServiceException {
		
		FsDirectoryResource fsDirRes = null;
		
		try {
			fsDirRes = fsResourcAdder.addRootDirectoryResource(dirName);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error adding directory",e);
		}
		
		return fsDirRes;
		
	}
	
	public FsDirectoryResource addChildDirectory(Long parentDirId, String name) throws FsServiceException {
		
		return null;
		
	}

}
