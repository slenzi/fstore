package org.lenzi.fstore.file.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.lenzi.fstore.file.repository.model.impl.FsDirectory_;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry_;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.model.impl.FSClosure;
import org.lenzi.fstore.core.repository.model.impl.FSClosure_;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.service.TreeBuilder;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with directory operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsDirectoryRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4125176200354740667L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("DirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;
	
	@Autowired
	private TreeBuilder<FsDirectory> treeBuilder;	
	
	@Autowired
	private FsHelper fsHelper;
	
	
	public enum FsDirectoryFetch {
		
		// just directory meta, no file entries
		FILE_NONE,		
		
		// just meta data for each file
		FILE_META,
		
		// meta data and byte data
		FILE_META_WITH_DATA
		
	}
	
	public FsDirectoryRepository() {
	
	}
	
	/**
	 * Get the full absolute path for a directory.
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 * @throws DatabaseException
	 */
	public Path getAbsoluteDirectoryPath(FsFileStore cmsStore, FsDirectory cmsDirectory) {
		
		return fsHelper.getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
	}
	
	/**
	 * Get directory tree
	 * 
	 * @param dirId
	 * @return
	 * @throws DatabaseException
	 */
	public Tree<FsDirectory> getTree(Long dirId) throws DatabaseException {
		
		// TODO - allow for specific fetch options (with file meta and file data if needed.)
		
		FsDirectory cmsDir = treeRepository.getNodeWithChild(new FsDirectory(dirId));
		Tree<FsDirectory> tree = null;
		try {
			tree = treeBuilder.buildTree(cmsDir);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from FsDirectory node, id => " + dirId, e);
		}
		return tree;
		
	}
	
	/**
	 * Get directory tree, with file meta
	 * 
	 * @param dirId
	 * @return
	 * @throws DatabaseException
	 */
	public Tree<FsDirectory> getTreeWithFileMeta(Long dirId) throws DatabaseException {
		
		// TODO - allow for specific fetch options (with file meta and file data if needed.)
		
		FsDirectory fsDir = getFsDirectoryWithChild(dirId);
		
		Tree<FsDirectory> tree = null;
		try {
			tree = treeBuilder.buildTree(fsDir);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from FsDirectory node, id => " + dirId, e);
		}
		return tree;
		
	}
	
	/**
	 * Get the full absolute path for a directory.
	 * 
	 * @param cmsDirId - id of the directory
	 * @return
	 * @throws DatabaseException
	 */
	public Path getAbsoluteDirectoryPath(Long cmsDirId) throws DatabaseException {
		
		// get directory
		FsDirectory fsdir = null;
		try {
			fsdir = getFsDirectoryById(cmsDirId, FsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve FsDirectory", e);
		}
		
		// get file store for directory
		FsFileStore fsFileStore = null;
		try {
			fsFileStore = fsFileStoreRepository.getFsFileStoreByDirId(fsdir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for dir id => " + fsdir.getDirId(), e);
		}
		
		return fsHelper.getAbsoluteDirectoryPath(fsFileStore, fsdir);
		
	}	
	
	/**
	 * Fetch a directory by a file id.
	 * 
	 * @param fileId - id of the file in the directory
	 * @param fetch - specify what to fetch for the directory
	 * @return the directory entry that the file is in.
	 * @throws DatabaseException
	 */
	public FsDirectory getFsDirectoryByFileId(Long fileId, FsDirectoryFetch fetch) throws DatabaseException {
		
		// TODO - check if this returns all file entries, or just the one with the specified file id
		
		/*
	 	select d from FsDirectory as d
		join fetch d.fileEntries e
		where e.fileId = 1
		*/
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<FsDirectory> query = criteriaBuilder.createQuery(FsDirectory.class);
		Root<FsDirectory> root = query.from(FsDirectory.class);
		
		SetJoin<FsDirectory,FsFileEntry> fileEntries = root.join(FsDirectory_.fileEntries, JoinType.LEFT);
		
		switch(fetch){
		
			// just directory meta, no join
			case FILE_NONE:
				break;
		
			// just file meta data
			case FILE_META:
				root.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
				break;
			
			// file meta data, and file byte data
			case FILE_META_WITH_DATA:
				Fetch<FsDirectory,FsFileEntry> metaFetch = root.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
				metaFetch.fetch(FsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just file meta data
			default:
				root.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
				break;
				
		}		
		
		query.distinct(true);
		query.select(root);
		query.where(
				criteriaBuilder.equal(fileEntries.get(FsFileEntry_.fileId), fileId)
				);
		
		//CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		FsDirectory result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
		return result;
		
	}

	/**
	 * Fetch a FsDirectory
	 * 
	 * @param dirId - directory (node) id
	 * @param fetch - specify which file data to fetch for the directory
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectory getFsDirectoryById(Long dirId, FsDirectoryFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<FsDirectory> query = criteriaBuilder.createQuery(FsDirectory.class);
		Root<FsDirectory> root = query.from(FsDirectory.class);		
		
		switch(fetch){
		
			// just directory meta, no join
			case FILE_NONE:
				break;
		
			// just file meta data
			case FILE_META:
				root.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
				break;
			
			// file meta data, and file byte data
			case FILE_META_WITH_DATA:
				Fetch<FsDirectory,FsFileEntry> metaFetch = root.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
				metaFetch.fetch(FsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just file meta data
			default:
				root.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
				break;
				
		}
		
		query.distinct(true);
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(FsDirectory_.nodeId), dirId)
				);
		
		//CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		FsDirectory result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
		return result;		
		
	}
	
	/**
	 * Fetches the directory, plus all child directories. Includes FsFileEntry objects for all directories (just
	 * the meta data, not any binary data.)
	 * 
	 * @param dirId
	 * @return
	 * @throws DatabaseException
	 */
	private FsDirectory getFsDirectoryWithChild(Long dirId) throws DatabaseException {
		
		logger.info("Getting directory whith child data, id => " + dirId);
		
		// TODO - left join when getting file entries
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<FsDirectory> query = criteriaBuilder.createQuery(FsDirectory.class);
		Root<FsDirectory> root = query.from(FsDirectory.class);
		
		SetJoin<FsDirectory, FSClosure> childClosureJoin = root.join(FsDirectory_.childClosure, JoinType.LEFT);
		
		Fetch<FsDirectory, FsFileEntry> fileEntriesFetch =  root.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
		
		Fetch<FsDirectory, FSClosure> childClosureFetch =  root.fetch(FsDirectory_.childClosure, JoinType.LEFT);
		
		Fetch parentNodeFetch = childClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		Fetch childNodeFetch = childClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		
		childNodeFetch.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
		childNodeFetch.fetch(FsDirectory_.fileEntries, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( criteriaBuilder.equal(root.get(FsDirectory_.nodeId), dirId) );
		andPredicates.add( criteriaBuilder.greaterThanOrEqualTo(childClosureJoin.get(FSClosure_.depth), 0) );
		
		query.distinct(true);
		query.select(root);
		query.where(
				criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		//N result = getEntityManager().createQuery(nodeSelect).getSingleResult();
		
		FsDirectory result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
		return result;
	}

}
