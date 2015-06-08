package org.lenzi.fstore.file.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For creating file stores
 * 
 * @author slenzi
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsFileStoreAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6005092212906356698L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("DirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;
	
	@Autowired
	private FsHelper fsHelper;	

	public FsFileStoreAdder() {
		
	}
	
	/**
	 * Create a new file store
	 * 
	 * existing store path:
	 * /a/b/c
	 * /e/f
	 * 
	 * good new store paths:
	 * /e/g
	 * /a/b/d
	 * 
	 * bad new store paths: new file store cannot have same path, and path cannot be a sub dir of an existing file store.
	 * /a       - parent of existing store
	 * /a/b     - parent of existing store
	 * /a/b/c   - match of existing store
	 * /a/b/c/e - child of existing store
	 * /e		- parent of existing store
	 * /e/f     - match of existing store
	 * /e/f/g   - child of existing store
	 * 
	 * @param storePath - path to where all files will be stored
	 * @param name - name of the file store
	 * @param description - description of the file store
	 * @param clearIfExists - if the 'dirPath' currently exists on the file system, and contains files, pass true to wipe
	 * 	everything in the directory. If you pass false, and the directory contains files, a DatabaseException will be thrown.
	 * 
	 * @return a reference to the newly created file store object
	 * 
	 * @throws DatabaseException - If the 'dirPath' exists on the file system and contains files, and 'clearIfExists' is false.
	 * 	Also throws a DatabaseException if data cannot be persisted.
	 */
	public FsFileStore createFileStore(Path storePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		FsFileStore store = doCreateStore(storePath, name, description, clearIfExists);
		
		return store;
		
	}
	// helper method for create operation
	private FsFileStore doCreateStore(Path storePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		//logger.info("Creating file store store for path => " + storePath.toString());
		
		// check for existing store paths that will conflict with the new store path
		List<FsFileStore> conflictingStores = null;
		try {
			conflictingStores = fsFileStoreRepository.validatePath(storePath);
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
			for(FsFileStore store : conflictingStores){
				buf.append("Store name: " + store.getName() + ", store path: " + store.getStorePath() + System.getProperty("line.separator"));
			}
			throw new DatabaseException(buf.toString());
		}
 		
		// create root directory for new file store
		FsDirectory storeRootDir = null;
		try {
			
			storeRootDir = treeRepository.addRootNode(new FsDirectory(storePath.getFileName().toString(), File.separator));
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create root directory tree node for file store, name => " + 
					name + ", path => " + storePath.toString(), e);
		}
		
		// create new file store and save to db
		FsFileStore fileStore = new FsFileStore();
		fileStore.setName(name);
		fileStore.setDescription(description);
		fileStore.setNodeId(storeRootDir.getDirId());
		fileStore.setStorePath(storePath.toString());
		fileStore.setDateCreated(DateUtil.getCurrentTime());
		fileStore.setDateUpdated(DateUtil.getCurrentTime());
		
		try {
			persist(fileStore);
		}catch(DatabaseException e){
			throw new DatabaseException("Error saving file store entry to database. ", e);
		}
		
		getEntityManager().flush();
		
		// want to avoid insert operation...
		fileStore.setRootDir(storeRootDir);
		
		try {
			fsHelper.createDirOnFileSystem(storePath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		return fileStore;		
		
	}

}
