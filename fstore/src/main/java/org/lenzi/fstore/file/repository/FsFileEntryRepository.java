package org.lenzi.fstore.file.repository;

import java.nio.file.Path;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.file.repository.model.impl.FsFileEntry_;
import org.lenzi.fstore.file.repository.model.impl.FsFile_;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFile;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with file operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsFileEntryRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886994509925778014L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsHelper fsHelper;
	
	/**
	 * When fetching a FsFileEntry, specify which file data to fetch.
	 */
	public enum FsFileEntryFetch {
		
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
	 * When fetching a FsFileEntry, specify which file data to fetch.
	 */
	public enum FsFileFetch {
		
		// just the FsFile data
		FILE_DATA,
		
		// FsFile data plus associated FsFileEntry meta data
		FILE_DATA_WITH_META,
		
	}
	
	public enum CopyOption {
		
		// skip file copy if target directory contains file with same name
		SKIP_EXISTING,
		
		// replace existing file
		REPLACE_EXISTING
		
	}	

	public FsFileEntryRepository() {
		
	}
	
	/**
	 * Get absolute file path
	 * 
	 * @param fsFileStore
	 * @param fsDirectory
	 * @param fsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(FsFileStore fsFileStore, FsDirectory fsDirectory, FsFileEntry fsFileEntry){
		
		return fsHelper.getAbsoluteFilePath(fsFileStore, fsDirectory, fsFileEntry);
		
	}
	
	/**
	 * Fetch a FsFile object
	 * 
	 * @param fileId - the file id
	 * @param fetch - specify what to fetch along with the file data
	 * @return
	 * @throws DatabaseException
	 */
	public FsFile getFsFileById(Long fileId, FsFileFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<FsFile> query = criteriaBuilder.createQuery(FsFile.class);
		Root<FsFile> root = query.from(FsFile.class);	
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(FsFile_.fileId), fileId)
				);
		
		switch(fetch){
		
			// just FsFile data
			case FILE_DATA:
				break;
			
			// CmsFile data plus associates FsFileEntry meta
			case FILE_DATA_WITH_META:
				root.fetch(FsFile_.fileEntry, JoinType.LEFT);
				break;
			
			// default to just meta data, no join
			default:
				break;
		
		}		
		
		//CmsFile result = getEntityManager().createQuery(query).getSingleResult();
		FsFile result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
		return result;
	}	
	
	/**
	 * Fetch a FsFileEntry
	 * 
	 * @param fileId - file entry id
	 * @param fetch - specify which file data to fetch for the entry, just meta data or also FsFile which includes byte data
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileEntry getFsFileEntryById(Long fileId, FsFileEntryFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<FsFileEntry> query = criteriaBuilder.createQuery(FsFileEntry.class);
		Root<FsFileEntry> root = query.from(FsFileEntry.class);		
		
		switch(fetch){
		
			// just meta data, no join
			case FILE_META:
				break;
				
			case FILE_META_WITH_DIR:
				root.fetch(FsFileEntry_.directory, JoinType.LEFT);
				break;
			
			// include FsFile with byte data
			case FILE_META_WITH_DATA:
				root.fetch(FsFileEntry_.file, JoinType.LEFT);
				break;
				
			case FILE_META_WITH_DATA_AND_DIR:
				root.fetch(FsFileEntry_.directory, JoinType.LEFT);
				root.fetch(FsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just meta data, no join
			default:
				break;
			
		}
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(FsFileEntry_.fileId), fileId)
				);
		
		//CmsFileEntry result = getEntityManager().createQuery(query).getSingleResult();
		FsFileEntry result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
		return result;		
		
	}	

}
