package org.lenzi.fstore.file.repository;

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
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For adding directories
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsDirectoryAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1605812756128470726L;

	@InjectLogger
	private Logger logger;	
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;	
	
	@Autowired
	private FsDirectoryRepository fsDirectoryRepository;
	
	@Autowired
	@Qualifier("DirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;	
	
	public FsDirectoryAdder() {
	
	}
	
	/**
	 * Add a new directory.
	 * 
	 * @param parentDirId - The parent directory under which the new child directory will be created.
	 * @param dirName - The name of the new directory
	 * @return - reference to the newly created directory object
	 * @throws DatabaseException - if something goes wrong...
	 */
	public FsDirectory addDirectory(Long parentDirId, String dirName) throws DatabaseException {
		
		// TODO - check if parent dir already contains dir with same name
		
		if(parentDirId == null){
			throw new DatabaseException("Parent dir id param is null.");
		}
		if(dirName == null){
			throw new DatabaseException("Dir name param is null.");
		}
		
		// get parent dir
		FsDirectory parentDir = null;
		try {
			parentDir = fsDirectoryRepository.getFsDirectoryById(parentDirId, FsDirectoryFetch.FILE_NONE);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch parent directory.", e);
		}
		
		if(parentDir == null){
			throw new DatabaseException("Cannot add new directory => " + dirName + " to parent dir => " + parentDirId + 
					". Failed to fetch parent dir from database. Returned object was null.");
		}
		
		// get file store
		FsFileStore fsFileStore = null;
		try {
			fsFileStore = fsFileStoreRepository.getFsFileStoreByDirId(parentDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for dir id => " + parentDir.getDirId(), e);
		}
		
		return add(parentDir, fsFileStore, dirName);
		
	}
	
	/**
	 * Add directory
	 * 
	 * @param parentDir
	 * @param fsFileStore
	 * @param dirName
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectory add(FsDirectory parentDir, FsFileStore fsFileStore, String dirName) throws DatabaseException {
		
		logger.info("Adding child dir " + dirName + " to parent dir " + parentDir.getName() + " for store " + fsFileStore.getName());
		
		// CmsDirectory.getRelativeDirPath() returns a path relative to the store path
		Path storePath = Paths.get(fsFileStore.getStorePath());
		Path childPath =  Paths.get(fsFileStore.getStorePath() + parentDir.getRelativeDirPath() + File.separator + dirName);
		Path childRelativePath = storePath.relativize(childPath);
		String sChildRelativePath = childRelativePath.toString();
		if(!sChildRelativePath.startsWith(File.separator)){
			sChildRelativePath = File.separator + sChildRelativePath;
		}
		
		// add new child dir
		logger.info("Child dir path => " + childPath.toString());
		FsDirectory childDir = null;
		try {
			
			childDir = treeRepository.addChildNode(parentDir, new FsDirectory(dirName, sChildRelativePath) );
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Error adding new directory to parent dir => " + parentDir.getDirId(), e);
		}
		
		try {
			createDirOnFileSystem(childPath, true);
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
