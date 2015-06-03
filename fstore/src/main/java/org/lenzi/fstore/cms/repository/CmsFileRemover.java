package org.lenzi.fstore.cms.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.CmsFileEntryRepository.CmsFileEntryFetch;
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
 * For removing cms file entries
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class CmsFileRemover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7432253700241113354L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FileStoreHelper fileStoreHelper;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;	
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;
	
	@Autowired
	private CmsFileEntryRepository cmsFileEntryRepository;	
	
	public CmsFileRemover() {
		
	}
	
	/**
	 * Remove a file
	 * 
	 * @param fileId - if of the file to remove
	 * @throws DatabaseException
	 */
	public void removeFile(Long fileId) throws DatabaseException {
		
		//CmsFile file = getCmsFileById(fileId, CmsFileFetch.FILE_DATA_WITH_META);
		
		CmsFileEntry fileEntry = cmsFileEntryRepository.getCmsFileEntryById(fileId, CmsFileEntryFetch.FILE_META);
		
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
