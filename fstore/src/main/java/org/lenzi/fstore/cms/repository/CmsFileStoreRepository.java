package org.lenzi.fstore.cms.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore_;
import org.lenzi.fstore.cms.service.FileStoreHelper;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.CollectionUtil;
import org.lenzi.fstore.util.DateUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with cms file store operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class CmsFileStoreRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1355098362744443645L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;
	
	@Autowired
	private FileStoreHelper fileStoreHelper;	
	
	public CmsFileStoreRepository() {
		
		
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
		
		// TODO - bug in pattern matching
		
		// /onetwo/threefour will match on /onetwo/three
		
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
	public CmsFileStore createFileStore(Path storePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		CmsFileStore store = doCreateStore(storePath, name, description, clearIfExists);
		
		return store;
		
	}
	// helper method for create operation
	private CmsFileStore doCreateStore(Path storePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		//logger.info("Creating file store store for path => " + storePath.toString());
		
		// check for existing store paths that will conflict with the new store path
		List<CmsFileStore> conflictingStores = null;
		try {
			conflictingStores = validatePath(storePath);
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
			for(CmsFileStore store : conflictingStores){
				buf.append("Store name: " + store.getName() + ", store path: " + store.getStorePath() + System.getProperty("line.separator"));
			}
			throw new DatabaseException(buf.toString());
		}
 		
		// create root directory for new file store
		CmsDirectory storeRootDir = null;
		try {
			
			storeRootDir = treeRepository.addRootNode(new CmsDirectory(storePath.getFileName().toString(), File.separator));
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create root directory tree node for file store, name => " + 
					name + ", path => " + storePath.toString(), e);
		}
		
		// create new file store and save to db
		CmsFileStore fileStore = new CmsFileStore();
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
			fileStoreHelper.createDirOnFileSystem(storePath, true);
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
		
		//logger.info("Get file store by store id " + storeId);
		
		return getCmsStoreByStoreIdCriteria(storeId);
		
	}
	
	/**
	 * Get the file store for the *root* directory.
	 * 
	 * @param dirId - id of the root dir for the file store
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileStore getCmsStoreByRootDirId(Long dirId) throws DatabaseException {
		
		//logger.info("Get file store by root dir id " + dirId);
		
		return getCmsStoreByRootDirIdCriteria(dirId);
		
	}
	
	/**
	 * Get the file store for the directory (does not have to be a root directory)
	 * 
	 * @param dirId - id of directory which belongs to the file store. This does not have to be an id
	 * 	of a root directory. This can be a child directory deep in the tree. This will walk the tree
	 *  all the way back to the root node to get the file store.
	 *  
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileStore getCmsFileStoreByDirId(Long dirId) throws DatabaseException {
		
		CmsDirectory rootDir = null;
		try {
			rootDir = treeRepository.getRootNode(new CmsDirectory(dirId));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error fetching root directory for dir => " + dirId, e);
		}
		
		CmsFileStore store = null;
		try {
			store = getCmsStoreByRootDirId(rootDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Erro fetching file store by root dir id => " + rootDir.getDirId(), e);
		}
		
		return store;
		
	}	
	
	/**
	 * Criteria query to get CmsFileStore by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws DatabaseException
	 */
	private CmsFileStore getCmsStoreByStoreIdCriteria(Long storeId) throws DatabaseException {
		
		//logger.info("Get file store by store id " + storeId + " criteria");
		
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
	 * Criteria query to get CmsFileStore by the stores root directory.
	 * 
	 * @param dirId - the ID of the store's root directory
	 * @return
	 * @throws DatabaseException
	 */
	private CmsFileStore getCmsStoreByRootDirIdCriteria(Long dirId) throws DatabaseException {
		
		//logger.info("Get store by root dir id " + dirId + " criteria");
		
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

}
