/**
 * 
 */
package org.lenzi.fstore.cms.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFile;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFile_;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.repository.tree.query.TreeQueryRepository;
import org.lenzi.fstore.service.ClosureMapBuilder;
import org.lenzi.fstore.service.TreeBuilder;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.tree.Tree;
import org.lenzi.fstore.tree.TreeNodeVisitException;
import org.lenzi.fstore.tree.Trees;
import org.lenzi.fstore.tree.Trees.PrintOption;
import org.lenzi.fstore.tree.Trees.WalkOption;
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
	
	@Autowired
	private TreeBuilder<CmsDirectory> treeBuilder;
	
	/**
	 * When fetching a CmsDirectory, specify which file data to fetch.
	 */
	public enum CmsDirectoryFetch {
		
		// just directory meta, no file entries
		FILE_NONE,		
		
		// just meta data for each file
		FILE_META,
		
		// meta data and byte data
		FILE_META_WITH_DATA
		
	}
	
	/**
	 * When fetching a CmsFileEntry, specify which file data to fetch.
	 */
	public enum CmsFileEntryFetch {
		
		// just meta data for each file
		FILE_META,
		
		// meta data, and directory
		FILE_META_WITH_DIR,
		
		// meta data and byte data
		FILE_META_WITH_DATA,
		
		// meta data, plus file byte data, plus directory
		FILE_META_WITH_DATA_AND_DIR
		
	}
	
	/**
	 * When fetching a CmsFileEntry, specify which file data to fetch.
	 */
	public enum CmsFileFetch {
		
		// just the CmsFile data
		FILE_DATA,
		
		// CmsFile data plus associated CmsFileEntry meta data
		FILE_DATA_WITH_META,
		
	}	
	
	private static final long serialVersionUID = 8439120459143189611L;

	
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
		
		logger.info("Creating file store store for path => " + storePath.toString());
		
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
			createDirOnFileSystem(storePath, true);
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
	 * Get the file store for the *root* directory.
	 * 
	 * @param dirId - id of the root dir for the file store
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileStore getCmsStoreByRootDirId(Long dirId) throws DatabaseException {
		
		logger.info("Get file store by root dir id " + dirId);
		
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
	 * Criteria query to get CmsFileStore by the stores root directory.
	 * 
	 * @param dirId - the ID of the store's root directory
	 * @return
	 * @throws DatabaseException
	 */
	private CmsFileStore getCmsStoreByRootDirIdCriteria(Long dirId) throws DatabaseException {
		
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
		
		// get parent dir
		CmsDirectory parentDir = null;
		try {
			parentDir = getCmsDirectoryById(parentDirId, CmsDirectoryFetch.FILE_NONE);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch parent cms directory.", e);
		}
		
		// get file store
		CmsFileStore store = null;
		try {
			store = getCmsFileStoreByDirId(parentDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + parentDir.getDirId(), e);
		}
		
		logger.info("Adding child dir " + dirName + " to parent dir " + parentDir.getName() + " for store " + store.getName());
		
		// CmsDirectory.getRelativeDirPath() returns a path relative to the store path
		Path storePath = Paths.get(store.getStorePath());
		Path childPath =  Paths.get(store.getStorePath() + parentDir.getRelativeDirPath() + File.separator + dirName);
		Path childRelativePath = storePath.relativize(childPath);
		String sChildRelativePath = childRelativePath.toString();
		if(!sChildRelativePath.startsWith(File.separator)){
			sChildRelativePath = File.separator + sChildRelativePath;
		}
		
		// add new child dir
		logger.info("Child dir path => " + childPath.toString());
		CmsDirectory childDir = null;
		try {
			
			childDir = treeRepository.addChildNode(parentDir, new CmsDirectory(dirName, sChildRelativePath) );
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Error adding new directory to parent dir => " + parentDirId, e);
		}
		
		try {
			createDirOnFileSystem(childPath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		return childDir;
		
	}
	
	/**
	 * Works backwards up the tree to the root node, and builds the full path. Pass true to
	 * include the store path.
	 * 
	 * @param cmsDirId - id of the cms directory (tree node)
	 * @return
	 * @throws DatabaseException
	 * 
	 * @deprecated - does not work.
	 */
	public String getWalkPath(Long cmsDirId) throws DatabaseException {
		
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
		// after reverse, first node in list is the root dir
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
		CmsFileStore store = getCmsStoreByRootDirId(rootDir.getDirId());
		path = store.getStorePath() + path;
		
		return path;
		
	}
	// builds list of cms directory nodes. first node in returned list is the child most node, and last 
	// node in returned list is the parent most node (root node)
	private List<CmsDirectory> buildChildToRootOrderedList(CmsDirectory child, HashMap<Long,CmsDirectory> parentMap, List<CmsDirectory> childRootList){
		
		childRootList.add(child);
		
		CmsDirectory parent = null;
		if((parent = parentMap.get(child.getDirId())) != null){
			buildChildToRootOrderedList(parent, parentMap, childRootList);
		}
		
		return childRootList;
		
	}
	
	/**
	 * Fetch a cms directory by a file id.
	 * 
	 * @param fileId - id of the file in the directory
	 * @param fetch - specify what to fetch for the cms directory
	 * @return the cms directory entry that the file is in.
	 * @throws DatabaseException
	 */
	public CmsDirectory getCmsDirectoryByFileId(Long fileId, CmsDirectoryFetch fetch) throws DatabaseException {
		
		// TODO - check if this returns all file entries, or just the one with the specified file id
		
		/*
	 	select d from CmsDirectory as d
		join fetch d.fileEntries e
		where e.fileId = 1
		*/
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<CmsDirectory> query = criteriaBuilder.createQuery(CmsDirectory.class);
		Root<CmsDirectory> root = query.from(CmsDirectory.class);
		
		SetJoin<CmsDirectory,CmsFileEntry> fileEntries = root.join(CmsDirectory_.fileEntries, JoinType.LEFT);
		
		switch(fetch){
		
			// just directory meta, no join
			case FILE_NONE:
				break;
		
			// just file meta data
			case FILE_META:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
			
			// file meta data, and file byte data
			case FILE_META_WITH_DATA:
				Fetch<CmsDirectory,CmsFileEntry> metaFetch = root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				metaFetch.fetch(CmsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just file meta data
			default:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
				
		}		
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(fileEntries.get(CmsFileEntry_.fileId), fileId)
				);
		
		CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		
		return result;
		
	}

	/**
	 * Fetch a CmsDirectory
	 * 
	 * @param dirId - directory (node) id
	 * @param fetch - specify which file data to fetch for the directory
	 * @return
	 * @throws DatabaseException
	 */
	public CmsDirectory getCmsDirectoryById(Long dirId, CmsDirectoryFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<CmsDirectory> query = criteriaBuilder.createQuery(CmsDirectory.class);
		Root<CmsDirectory> root = query.from(CmsDirectory.class);		
		
		switch(fetch){
		
			// just directory meta, no join
			case FILE_NONE:
				break;
		
			// just file meta data
			case FILE_META:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
			
			// file meta data, and file byte data
			case FILE_META_WITH_DATA:
				Fetch<CmsDirectory,CmsFileEntry> metaFetch = root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				metaFetch.fetch(CmsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just file meta data
			default:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
				
		}
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(CmsDirectory_.nodeId), dirId)
				);
		
		CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		
		return result;		
		
	}
	
	/**
	 * Fetch a CmsFileEntry
	 * 
	 * @param fileId - file entry id
	 * @param fetch - specify which file data to fetch for the entry, just meta data or also CmsFile which includes byte data
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileEntry getCmsFileEntryById(Long fileId, CmsFileEntryFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<CmsFileEntry> query = criteriaBuilder.createQuery(CmsFileEntry.class);
		Root<CmsFileEntry> root = query.from(CmsFileEntry.class);		
		
		switch(fetch){
		
			// just meta data, no join
			case FILE_META:
				break;
				
			case FILE_META_WITH_DIR:
				root.fetch(CmsFileEntry_.directory, JoinType.LEFT);
				break;
			
			// include CmsFile with byte data
			case FILE_META_WITH_DATA:
				root.fetch(CmsFileEntry_.file, JoinType.LEFT);
				break;
				
			case FILE_META_WITH_DATA_AND_DIR:
				root.fetch(CmsFileEntry_.directory, JoinType.LEFT);
				root.fetch(CmsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just meta data, no join
			default:
				break;
			
		}
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(CmsFileEntry_.fileId), fileId)
				);
		
		CmsFileEntry result = getEntityManager().createQuery(query).getSingleResult();
		
		return result;		
		
	}
	
	/**
	 * Fetch a CmsFile object
	 * 
	 * @param fileId - the file id
	 * @param fetch - specify what to fetch along with the cms file data
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFile getCmsFileById(Long fileId, CmsFileFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<CmsFile> query = criteriaBuilder.createQuery(CmsFile.class);
		Root<CmsFile> root = query.from(CmsFile.class);	
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(CmsFile_.fileId), fileId)
				);
		
		switch(fetch){
		
			// just CmsFile data
			case FILE_DATA:
				break;
			
			// CmsFile data plus associates CmsFileEntry meta
			case FILE_DATA_WITH_META:
				root.fetch(CmsFile_.fileEntry, JoinType.LEFT);
				break;
			
			// default to just meta data, no join
			default:
				break;
		
		}		
		
		CmsFile result = getEntityManager().createQuery(query).getSingleResult();
		
		return result;
	}
	
	/**
	 * Add file, or replace existing file.
	 * 
	 * @param fileToAdd - the file to add
	 * @param cmsDirId - id of the cms directory where file is to be added
	 * @param replaceExisting - true to replace existing file, false not to. If file already exists, and 'replaceExisting'
	 * 	is set to false, a DatabaseException will be thrown.
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public CmsFileEntry addFile(Path fileToAdd, Long cmsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(!Files.exists(fileToAdd)){
			throw new IOException("File does not exist => " + fileToAdd.toString());
		}
		if(Files.isDirectory(fileToAdd)){
			throw new IOException("Path is a directory => " + fileToAdd.toString());
		}
		
		String fileName = fileToAdd.getFileName().toString();
		
		// get parent dir
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = getCmsDirectoryById(cmsDirId, CmsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve CmsDirectory for cms dir id => " + cmsDirId, e);
		}
		
		// get file store
		CmsFileStore cmsStore = null;
		try {
			cmsStore = getCmsFileStoreByDirId(cmsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirId, e);
		}
		
		String dirFullPath = getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
		// check if there is an existing file with the same name
		CmsFileEntry existingCmsFileEntry = cmsDirectory.getEntryByFileName(fileName, false);
		
		// file exists, but we are not to replace file. throw error 
		if(existingCmsFileEntry != null && !replaceExisting){
		
			throw new DatabaseException("File " + fileName + " already exists in cms directory " + cmsDirectory.getName() + 
					" at path " + dirFullPath + ". Cannot replace existing file because 'replaceExisting' param is false.");
		
		// file exists, and we need to replace existing one
		}else if(existingCmsFileEntry != null && replaceExisting){
		
			return replaceExistingFile(fileToAdd, existingCmsFileEntry, cmsDirectory, cmsStore);
			
		// not existing file. add a new entry
		}else{
			
			return addNewFile(fileToAdd, cmsDirectory, cmsStore);
			
		}
		
	}
	
	/**
	 * Add a list of files, or replace a series of existing files.
	 * 
	 * @param filesToAdd
	 * @param cmsDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public List<CmsFileEntry> addFile(List<Path> filesToAdd, Long cmsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(filesToAdd == null || filesToAdd.size() == 0){
			throw new DatabaseException("Files to add list is null or empty");
		}
		for(Path p : filesToAdd){
			if(!Files.exists(p)){
				throw new IOException("File does not exist => " + p.toString());
			}
			if(Files.isDirectory(p)){
				throw new IOException("Path is a directory => " + p.toString());
			}			
		}
		
		// get parent dir
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = getCmsDirectoryById(cmsDirId, CmsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve CmsDirectory for cms dir id => " + cmsDirId, e);
		}
		
		// get file store
		CmsFileStore cmsStore = null;
		try {
			cmsStore = getCmsFileStoreByDirId(cmsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirId, e);
		}
		
		String dirFullPath = getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
		CmsFileEntry newCmsFileEntry = null;
		List<CmsFileEntry> newCmsFileEntries = new ArrayList<CmsFileEntry>();
		
		for(Path fileToAdd : filesToAdd){
			
			String fileName = fileToAdd.getFileName().toString();
			
			// check if there is an existing file with the same name
			CmsFileEntry existingCmsFileEntry = cmsDirectory.getEntryByFileName(fileName, false);
			
			// file exists, but we are not to replace file. throw error 
			if(existingCmsFileEntry != null && !replaceExisting){
			
				throw new DatabaseException("File " + fileName + " already exists in cms directory " + cmsDirectory.getName() + 
						" at path " + dirFullPath + ". Cannot replace existing file because 'replaceExisting' param is false.");
			
			// file exists, and we need to replace existing one
			}else if(existingCmsFileEntry != null && replaceExisting){
			
				newCmsFileEntry = replaceExistingFile(fileToAdd, existingCmsFileEntry, cmsDirectory, cmsStore);
				
				newCmsFileEntries.add(newCmsFileEntry);
				
			// not existing file. add a new entry
			}else{
				
				newCmsFileEntry = addNewFile(fileToAdd, cmsDirectory, cmsStore);
				
				newCmsFileEntries.add(newCmsFileEntry);
				
			}			
			
		}
		
		return newCmsFileEntries;
		
	}
	
	/**
	 * Replace existing file
	 * 
	 * @param newFile
	 * @param existingCmsFileEntry
	 * @param cmsDirectory
	 * @param cmsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	private CmsFileEntry replaceExistingFile(Path newFile, CmsFileEntry existingCmsFileEntry, CmsDirectory cmsDirectory, CmsFileStore cmsStore) throws DatabaseException, IOException {
		
		Long fileId = existingCmsFileEntry.getFileId();
		String newFileName = newFile.getFileName().toString();
		String oldFileName = existingCmsFileEntry.getFileName();
		Long oldFileSize = existingCmsFileEntry.getFileSize();
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(newFile);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + newFile.toString(), e);
		}	
		
		String dirFullPath = getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		String existingFilePath = getAbsoluteFilePath(cmsStore, cmsDirectory, existingCmsFileEntry);
		
		logger.info("Replacing old file => " + oldFileName + ", size => " + oldFileSize + " bytes , with new file => " + newFileName +
				", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Cms Directory Id => " + cmsDirectory.getDirId() + ", Cms Directory Name => " + cmsDirectory.getName() +
				", File system path => " + dirFullPath);			
		
		// update database
		CmsFile updatedFile = new CmsFile();
		updatedFile.setFileId(fileId);
		updatedFile.setFileData(fileBytes);
		existingCmsFileEntry.setFileName(newFileName);
		existingCmsFileEntry.setFileSize(Files.size(newFile));
		CmsFile updatedCmsFile = (CmsFile)merge(updatedFile);
		CmsFileEntry updatedCmsFileEntry = (CmsFileEntry)merge(existingCmsFileEntry);
		updatedCmsFileEntry.setFile(updatedCmsFile);
		
		// delete old file on disk
		try {
			FileUtil.deletePath(Paths.get(existingFilePath));
		} catch (IOException e) {
			throw new DatabaseException("Could not remove existing file on disk " + existingFilePath);
		}
		
		// add new file on disk
		Path target = Paths.get(dirFullPath + File.separator + newFileName);
		try {
			
			Files.copy(newFile, target);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		}		
		
		return updatedCmsFileEntry;
	}
	
	/**
	 * Add a new file to a CmsDirectory
	 * 
	 * @param fileToAdd
	 * @param cmsDirectory
	 * @param cmsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	private CmsFileEntry addNewFile(Path fileToAdd, CmsDirectory cmsDirectory, CmsFileStore cmsStore) throws DatabaseException, IOException {
		
		String fileName = fileToAdd.getFileName().toString();
		String dirFullPath = getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(fileToAdd);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + fileToAdd.toString(), e);
		}
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Cms Directory Id => " + cmsDirectory.getDirId() + ", Cms Directory Name => " + cmsDirectory.getName() +
				", File system path => " + dirFullPath);	
		
		// create cms file entry for meta data
		CmsFileEntry cmsFileEntry = new CmsFileEntry();
		cmsFileEntry.setDirectory(cmsDirectory);
		cmsFileEntry.setFileName(fileName);
		cmsFileEntry.setFileSize(Files.size(fileToAdd));
		persist(cmsFileEntry);
		getEntityManager().flush();

		// update cms directory with new cms file entry (updates linking table)
		cmsDirectory.addFileEntry(cmsFileEntry);
		cmsDirectory = (CmsDirectory)merge(cmsDirectory);
		
		// create cms file object for file byte data, and persist
		CmsFile cmsFile = new CmsFile();
		cmsFile.setFileId(cmsFileEntry.getFileId());
		cmsFile.setFileData(fileBytes);
		persist(cmsFile);
		getEntityManager().flush();
		
		// make sure objects have all data set before returning
		cmsFileEntry.setDirectory(cmsDirectory);
		cmsFileEntry.setFile(cmsFile);
		cmsFile.setFileEntry(cmsFileEntry);
		
		// copy file to directory for CmsDirectory
		Path target = Paths.get(dirFullPath + File.separator + fileName);
		try {
			
			Files.copy(fileToAdd, target);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		}
		
		// not really needed... just a little extra precaution
		if(!Files.exists(target)){
			throw new IOException("Copy proceeded without error, but file copy does not appear to exists..");
		}
		
		return cmsFileEntry;
		
	}
	
	/**
	 * Remove a file
	 * 
	 * @param fileId - if of the file to remove
	 * @throws DatabaseException
	 */
	public void removeFile(Long fileId) throws DatabaseException {
		
		//CmsFile file = getCmsFileById(fileId, CmsFileFetch.FILE_DATA_WITH_META);
		CmsFileEntry fileEntry = this.getCmsFileEntryById(fileId, CmsFileEntryFetch.FILE_META);
		
		CmsDirectory dir = getCmsDirectoryByFileId(fileId, CmsDirectoryFetch.FILE_NONE);
		CmsFileStore store = getCmsFileStoreByDirId(dir.getDirId());
		
		_removeFile(store, dir, fileEntry);
		
	}
	private void _removeFile(CmsFileStore store, CmsDirectory dir, CmsFileEntry fileEntry) throws DatabaseException {
		
		String fileToDelete = getAbsoluteFilePath(store, dir, fileEntry);		
		
		logger.info("removing file id => " + fileEntry.getFileId() + ", name => " + fileEntry.getFileName() + 
				", path => " + fileToDelete);
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		try {
			
			//remove(new CmsFile(fileEntry.getFileId())); // needed?  we have CASCADE set to ALL
			//remove(fileEntry);
			
			// delete cms file
			CriteriaDelete<CmsFile> cmsFileDelete = cb.createCriteriaDelete(CmsFile.class);
			Root<CmsFile> cmsFileRoot = cmsFileDelete.from(CmsFile.class);
			cmsFileDelete.where(cb.lessThanOrEqualTo(cmsFileRoot.get(CmsFile_.fileId), fileEntry.getFileId()));
			executeUpdate(cmsFileDelete);
			
			// delete cms file entry
			CriteriaDelete<CmsFileEntry> cmsFileEntryDelete = cb.createCriteriaDelete(CmsFileEntry.class);
			Root<CmsFileEntry> cmsFileEntryRoot = cmsFileEntryDelete.from(CmsFileEntry.class);
			cmsFileEntryDelete.where(cb.lessThanOrEqualTo(cmsFileEntryRoot.get(CmsFileEntry_.fileId), fileEntry.getFileId()));
			executeUpdate(cmsFileEntryDelete);
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to remove file from database for file id => " + fileEntry.getFileId(), e);
		}
		
		Path filePath = Paths.get(fileToDelete);
		try {
			FileUtil.deletePath(filePath);
		} catch (IOException e) {
			throw new DatabaseException("Failed to remove file from local file system => " + filePath.toString(), e);
		}		
		
	}
	
	/**
	 * Remove a directory
	 * 
	 * @param dirId
	 * @throws DatabaseException
	 */
	public void removeDirectory(Long dirId) throws DatabaseException {
		
		//
		// build tree
		//
		CmsDirectory parentDir = treeRepository.getNodeWithChild(new CmsDirectory(dirId));
		Tree<CmsDirectory> dirTree = null;
		try {
			dirTree = treeBuilder.buildTree(parentDir);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from CmsDirectory node. Need tree for post-order traversal delete.", e);
		}
		
		logger.info(dirTree.printTree());
		
		CmsDirectory cmsDirectory = getCmsDirectoryById(dirId, CmsDirectoryFetch.FILE_META);
		CmsFileStore cmsStore = getCmsFileStoreByDirId(cmsDirectory.getDirId());
		
		//
		// walk tree in post-order traversal, deleting one directory at a time
		//
		try {
			
			Trees.walkTree(dirTree,
					(treeNode) -> {
						
						CmsDirectory dirToDelete = treeNode.getData();
						
						try {
							_removeDirectory(cmsStore, dirToDelete);
						} catch (DatabaseException e) {
							throw new TreeNodeVisitException(e.getMessage(), e);
						}
						
					},
					WalkOption.POST_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new DatabaseException("Error deleting directory id => " + parentDir.getDirId() + 
					", name => " + parentDir.getDirName(), e);
		}
		
	}
	// TODO - test rollback
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
	private void _removeDirectory(CmsFileStore cmsStore, CmsDirectory dirToDelete) throws DatabaseException {
		
		String dirPath = getAbsoluteDirectoryPath(cmsStore, dirToDelete);
		
		logger.info("Removing directory, id => " + dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName() +
				", path => " + dirPath);
		
		// remove all files in the directory
		for(CmsFileEntry fileEntryToDelete : dirToDelete.getFileEntries()){
			try {
				_removeFile(cmsStore, dirToDelete, fileEntryToDelete);
			} catch (DatabaseException e) {
				throw new DatabaseException("Error deleting CmsFileEntry, file id => " + 
						fileEntryToDelete.getFileId() + ", name => " + fileEntryToDelete.getFileName() +
						", in CmsDirectory, dir id => " + dirToDelete.getDirId() + ", name => " +
						dirToDelete.getDirName(), e);
			}
		}
		
		// remove the directory
		try {
			treeRepository.removeNode(dirToDelete);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error deleting CmsDirectory, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName(), e);
		}
		
		// remove dir on file system
		try {
			FileUtil.deletePath(Paths.get(dirPath));
		} catch (IOException e) {
			throw new DatabaseException("Error deleting physical directory on file system for CmsDirectory, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName() + ", path => " + dirPath, e);
		}
		
	}
	
	/**
	 * Move a file.
	 * 
	 * @param fileId - id of the file entry
	 * @param targetDirId - id of target directory, where file will be moved to.
	 * @param replaceExisting - pass true to replace any existing file with the same name in the target directory,
	 * 	or pass false not to replace. If you pass false, and a file already exists in the target directory, then a
	 * 	database exception will be thrown.
	 * @throws DatabaseException
	 */
	public CmsFileEntry moveFile(Long fileId, Long targetDirId, boolean replaceExisting) throws DatabaseException {

		// get source information
		CmsDirectory sourceDir = getCmsDirectoryByFileId(fileId, CmsDirectoryFetch.FILE_META);
		CmsFileStore sourceStore = getCmsFileStoreByDirId(sourceDir.getDirId());
		CmsFileEntry sourceEntry = sourceDir.getEntryByFileId(fileId);
		
		// get target information
		CmsDirectory targetDir = getCmsDirectoryById(targetDirId, CmsDirectoryFetch.FILE_META);
		CmsFileStore targetStore = getCmsFileStoreByDirId(targetDir.getDirId());
		CmsFileEntry conflictingTargetEntry = targetDir.getEntryByFileName(sourceEntry.getFileName(), false);
		
		String sourceFilePath = getAbsoluteFilePath(sourceStore, sourceDir, sourceEntry);
		String targetFilePath = getAbsoluteFilePath(targetStore, targetDir, sourceEntry); // use source file name
		
		// will be true of we need to replace the existing file in the target directory
		boolean needReplace = conflictingTargetEntry != null ? true : false;
		
		// replace existing file in target dir with file from source dir
		if(needReplace && replaceExisting){
			
			String conflictingTargetFilePath = getAbsoluteFilePath(targetStore, targetDir, conflictingTargetEntry);
			
			return _moveWithReplace(sourceDir, targetDir, sourceEntry, conflictingTargetEntry, 
					Paths.get(sourceFilePath), Paths.get(targetFilePath), Paths.get(conflictingTargetFilePath));
			
		// user specified not to replace, throw database exception
		}else if(needReplace && !replaceExisting){
			
			throw new DatabaseException("Target directory contains a file with the same name, but 'replaceExisting' param "
					+ "was false. Cannot move file to target directory.");
		
		// simply move file to target dir
		}else{
			
			return _moveWithoutReplace(sourceDir, targetDir, sourceEntry, Paths.get(sourceFilePath), Paths.get(targetFilePath));
			
		}
		
	}
	// helper method for move file opertation. used when we need to replace a file with the same name in the target directory
	private CmsFileEntry _moveWithReplace(
			CmsDirectory sourceDir, CmsDirectory targetDir,
			CmsFileEntry sourceEntry, CmsFileEntry conflictingTargetEntry,
			Path sourceFilePath, Path targetFilePath, Path conflictTargetFilePath) throws DatabaseException {
		
		// remove existing entry from target dir, then delete it
		CmsFileEntry entryToRemove = targetDir.removeEntryById(conflictingTargetEntry.getFileId());
		remove(entryToRemove);
		
		// remove entry from source dir, and update
		sourceDir.removeEntryById(sourceEntry.getFileId());
		sourceDir = (CmsDirectory)merge(sourceDir);
		
		// add source entry to new target directory, and update
		targetDir.addFileEntry(sourceEntry);
		sourceEntry.setDirectory(targetDir);
		targetDir = (CmsDirectory)merge(targetDir);
		
		// remove physical conflicting file, and move new file over
		try {
			FileUtil.deletePath(conflictTargetFilePath);
			FileUtil.moveFile(sourceFilePath, targetFilePath);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionMoveError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionMoveError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		}
		
		return sourceEntry;
		
	}
	// helper method for move file operation. used when we don't have to worry about replacing a file with the same name
	private CmsFileEntry _moveWithoutReplace(
			CmsDirectory sourceDir, CmsDirectory targetDir, CmsFileEntry sourceEntry,
			Path sourceFilePath, Path targetFilePath) throws DatabaseException {
		
		// remove entry from source dir
		sourceDir.removeEntryById(sourceEntry.getFileId());
		sourceDir = (CmsDirectory)merge(sourceDir);
		
		// add source entry to new target directory
		targetDir.addFileEntry(sourceEntry);
		sourceEntry.setDirectory(targetDir);
		targetDir = (CmsDirectory)merge(targetDir);
		
		// move file to new directory
		try {
			FileUtil.moveFile(sourceFilePath, targetFilePath);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionMoveError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionMoveError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		}
		
		return sourceEntry;
		
	}
	
	public void moveDirectory(Long dirId, Long newParentDirId) throws DatabaseException {
		
		// make sure not a root directory
		
	}
	
	/**
	 * Joins the CmsStore path and the relative CmsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 */
	public String getAbsoluteDirectoryPath(CmsFileStore cmsStore, CmsDirectory cmsDirectory){
		
		String dirRelativePath = cmsDirectory.getRelativeDirPath();
		if(!dirRelativePath.startsWith(File.separator)){
			dirRelativePath = File.separator + dirRelativePath;
		}
		return cmsStore.getStorePath() + dirRelativePath;		
		
	}
	
	/**
	 * Joins the CmsStore path, relative CmsDirectory path, and CmsFileEntry file name to
	 * get the full/absolute path to the fole on the file system.
	 */
	public String getAbsoluteFilePath(CmsFileStore cmsStore, CmsDirectory cmsDirectory, CmsFileEntry cmsFileEntry){
		
		return getAbsoluteDirectoryPath(cmsStore, cmsDirectory) + File.separator + cmsFileEntry.getFileName();	
		
	}	
	
	/**
	 * Builds a database exception for file copy error process.
	 * 
	 * @param source
	 * @param target
	 * @param directory
	 * @param e
	 * @return
	 */
	private DatabaseException buildDatabaseExceptionCopyError(Path source, Path target, CmsDirectory directory, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error copying source file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Target cms directory, id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}
	
	private DatabaseException buildDatabaseExceptionMoveError(Path source, Path target, CmsDirectory sourceDir, CmsDirectory targetDir, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error moving file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Source cms directory, id => " + sourceDir.getDirId() + ", name => " + sourceDir.getName() + cr);
		buf.append("Target cms directory, id => " + targetDir.getDirId() + ", name => " + targetDir.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
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
	
	/**
	 * Get the full absolute path for a cms directory.
	 * 
	 * @param cmsDirId - id of the cms directory
	 * @return
	 * @throws DatabaseException
	 */
	public String getAbsoluteDirPath(Long cmsDirId) throws DatabaseException {
		
		// get directory
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = getCmsDirectoryById(cmsDirId, CmsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve CmsDirectory", e);
		}
		
		// get file store for directory
		CmsFileStore store = null;
		try {
			store = getCmsFileStoreByDirId(cmsDirectory.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirectory.getDirId(), e);
		}
		
		String path = store.getStorePath() + cmsDirectory.getRelativeDirPath();
		
		return path;
		
	}

}
