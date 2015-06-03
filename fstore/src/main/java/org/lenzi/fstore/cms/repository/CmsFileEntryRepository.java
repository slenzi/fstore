package org.lenzi.fstore.cms.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFile;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.repository.model.impl.CmsFile_;
import org.lenzi.fstore.cms.service.FileStoreHelper;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.FileUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with cms file operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class CmsFileEntryRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886994509925778014L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FileStoreHelper fileStoreHelper;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;
	
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
	
	public enum CopyOption {
		
		// skip file copy if target directory contains file with same name
		SKIP_EXISTING,
		
		// replace existing file
		REPLACE_EXISTING
		
	}	

	public CmsFileEntryRepository() {
		
	}
	
	/**
	 * Get absolute file path
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @param cmsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(CmsFileStore cmsStore, CmsDirectory cmsDirectory, CmsFileEntry cmsFileEntry){
		
		return fileStoreHelper.getAbsoluteFilePath(cmsStore, cmsDirectory, cmsFileEntry);
		
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
	 * Remove a file
	 * 
	 * @param fileId - if of the file to remove
	 * @throws DatabaseException
	 */
	public void removeFile(Long fileId) throws DatabaseException {
		
		//CmsFile file = getCmsFileById(fileId, CmsFileFetch.FILE_DATA_WITH_META);
		
		CmsFileEntry fileEntry = getCmsFileEntryById(fileId, CmsFileEntryFetch.FILE_META);
		
		CmsDirectory dir = cmsDirectoryRepository.getCmsDirectoryByFileId(fileId, CmsDirectoryFetch.FILE_NONE);
		CmsFileStore store = cmsFileStoreRepository.getCmsFileStoreByDirId(dir.getDirId());
		
		remove(store, dir, fileEntry);
		
	}	
	
	/**
	 * Remove file
	 * 
	 * @param store
	 * @param dir
	 * @param fileEntry
	 * @throws DatabaseException
	 */
	public void remove(CmsFileStore store, CmsDirectory dir, CmsFileEntry fileEntry) throws DatabaseException {
		
		String fileToDelete = fileStoreHelper.getAbsoluteFileString(store, dir, fileEntry);		
		
		logger.info("removing file id => " + fileEntry.getFileId() + ", name => " + fileEntry.getFileName() + 
				", path => " + fileToDelete);
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		try {
			
			//remove(new CmsFile(fileEntry.getFileId())); // needed?  we have CASCADE set to ALL
			//remove(fileEntry);
			
			// delete cms file
			CriteriaDelete<CmsFile> cmsFileDelete = cb.createCriteriaDelete(CmsFile.class);
			Root<CmsFile> cmsFileRoot = cmsFileDelete.from(CmsFile.class);
			cmsFileDelete.where(cb.equal(cmsFileRoot.get(CmsFile_.fileId), fileEntry.getFileId()));
			executeUpdate(cmsFileDelete);
			
			// delete cms file entry
			CriteriaDelete<CmsFileEntry> cmsFileEntryDelete = cb.createCriteriaDelete(CmsFileEntry.class);
			Root<CmsFileEntry> cmsFileEntryRoot = cmsFileEntryDelete.from(CmsFileEntry.class);
			cmsFileEntryDelete.where(cb.equal(cmsFileEntryRoot.get(CmsFileEntry_.fileId), fileEntry.getFileId()));
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

}
