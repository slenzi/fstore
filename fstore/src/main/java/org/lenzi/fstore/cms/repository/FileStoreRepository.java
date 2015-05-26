/**
 * 
 */
package org.lenzi.fstore.cms.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore_;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.repository.tree.query.TreeQueryRepository;
import org.lenzi.fstore.service.ClosureMapBuilder;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.CollectionUtil;
import org.lenzi.fstore.util.DateUtil;
import org.lenzi.fstore.util.FileUtil;

/**
 * Rollbacks by default only happen for unchecked exceptions. In the transaction annotation
 * we add rollbackFor=Throwable.class so rollbacks will happen for checked exceptions as
 * well, e.g., our DatabaseException class.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FileStoreRepository extends AbstractRepository {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;
	
	@Autowired
	private TreeQueryRepository queryRepository;
	
	@Autowired
	private ClosureMapBuilder<CmsDirectory> closureMapBuilder;
	
	@Autowired
	private ClosureLogger<CmsDirectory> closureLogger;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8439120459143189611L;

	/**
	 * 
	 */
	public FileStoreRepository() {
	
	}
	
	/**
	 * Retrieve any file stores whose path is a parent directory of the 'dirPath'
	 * 
	 * File stores cannot be nested. i.e., the store path of one file store cannot
	 * be a sub directory of another file store path.
	 * 
	 * @param dirPath
	 * @return
	 */
	public List<CmsFileStore> getParentFileStores(Path dirPath) throws DatabaseException {
		
		//
		// make sure new path is not a sub directory of a current file store path
		//
		// select f from CmsFileStore as f
		// where '/Users/slenzi/Programming/sample_store/foo' like concat(f.storePath, '%') 
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<CmsFileStore> type = CmsFileStore.class;
		CriteriaQuery<CmsFileStore> query = cb.createQuery(type);
		Root<CmsFileStore> root = query.from(type);
		
		query.select(root);
		query.where(
				cb.like( cb.concat(root.get(CmsFileStore_.storePath), "%"), dirPath.toString() )
				);
		
		List<CmsFileStore> stores = null;
		try {
			stores = getResultList(query);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error checking if any parent stores exists for path " + dirPath.toString(), e);
		}
		
		return stores;
	}
	
	/**
	 * Retrieve any file stores whose path is a child directory of 'dirPath'
	 * 
	 * File stores cannot be nested. i.e., the store path of one file store cannot
	 * be a sub directory of another file store path.
	 * 
	 * @param dirPath
	 * @return
	 */
	public List<CmsFileStore> getChildFileStores(Path dirPath) throws DatabaseException {
		
		//
		// Make sure new path is not a parent dir of a current file store path
		//
		// select f from CmsFileStore as f
		// where f.storePath like concat('/Users/slenzi/Programming', '%')
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<CmsFileStore> type = CmsFileStore.class;
		CriteriaQuery<CmsFileStore> query = cb.createQuery(type);
		Root<CmsFileStore> root = query.from(type);
		
		query.select(root);
		query.where(
				cb.like( root.get(CmsFileStore_.storePath), dirPath.toString() + "%" )
				);
		
		List<CmsFileStore> stores = null;
		try {
			stores = getResultList(query);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error checking if any child stores exists for path " + dirPath.toString(), e);
		}
		
		return stores;
		
	}
	
	/**
	 * Check if there are any existing stores the match the following conditions.
	 * 
	 * 1. have the same store path
	 * 2. existing store path is a child directory of 'dirPath'
	 * 3. existing store path is a parent directory of 'dirPath'
	 * 
	 * When creating a new store, it's path must be unique, and cannot be a child or parent directory of
	 * and existing store path.
	 * 
	 * @param dirPath - the path to check
	 * @return A list of all stores which match any of the three conditions listed above.
	 * @throws DatabaseException
	 */
	private List<CmsFileStore> validatePath(Path dirPath) throws DatabaseException {
		
		List<CmsFileStore> conflictingStores = new ArrayList<CmsFileStore>();
		
		conflictingStores.addAll( CollectionUtil.emptyListIfNull(getParentFileStores(dirPath)) );
		conflictingStores.addAll( CollectionUtil.emptyListIfNull(getChildFileStores(dirPath)) );
		
		List<CmsFileStore> dupFree = conflictingStores.parallelStream().distinct().collect(Collectors.toList());
		
		Collections.sort(dupFree);
		
		return dupFree;
		
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
	 * @param dirPath - path to where all files will be stored
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
	public CmsFileStore createFileStore(Path dirPath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		CmsFileStore store = doCreateStore(dirPath, name, description, clearIfExists);
		
		return store;
		
	}
	// helper method for create operation
	private CmsFileStore doCreateStore(Path dirPath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		logger.info("Creating file store store for path => " + dirPath.toString());
		
		// check for existing store paths that will conflict with the new store path
		List<CmsFileStore> conflictingStores = null;
		try {
			conflictingStores = validatePath(dirPath);
		} catch (Exception e) {
			throw new DatabaseException("Error checking for conflicting store paths.", e);
		}
		if(conflictingStores != null && conflictingStores.size() > 0){
			StringBuffer buf = new StringBuffer();
			buf.append("The following existing files stores conflict with the new file store path " + dirPath.toString() + 
					System.getProperty("line.separator"));
			buf.append("New file store path must not be the same as an existing file store path. Additionally, new path "
					+ "must not be a child directory of an existing store path, and must not be a parent directory of "
					+ "an existing store path." + System.getProperty("line.separator"));
			for(CmsFileStore store : conflictingStores){
				buf.append("Store name: " + store.getName() + ", store path: " + store.getStorePath() + System.getProperty("line.separator"));
			}
			throw new DatabaseException(buf.toString());
		}
 		
		// create root directory for new file store
		CmsDirectory storeRootDir = null;
		try {
			storeRootDir = treeRepository.addRootNode(new CmsDirectory(dirPath.getFileName().toString()));
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create root directory tree node for file store, name => " + 
					name + ", path => " + dirPath.toString(), e);
		}
		
		// create new file store and save to db
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
		
		getEntityManager().flush();
		
		// want to avoid insert operation...
		fileStore.setRootDir(storeRootDir);
		
		try {
			createDirOnFileSystem(dirPath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		return fileStore;		
		
	}
	
	/**
	 * Get file store by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileStore getCmsStoreByStoreId(Long storeId) throws DatabaseException {
		
		logger.info("Get file store by store id " + storeId);
		
		return getCmsStoreByStoreIdCriteria(storeId);
		
	}
	
	/**
	 * Get file store by root dir id
	 * 
	 * @param dirId - id of the root dir for the file store
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileStore getCmsStoreByRootDirId(Long dirId) throws DatabaseException {
		
		logger.info("Get file store by root dir id " + dirId);
		
		return getCmsStoreByRootDirIdCriteria(dirId);
		
	}	
	
	public CmsFileStore getCmsStoreByStoreIdCriteria(Long storeId) throws DatabaseException {
		
		logger.info("Get file store by store id " + storeId + " criteria");
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<CmsFileStore> type = CmsFileStore.class;
		CriteriaQuery<CmsFileStore> query = cb.createQuery(type);
		Root<CmsFileStore> root = query.from(type);
		
		//javax.persistence.criteria.Path<CmsDirectory> rootDir = root.get(CmsFileStore_.rootDir);
		//Join<CmsFileStore,CmsDirectory> rootDirJoin = root.join(CmsFileStore_.rootDir, JoinType.INNER);
		Fetch<CmsFileStore,CmsDirectory> rootDirFetch =  root.fetch(CmsFileStore_.rootDir, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(CmsFileStore_.storeId), storeId) );
		//andPredicates.add( cb.equal(root.get(CmsFileStore_.nodeId), rootDir.get(CmsDirectory_.nodeId)) );
		
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		TypedQuery<CmsFileStore> tquery = getEntityManager().createQuery(query);
		
		return tquery.getSingleResult();
		
	}	
	
	/**
	 * Get a file store with its root directory.
	 * 
	 * @param dirId - the ID of the store's root directory
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileStore getCmsStoreByRootDirIdCriteria(Long dirId) throws DatabaseException {
		
		logger.info("Get store by root dir id " + dirId + " criteria");
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<CmsFileStore> type = CmsFileStore.class;
		CriteriaQuery<CmsFileStore> query = cb.createQuery(type);
		Root<CmsFileStore> root = query.from(type);
		
		//Join<CmsFileStore,CmsDirectory> rootDirJoin = root.join(CmsFileStore_.rootDir, JoinType.LEFT);
		Fetch<CmsFileStore,CmsDirectory> rootDirFetch =  root.fetch(CmsFileStore_.rootDir, JoinType.LEFT);
		
		javax.persistence.criteria.Path<CmsDirectory> rootDir = root.get(CmsFileStore_.rootDir);
		
		query.select(root);
		query.where(
				cb.equal(rootDir.get(CmsDirectory_.nodeId), dirId)
				);
		
		CmsFileStore store = null;
		try {
			store = (CmsFileStore) this.getSingleResult(query);
		} catch (Exception e) {
			throw new DatabaseException("Error retrieving file store for for root dir id => " + dirId);
		}
		
		return store;
		
	}
	
	/**
	 * Add a new directory.
	 * 
	 * @param parentDirId - The parent directory under which the new child directory will be created.
	 * @param dirName - The name of the new directory
	 * @return - reference to the newly created directory object
	 * @throws DatabaseException - if something goes wrong...
	 */
	public CmsDirectory addDirectory(Long parentDirId, String dirName) throws DatabaseException {
		
		if(parentDirId == null){
			throw new DatabaseException("Parent dir id param is null.");
		}
		if(dirName == null){
			throw new DatabaseException("Dir name param is null.");
		}
		
		CmsDirectory dir = null;
		try {
			dir = treeRepository.addChildNode(new CmsDirectory(parentDirId), new CmsDirectory(dirName));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error adding new directory to parent dir => " + parentDirId, e);
		}
		
		String fullPath = null;
		try {
			fullPath = getPath(dir.getNodeId());
		} catch (Exception e) {
			throw new DatabaseException("Failed to get full path to newly created directory => " + dir.getNodeId() + 
					" which would exist under parent directory => " + parentDirId, e);
		}
		Path dirPath = Paths.get(fullPath);
		
		try {
			createDirOnFileSystem(dirPath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		return dir;
		
	}
	
	/**
	 * Works backwards, up the tree to the root node, and builds the full path. Pass true to
	 * include the store path.
	 * 
	 * @param cmsDirId
	 * @return
	 * @throws DatabaseException
	 */
	public String getPath(Long cmsDirId) throws DatabaseException {
		
		logger.info("Getting path for dir => " + cmsDirId);
		
		// get not with parent closure data
		CmsDirectory cmsDir = treeRepository.getNodeWithParent(new CmsDirectory(cmsDirId));
		Set<DBClosure<CmsDirectory>> parentClosure = cmsDir.getParentClosure();
		
		//closureLogger.logClosure(parentClosure);
		
		// create a map from the parent closure data
		HashMap<Long,CmsDirectory> treeMap = closureMapBuilder.buildParentMapFromClosure(parentClosure);
		
		// build ordered list, and reverse
		List<CmsDirectory> childRootList = new ArrayList<CmsDirectory>();
		buildChildToRootOrderedList(cmsDir, treeMap, childRootList);
		Collections.reverse(childRootList);
		
		// build path from list data
		CmsDirectory rootDir = null;
		StringBuffer buf = new StringBuffer();
		for(CmsDirectory dir : childRootList){
			if(rootDir == null){
				// store path includes root dir name, so we do not need to append it to our buffer
				rootDir = dir;
			}else{
				buf.append(File.separator + dir.getDirName());
			}
		}
		String path = buf.toString();
		
		// optionally include the store path
		CmsFileStore store = getCmsStoreByRootDirId(rootDir.getNodeId());
		path = store.getStorePath() + path;
		
		return path;
	}
	private List<CmsDirectory> buildChildToRootOrderedList(CmsDirectory child, HashMap<Long,CmsDirectory> parentMap, List<CmsDirectory> childRootList){
		
		childRootList.add(child);
		
		CmsDirectory parent = null;
		if((parent = parentMap.get(child.getNodeId())) != null){
			buildChildToRootOrderedList(parent, parentMap, childRootList);
		}
		
		return childRootList;
	}

	public void addFile(Path file, Long cmsDirId) throws DatabaseException {
		
		logger.info("Adding file " + file.toString());
		
		// get directory
		CmsDirectory cmsDir = treeRepository.getNodeWithParent(new CmsDirectory(cmsDirId));
		Set<DBClosure<CmsDirectory>> parentClosure = cmsDir.getParentClosure();
		
		// if not a root node, get all parent dir data
		
		// get file store
		
		// build complete destination path on disk
		
	}
	
	private void createDirOnFileSystem(Path path, boolean clearIfExists) throws IOException, SecurityException {
		
		FileUtil.createDirectory(path, clearIfExists);

		boolean canReadWrite = Files.isReadable(path) && Files.isWritable(path);
		if(!canReadWrite){
			throw new SecurityException("Cannot read and write to directory " + path.toString());
		}		
		
	}

}
