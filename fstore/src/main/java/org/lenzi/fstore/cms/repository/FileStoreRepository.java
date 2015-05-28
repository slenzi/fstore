/**
 * 
 */
package org.lenzi.fstore.cms.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.lenzi.fstore.cms.repository.model.impl.CmsFile;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry_;
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
		
		// meta data and byte data
		FILE_META_WITH_DATA,
		
		// meta data, plus file byte data, plus directory
		FILE_META_WITH_DATA_AND_DIR
		
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
	 * Add file to directory
	 * 
	 * @param file - file to add to the cms directory
	 * @param cmsDirId - id of the cms directory
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public CmsFileEntry addFile(Path file, Long cmsDirId) throws DatabaseException, IOException {
		
		logger.info("Adding file " + file.toString());
		
		if(!Files.exists(file)){
			throw new IOException("File does not exist => " + file.toString());
		}
		if(Files.isDirectory(file)){
			throw new IOException("Path is a directory => " + file.toString());
		}
		
		// get parent dir
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = getCmsDirectoryById(cmsDirId, CmsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve CmsDirectory", e);
		}
		
		// get file store
		CmsFileStore store = null;
		try {
			store = getCmsFileStoreByDirId(cmsDirectory.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirectory.getDirId(), e);
		}		
		
		// get full path to directory using relative path from CmsDirectory and store path from CmsFileStore
		String dirRelativePath = cmsDirectory.getRelativeDirPath();
		if(!dirRelativePath.startsWith(File.separator)){
			dirRelativePath = File.separator + dirRelativePath;
		}
		String dirAbsolutePath = store.getStorePath() + dirRelativePath;
	
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(file);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + file.toString(), e);
		}
		
		String fileName = file.getFileName().toString();
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Cms Directory Id => " + cmsDirectory.getDirId() + ", Cms Directory Name => " + cmsDirectory.getName() +
				", File system path => " + dirAbsolutePath);
		
		// create cms file entry for meta data
		CmsFileEntry cmsFileEntry = new CmsFileEntry();
		cmsFileEntry.setDirectory(cmsDirectory);
		cmsFileEntry.setFileName(fileName);
		cmsFileEntry.setFileSize(Files.size(file));
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
		Path target = Paths.get(dirAbsolutePath + File.separator + fileName);
		try {
			
			Files.copy(file, target);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(file, target, cmsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(file, target, cmsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(file, target, cmsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(file, target, cmsDirectory, e);
		}
		
		// not really needed...
		if(!Files.exists(target)){
			throw new IOException("Copy proceeded without error, but file copy does not appear to exists..");
		}
		
		return cmsFileEntry;
		
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
		buf.append("CMS directory id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
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
