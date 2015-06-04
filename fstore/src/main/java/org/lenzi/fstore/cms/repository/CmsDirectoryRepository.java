package org.lenzi.fstore.cms.repository;

import java.nio.file.Path;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.FileStoreHelper;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.service.TreeBuilder;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with cms directory operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class CmsDirectoryRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4125176200354740667L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;
	
	@Autowired
	private TreeBuilder<CmsDirectory> treeBuilder;	
	
	@Autowired
	private FileStoreHelper fileStoreHelper;
	
	
	public enum CmsDirectoryFetch {
		
		// just directory meta, no file entries
		FILE_NONE,		
		
		// just meta data for each file
		FILE_META,
		
		// meta data and byte data
		FILE_META_WITH_DATA
		
	}
	
	public CmsDirectoryRepository() {
	
	}
	
	/**
	 * Get the full absolute path for a cms directory.
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 * @throws DatabaseException
	 */
	public Path getAbsoluteDirectoryPath(CmsFileStore cmsStore, CmsDirectory cmsDirectory) {
		
		return fileStoreHelper.getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
	}
	
	/**
	 * Get directory tree
	 * 
	 * @param dirId
	 * @return
	 * @throws DatabaseException
	 */
	public Tree<CmsDirectory> getTree(Long dirId) throws DatabaseException {
		
		// TODO - allow for specific fetch options (with file meta and file data if needed.)
		
		CmsDirectory cmsDir = treeRepository.getNodeWithChild(new CmsDirectory(dirId));
		Tree<CmsDirectory> tree = null;
		try {
			tree = treeBuilder.buildTree(cmsDir);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from CmsDirectory node, id => " + dirId, e);
		}
		return tree;
		
	}	
	
	/**
	 * Get the full absolute path for a cms directory.
	 * 
	 * @param cmsDirId - id of the cms directory
	 * @return
	 * @throws DatabaseException
	 */
	public Path getAbsoluteDirectoryPath(Long cmsDirId) throws DatabaseException {
		
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
			store = cmsFileStoreRepository.getCmsFileStoreByDirId(cmsDirectory.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirectory.getDirId(), e);
		}
		
		return fileStoreHelper.getAbsoluteDirectoryPath(store, cmsDirectory);
		
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
		
		query.distinct(true);
		query.select(root);
		query.where(
				criteriaBuilder.equal(fileEntries.get(CmsFileEntry_.fileId), fileId)
				);
		
		//CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		CmsDirectory result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
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
		
		query.distinct(true);
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(CmsDirectory_.nodeId), dirId)
				);
		
		//CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		CmsDirectory result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
		return result;		
		
	}	

}
