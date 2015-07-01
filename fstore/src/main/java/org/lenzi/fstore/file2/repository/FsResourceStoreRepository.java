package org.lenzi.fstore.file2.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.constants.FsConstants;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.CollectionUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource_;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore_;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with resource store operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsResourceStoreRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1355098362744443645L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;
	
	
	public FsResourceStoreRepository() {
		
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
	public List<FsResourceStore> getParentStores(Path dirPath) throws DatabaseException {
		
		String path = dirPath.toString();
		
		// all paths in database use forward slash
		path = path.replace("\\", FsConstants.FILE_SEPARATOR);
		
		if(!path.endsWith(FsConstants.FILE_SEPARATOR)){
			path += FsConstants.FILE_SEPARATOR;
		}
		
		logger.info("Checking for existing parent file stores for path, " + path);
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsResourceStore> type = FsResourceStore.class;
		CriteriaQuery<FsResourceStore> query = cb.createQuery(type);
		Root<FsResourceStore> root = query.from(type);
		
		query.select(root);
		query.where(
				cb.like( cb.concat(root.get(FsResourceStore_.storePath), FsConstants.FILE_SEPARATOR + "%"), path )
				);
		
		List<FsResourceStore> stores = null;
		try {
			stores = ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error checking if any parent stores exists for path " + path, e);
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
	public List<FsResourceStore> getChildStores(Path dirPath) throws DatabaseException {
		
		String path = dirPath.toString();
		
		path = path.replace("\\", FsConstants.FILE_SEPARATOR);
		
		if(!path.endsWith(FsConstants.FILE_SEPARATOR)){
			path += FsConstants.FILE_SEPARATOR;
		}
		
		logger.info("Checking for existing child file stores for path, " + path);
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsResourceStore> type = FsResourceStore.class;
		CriteriaQuery<FsResourceStore> query = cb.createQuery(type);
		Root<FsResourceStore> root = query.from(type);
		
		query.select(root);
		query.where(
				cb.like( root.get(FsResourceStore_.storePath), path + "%" )
				);
		
		List<FsResourceStore> stores = null;
		try {
			stores = ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error checking if any child stores exists for path " + path, e);
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
	public List<FsResourceStore> validatePath(Path dirPath) throws DatabaseException {
		
		// TODO - bug in pattern matching
		
		// /onetwo/threefour will match on /onetwo/three
		
		List<FsResourceStore> conflictingStores = new ArrayList<FsResourceStore>();
		
		conflictingStores.addAll( CollectionUtil.emptyListIfNull(getParentStores(dirPath)) );
		conflictingStores.addAll( CollectionUtil.emptyListIfNull(getChildStores(dirPath)) );
		
		List<FsResourceStore> dupFree = conflictingStores.parallelStream().distinct().collect(Collectors.toList());
		
		Collections.sort(dupFree);
		
		return dupFree;
		
	}
	
	
	/**
	 * Get all resource stores
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsResourceStore> getAllStores() throws DatabaseException {
		
		return getAllStoresCriteria();
		
	}
	
	/**
	 * Get resuorce store by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws DatabaseException
	 */
	public FsResourceStore getStoreByStoreId(Long storeId) throws DatabaseException {
		
		//logger.info("Get file store by store id " + storeId);
		
		return getStoreByStoreIdCriteria(storeId);
		
	}
	
	/**
	 * Get the file store for the *root* directory.
	 * 
	 * @param dirId - id of the root dir for the file store
	 * @return
	 * @throws DatabaseException
	 */
	public FsResourceStore getStoreByRootDirectoryId(Long dirId) throws DatabaseException {
		
		//logger.info("Get file store by root dir id " + dirId);
		
		return getStoreByRootDirectoryIdCriteria(dirId);
		
	}
	
	/**
	 * Get the file store for the directory (does not have to be a root directory)
	 * 
	 * @deprecated - use getStoreByPathResourceId(Long resourceId) instead
	 * 
	 * @param dirId - id of directory which belongs to the file store. This does not have to be an id
	 * 	of a root directory. This can be a child directory deep in the tree. This will walk the tree
	 *  all the way back to the root node to get the file store.
	 *  
	 * @return
	 * @throws DatabaseException
	 */
	/*
	public FsResourceStore getStoreByDirectoryId(Long dirId) throws DatabaseException {
		
		FsDirectoryResource rootDir = null;
		try {
			rootDir = (FsDirectoryResource) treeRepository.getRootNode(new FsDirectoryResource(dirId));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error fetching store root directory for dir => " + dirId, e);
		}
		
		FsResourceStore store = null;
		try {
			store = getStoreByRootDirectoryId(rootDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Erro fetching file store by root dir id => " + rootDir.getDirId(), e);
		}
		
		return store;
		
	}
	*/
	
	/**
	 * Get store by file id
	 * 
	 * @deprecated - use getStoreByPathResourceId(Long resourceId) instead
	 * @param fileId
	 * @return
	 * @throws DatabaseException
	 */
	/*
	public FsResourceStore getStoreByFileId(Long fileId) throws DatabaseException {
		
		// TODO - test this method
		
		FsDirectoryResource rootDir = null;
		try {
			rootDir = (FsDirectoryResource) treeRepository.getRootNode(new FsFileMetaResource(fileId));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error fetching store root directory for file id => " + fileId, e);
		}
		
		FsResourceStore store = null;
		try {
			store = getStoreByRootDirectoryId(rootDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Erro fetching file store by root dir id => " + rootDir.getDirId(), e);
		}
		
		return store;
		
	}
	*/
	
	/**
	 * Fetch resource store by any path resource, a FsDirectoryResource, or FsFileMetaResource, or any other future
	 * resource that extends from FsPathResource.
	 * 
	 * @param resourceId - the node id / resource id of the FsPathResource.
	 * @return
	 * @throws DatabaseException
	 */
	public FsResourceStore getStoreByPathResourceId(Long resourceId) throws DatabaseException {
		
		// TODO - test this method, if it works you don't need get store by dir id or get store by file id
		
		FsDirectoryResource rootDir = null;
		try {
			rootDir = (FsDirectoryResource) treeRepository.getRootNode(resourceId, FsPathResource.class);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error fetching store root directory for path resource id => " + resourceId, e);
		}
		
		FsResourceStore store = null;
		try {
			store = getStoreByRootDirectoryId(rootDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Erro fetching file store by root dir id => " + rootDir.getDirId(), e);
		}
		
		return store;
		
	}
	
	/**
	 * Criteria query to get all resource stores
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	private List<FsResourceStore> getAllStoresCriteria() throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsResourceStore> type = FsResourceStore.class;
		CriteriaQuery<FsResourceStore> query = cb.createQuery(type);
		Root<FsResourceStore> root = query.from(type);

		root.fetch(FsResourceStore_.rootDirectoryResource, JoinType.LEFT);
		
		query.select(root);
		
		return ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));		
		
	}
	
	/**
	 * Criteria query to get resource store by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws DatabaseException
	 */
	private FsResourceStore getStoreByStoreIdCriteria(Long storeId) throws DatabaseException {
		
		//logger.info("Get file store by store id " + storeId + " criteria");
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsResourceStore> type = FsResourceStore.class;
		CriteriaQuery<FsResourceStore> query = cb.createQuery(type);
		Root<FsResourceStore> root = query.from(type);
		
		//javax.persistence.criteria.Path<CmsDirectory> rootDir = root.get(CmsFileStore_.rootDir);
		//Join<CmsFileStore,CmsDirectory> rootDirJoin = root.join(CmsFileStore_.rootDir, JoinType.INNER);
		Fetch<FsResourceStore,FsDirectoryResource> rootDirFetch =  root.fetch(FsResourceStore_.rootDirectoryResource, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsResourceStore_.storeId), storeId) );
		//andPredicates.add( cb.equal(root.get(CmsFileStore_.nodeId), rootDir.get(CmsDirectory_.nodeId)) );
		
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}	
	
	/**
	 * Criteria query to get FsFileStore by the stores root directory.
	 * 
	 * @param dirId - the ID of the store's root directory
	 * @return
	 * @throws DatabaseException
	 */
	private FsResourceStore getStoreByRootDirectoryIdCriteria(Long dirId) throws DatabaseException {
		
		//logger.info("Get store by root dir id " + dirId + " criteria");
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsResourceStore> type = FsResourceStore.class;
		CriteriaQuery<FsResourceStore> query = cb.createQuery(type);
		Root<FsResourceStore> root = query.from(type);
		
		//Join<CmsFileStore,CmsDirectory> rootDirJoin = root.join(CmsFileStore_.rootDir, JoinType.LEFT);
		Fetch<FsResourceStore,FsDirectoryResource> rootDirFetch =  root.fetch(FsResourceStore_.rootDirectoryResource, JoinType.LEFT);
		
		javax.persistence.criteria.Path<FsDirectoryResource> rootDir = root.get(FsResourceStore_.rootDirectoryResource);
		
		query.select(root);
		query.where(
				cb.equal(rootDir.get(FsDirectoryResource_.nodeId), dirId)
				);
		
		FsResourceStore store = null;
		try {
			store = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		} catch (Exception e) {
			throw new DatabaseException("Error retrieving file store for for root dir id => " + dirId);
		}
		
		return store;
		
	}	

}
