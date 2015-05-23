/**
 * 
 */
package org.lenzi.fstore.cms.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.repository.tree.query.TreeQueryRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.DateUtil;

/**
 * @author sal
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED)
public class FileStoreRepository extends AbstractRepository {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;
	
	@Autowired
	private TreeQueryRepository queryRepository;	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8439120459143189611L;

	/**
	 * 
	 */
	public FileStoreRepository() {
	
	}
	
	public List<CmsFileStore> getParentFileStores(Path dirPath){
		
		//
		// make sure new path is not a sub directory of a current file store path
		//
		// select f from CmsFileStore as f
		// where '/Users/slenzi/Programming/sample_store/foo' like concat(f.storePath, '%') 
		
		return null;
	}
	
	public List<CmsFileStore> getChildFileStores(Path dirPath){
		
		//
		// Make sure new path is not a parent dir of a current file store path
		//
		// select f from CmsFileStore as f
		// where f.storePath like concat('/Users/slenzi/Programming', '%')
		
		return null;
	}
	
	/*
	 
	 existing:
	 /a/b/c
	 /e/f
	 
	 ok:
	 /e/g
	 /a/b/d
	 
	 bad: new file store cannot have same path, and path cannot be a subdir of an existing file store.
	 
	 /a       - parent of existing store
	 /a/b     - parent of existing store
	 /a/b/c   - match of existing store
	 /a/b/c/e - child of existing store
	 /e		  - parent of existing store
	 /e/f     - match of existing store
	 /e/f/g   - child of existing store
	 
	 */
	
	/**
	 * Create a new file store
	 * 
	 * @param dirPath - path to where all files will be stored
	 * @param name - name of the file store
	 * @param description - description of the file store
	 * @return a reference to the newly created file store object
	 * @throws DatabaseException
	 */
	public CmsFileStore createFileStore(Path dirPath, String name, String description) throws DatabaseException {
		
		// TODO - make sure new path is not a child directory of a current file store, or a parent directory of a current file store.
		
		logger.info("Creating file store store for path => " + dirPath.toString());
		
		CmsDirectory storeRootDir = null;
		try {
			storeRootDir = treeRepository.addRootNode(new CmsDirectory(dirPath.getFileName().toString()));
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create root directory tree node for file store, name => " + 
					name + ", path => " + dirPath.toString(), e);
		}
		
		logger.info("Created CmsDirectory for store root dir");
		logger.info(storeRootDir.toString());
		
		CmsFileStore fileStore = new CmsFileStore();
		fileStore.setName(name);
		fileStore.setDescription(description);
		fileStore.setNodeId(storeRootDir.getNodeId());
		fileStore.setStorePath(dirPath.toString());
		fileStore.setDateCreated(DateUtil.getCurrentTime());
		fileStore.setDateUpdated(DateUtil.getCurrentTime());
		
		try {
			persist(fileStore);
		}catch(DatabaseException e){
			throw new DatabaseException("Error saving file store entry to database. ", e);
		}
		
		logger.info("File store created in db");
		logger.info(fileStore.toString());
		
		//
		// throw a database exception if anything else fails. That will cause a rollback to
		// occur so that database isn't out of sync with the file system.
		//
		
		//if(Files.exists(dirPath)){
		//	throw new DatabaseException("Cannot create new file store. Path " + dirPath.toString() + " already exists.");
		//}
		
		try {
			Files.createDirectories(dirPath);
		} catch (IOException e) {
			throw new DatabaseException("Failed to create directory " + dirPath.toString(), e);
		}
		
		logger.info("Created path on local file system => " + dirPath.toString());
		
		boolean canReadWrite = Files.isReadable(dirPath) && Files.isWritable(dirPath);
		if(!canReadWrite){
			throw new DatabaseException("Cannot read and write to directory " + dirPath.toString());
		}
		
		logger.info("Read and write permissions look OK!");
		logger.info("Done!");
		
		return fileStore;
	}



}
