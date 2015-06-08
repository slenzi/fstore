package org.lenzi.fstore.file.repository;

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

import org.lenzi.fstore.file.repository.model.impl.FsDirectory_;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore_;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.CollectionUtil;
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
 * Repository for dealing with file store operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsFileStoreRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1355098362744443645L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("DirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;
	
	@Autowired
	private FsHelper fsHelper;	
	
	public FsFileStoreRepository() {
		
		
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
	public List<FsFileStore> getParentFileStores(Path dirPath) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsFileStore> type = FsFileStore.class;
		CriteriaQuery<FsFileStore> query = cb.createQuery(type);
		Root<FsFileStore> root = query.from(type);
		
		query.select(root);
		query.where(
				cb.like( cb.concat(root.get(FsFileStore_.storePath), "%"), dirPath.toString() )
				);
		
		List<FsFileStore> stores = null;
		try {
			//stores = getResultList(query);
			stores = ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));
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
	public List<FsFileStore> getChildFileStores(Path dirPath) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsFileStore> type = FsFileStore.class;
		CriteriaQuery<FsFileStore> query = cb.createQuery(type);
		Root<FsFileStore> root = query.from(type);
		
		query.select(root);
		query.where(
				cb.like( root.get(FsFileStore_.storePath), dirPath.toString() + "%" )
				);
		
		List<FsFileStore> stores = null;
		try {
			//stores = getResultList(query);
			stores = ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));
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
	public List<FsFileStore> validatePath(Path dirPath) throws DatabaseException {
		
		// TODO - bug in pattern matching
		
		// /onetwo/threefour will match on /onetwo/three
		
		List<FsFileStore> conflictingStores = new ArrayList<FsFileStore>();
		
		conflictingStores.addAll( CollectionUtil.emptyListIfNull(getParentFileStores(dirPath)) );
		conflictingStores.addAll( CollectionUtil.emptyListIfNull(getChildFileStores(dirPath)) );
		
		List<FsFileStore> dupFree = conflictingStores.parallelStream().distinct().collect(Collectors.toList());
		
		Collections.sort(dupFree);
		
		return dupFree;
		
	}
	
	/**
	 * Get file store by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileStore getFsStoreByStoreId(Long storeId) throws DatabaseException {
		
		//logger.info("Get file store by store id " + storeId);
		
		return getFsStoreByStoreIdCriteria(storeId);
		
	}
	
	/**
	 * Get the file store for the *root* directory.
	 * 
	 * @param dirId - id of the root dir for the file store
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileStore getFsStoreByRootDirId(Long dirId) throws DatabaseException {
		
		//logger.info("Get file store by root dir id " + dirId);
		
		return getFsStoreByRootDirIdCriteria(dirId);
		
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
	public FsFileStore getFsFileStoreByDirId(Long dirId) throws DatabaseException {
		
		FsDirectory rootDir = null;
		try {
			rootDir = treeRepository.getRootNode(new FsDirectory(dirId));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error fetching root directory for dir => " + dirId, e);
		}
		
		FsFileStore store = null;
		try {
			store = getFsStoreByRootDirId(rootDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Erro fetching file store by root dir id => " + rootDir.getDirId(), e);
		}
		
		return store;
		
	}	
	
	/**
	 * Criteria query to get FsFileStore by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws DatabaseException
	 */
	private FsFileStore getFsStoreByStoreIdCriteria(Long storeId) throws DatabaseException {
		
		//logger.info("Get file store by store id " + storeId + " criteria");
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsFileStore> type = FsFileStore.class;
		CriteriaQuery<FsFileStore> query = cb.createQuery(type);
		Root<FsFileStore> root = query.from(type);
		
		//javax.persistence.criteria.Path<CmsDirectory> rootDir = root.get(CmsFileStore_.rootDir);
		//Join<CmsFileStore,CmsDirectory> rootDirJoin = root.join(CmsFileStore_.rootDir, JoinType.INNER);
		Fetch<FsFileStore,FsDirectory> rootDirFetch =  root.fetch(FsFileStore_.rootDir, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsFileStore_.storeId), storeId) );
		//andPredicates.add( cb.equal(root.get(CmsFileStore_.nodeId), rootDir.get(CmsDirectory_.nodeId)) );
		
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		//TypedQuery<CmsFileStore> tquery = getEntityManager().createQuery(query);
		//return tquery.getSingleResult();
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}	
	
	/**
	 * Criteria query to get FsFileStore by the stores root directory.
	 * 
	 * @param dirId - the ID of the store's root directory
	 * @return
	 * @throws DatabaseException
	 */
	private FsFileStore getFsStoreByRootDirIdCriteria(Long dirId) throws DatabaseException {
		
		//logger.info("Get store by root dir id " + dirId + " criteria");
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsFileStore> type = FsFileStore.class;
		CriteriaQuery<FsFileStore> query = cb.createQuery(type);
		Root<FsFileStore> root = query.from(type);
		
		//Join<CmsFileStore,CmsDirectory> rootDirJoin = root.join(CmsFileStore_.rootDir, JoinType.LEFT);
		Fetch<FsFileStore,FsDirectory> rootDirFetch =  root.fetch(FsFileStore_.rootDir, JoinType.LEFT);
		
		javax.persistence.criteria.Path<FsDirectory> rootDir = root.get(FsFileStore_.rootDir);
		
		query.select(root);
		query.where(
				cb.equal(rootDir.get(FsDirectory_.nodeId), dirId)
				);
		
		FsFileStore store = null;
		try {
			//store = (CmsFileStore) this.getSingleResult(query);
			store = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		} catch (Exception e) {
			throw new DatabaseException("Error retrieving file store for for root dir id => " + dirId);
		}
		
		return store;
		
	}	

}
