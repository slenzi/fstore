/**
 * 
 */
package org.lenzi.fstore.cms.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.stereotype.InjectLogger;

/**
 * @author sal
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED)
public class FileStoreRepository extends AbstractRepository {

	@InjectLogger
	private Logger logger;
	
	//@Autowired
	//TreeRepository<N> treeRepository;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8439120459143189611L;

	/**
	 * 
	 */
	public FileStoreRepository() {
	
	}
	
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
