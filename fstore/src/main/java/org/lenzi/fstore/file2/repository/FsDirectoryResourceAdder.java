/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
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
 * Manager for adding directories
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsDirectoryResourceAdder extends AbstractRepository {

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
	public FsDirectoryResourceAdder() {
		
	}
	
	// TODO - test method. remove.
	/**
	 * @deprecated
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
					new FsDirectoryResource(dirName, File.separator));
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create new root directory resource.", e);
		}
		
		return dirResource;
		
	}
	
	/**
	 * Add directory resource
	 * 
	 * @param parentDirId
	 * @param dirName
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectoryResource addDirectoryResource(Long parentDirId, String dirName) throws DatabaseException {
		
		// TODO - check if parent dir already contains dir with same name
		
		if(parentDirId == null){
			throw new DatabaseException("Parent dir id param is null.");
		}
		if(dirName == null){
			throw new DatabaseException("Dir name param is null.");
		}
		
		FsDirectoryResource parentDir = null;
		try {
			parentDir = fsDirectoryResourceRepository.getDirectoryResourceById(parentDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch parent directory, parent dir id => " + parentDirId, e);
		}
		
		if(parentDir == null){
			throw new DatabaseException("Cannot add new directory => " + dirName + " to parent dir => " + parentDirId + 
					". Failed to fetch parent dir from database. Returned object was null.");
		}
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByDirectoryId(parentDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch resource store for dir id => " + parentDir.getDirId(), e);
		}
		
		return add(parentDir, store, dirName);
	
	}
	
	/**
	 * Add directory resource
	 * 
	 * @param parentDir
	 * @param fsResourceStore
	 * @param dirName
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectoryResource add(FsDirectoryResource parentDir, FsResourceStore fsResourceStore, String dirName) throws DatabaseException {
		
		logger.info("Adding child dir " + dirName + " to parent dir " + parentDir.getName() + " for store " + fsResourceStore.getName());
		
		Path absolutePath   = fsResourceHelper.getAbsolutePath(fsResourceStore, parentDir, dirName);
		String relativePath = fsResourceHelper.getRelativePath(fsResourceStore, parentDir, dirName);
		
		// add new child dir
		logger.info("Child dir path => " + absolutePath.toString());
		
		FsDirectoryResource childDir = null;
		try {
			
			childDir = (FsDirectoryResource) treeRepository.addChildNode(
					parentDir, new FsDirectoryResource(dirName, relativePath) );
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Error adding new directory to parent dir => " + parentDir.getDirId(), e);
		}
		
		try {
			createDirOnFileSystem(absolutePath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		// TODO - check if needed
		getEntityManager().flush();
		
		return childDir;
		
	}
	
	/**
	 * Create directory on file system (plus all parent directories if they don't exist.)
	 * 
	 * @param path - the directory to create
	 * @param clearIfExists - clear the directory if not empty
	 * @throws IOException
	 * @throws SecurityException
	 */
	private void createDirOnFileSystem(Path path, boolean clearIfExists) throws IOException, SecurityException {
		
		FileUtil.createDirectory(path, clearIfExists);

		boolean canReadWrite = Files.isReadable(path) && Files.isWritable(path);
		if(!canReadWrite){
			throw new SecurityException("Cannot read and write to directory " + path.toString());
		}		
		
	}

}
