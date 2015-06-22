/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
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
 * For adding/creating resource stores.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsResourceStoreAdder extends AbstractRepository {

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
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;

	/**
	 * 
	 */
	public FsResourceStoreAdder() {
		
	}
	
	/**
	 * Create resource store
	 * 
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws DatabaseException
	 */
	public FsResourceStore createResourceStore(Path storePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		return doCreateResourceStore(storePath, name, description, clearIfExists);
		
	}
	// helper method for create operation
	private FsResourceStore doCreateResourceStore(Path storePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		//logger.info("Creating file store store for path => " + storePath.toString());
		
		// check for existing store paths that will conflict with the new store path
		List<FsResourceStore> conflictingStores = null;
		try {
			conflictingStores = fsResourceStoreRepository.validatePath(storePath);
		} catch (Exception e) {
			throw new DatabaseException("Error checking for conflicting store paths.", e);
		}
		if(conflictingStores != null && conflictingStores.size() > 0){
			StringBuffer buf = new StringBuffer();
			buf.append("The following existing files stores conflict with the new file store path " + storePath.toString() + 
					System.getProperty("line.separator"));
			buf.append("New file store path must not be the same as an existing file store path. Additionally, new path "
					+ "must not be a child directory of an existing store path, and must not be a parent directory of "
					+ "an existing store path." + System.getProperty("line.separator"));
			for(FsResourceStore store : conflictingStores){
				buf.append("Store name: " + store.getName() + ", store path: " + store.getStorePath() + System.getProperty("line.separator"));
			}
			throw new DatabaseException(buf.toString());
		}
 		
		//
		// Create root directory for new file store
		//
		FsDirectoryResource storeRootDir = null;
		try {
			storeRootDir = (FsDirectoryResource) treeRepository.addRootNode(
					new FsDirectoryResource(0L, storePath.getFileName().toString(), File.separator));
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create root directory tree node for file store, name => " + 
					name + ", path => " + storePath.toString(), e);
		}
		
		//
		// create new file store and save to db
		//
		FsResourceStore fileStore = new FsResourceStore();
		fileStore.setName(name);
		fileStore.setDescription(description);
		fileStore.setNodeId(storeRootDir.getNodeId());
		fileStore.setStorePath(storePath.toString());
		fileStore.setDateCreated(DateUtil.getCurrentTime());
		fileStore.setDateUpdated(DateUtil.getCurrentTime());
		try {
			persist(fileStore);
		}catch(DatabaseException e){
			throw new DatabaseException("Error saving file store entry to database. ", e);
		}
		getEntityManager().flush();
		

		//
		// Update root dir store id
		//
		try {
			storeRootDir.setStoreId(fileStore.getStoreId());
			merge(storeRootDir);
		}catch(DatabaseException e){
			throw new DatabaseException("Error updating root dir store id. ", e);
		}
		
		// want to avoid insert operation...
		fileStore.setRootDirectory(storeRootDir);
		
		//
		// Create directory on file system
		//
		try {
			fsResourceHelper.createDirOnFileSystem(storePath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		return fileStore;		
		
	}

}
