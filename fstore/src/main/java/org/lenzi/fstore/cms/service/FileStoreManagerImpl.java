/**
 * 
 */
package org.lenzi.fstore.cms.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.service.exception.ServiceException;

/**
 * @author sal
 *
 */
@Service
@Transactional(propagation=Propagation.REQUIRED)
public class FileStoreManagerImpl implements FileStoreManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8439120459143189611L;

	/**
	 * 
	 */
	public FileStoreManagerImpl() {
	
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.cms.service.FileStoreManager#createFileStore(java.nio.file.Path, java.lang.String, java.lang.String)
	 */
	@Override
	public CmsFileStore createFileStore(Path dirPath, String name, String description) throws ServiceException {
		
		// TODO - make sure path is not under any under file store path.
		
		if(Files.exists(dirPath)){
			throw new ServiceException("Cannot create new file store. Path " + dirPath.toString() + " already exists.");
		}
		
		try {
			Files.createDirectories(dirPath);
		} catch (IOException e) {
			throw new ServiceException("Failed to create directory " + dirPath.toString(), e);
		}
		
		boolean canReadWrite = Files.isReadable(dirPath) && Files.isWritable(dirPath);
		if(!canReadWrite){
			throw new ServiceException("Cannot read and write to directory " + dirPath.toString());
		}
		
		return null;
	}



}
