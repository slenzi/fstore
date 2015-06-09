/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
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
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsFileResourceAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1756855144789479319L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;

	/**
	 * 
	 */
	public FsFileResourceAdder() {
		
	}
	
	/**
	 * Add file
	 * 
	 * @param fileToAdd
	 * @param fsDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addFileResource(Path fileToAdd, Long fsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(!Files.exists(fileToAdd)){
			throw new IOException("File does not exist => " + fileToAdd.toString());
		}
		if(Files.isDirectory(fileToAdd)){
			throw new IOException("Path is a directory => " + fileToAdd.toString());
		}
		
		String fileName = fileToAdd.getFileName().toString();
		
		// check if we already have a file with the same name
		
		FsDirectoryResource dirResource = null;
		try {
			dirResource = (FsDirectoryResource) treeRepository.getNodeWithChild(new FsDirectoryResource(fsDirId), 1);
		} catch (Exception e) {
			throw new DatabaseException("Failed to fetch depth-1 resources for path resource, id => " + fsDirId, e);
		}
		if(dirResource != null){
			dirResource.getChildClosure().stream().forEach(closure -> {
				logger.info("Path resource " +
						", id => " + closure.getChildNode().getNodeId() + 
						", name => " + closure.getChildNode().getName() + 
						", type => " + closure.getChildNode().getPathType().getType() +
						", depth => " + closure.getDepth()
						);
			});
		}else{
			logger.error("No path resource for id => " + fsDirId);
		}
		
		FsDirectoryResource parentDir = null;
		try {
			parentDir = fsDirectoryResourceRepository.getDirectoryResourceById(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch parent directory, parent dir id => " + fsDirId, e);
		}
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByDirectoryId(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch resource store for dir id => " + fsDirId, e);
		}	
		
		String fullDirectoryPath = fsResourceHelper.getAbsoluteDirectoryString(store, parentDir);
		
		return null;
	}

}
