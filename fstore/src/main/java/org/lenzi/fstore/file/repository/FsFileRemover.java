package org.lenzi.fstore.file.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.file.repository.model.impl.FsFileEntry_;
import org.lenzi.fstore.file.repository.model.impl.FsFile_;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
import org.lenzi.fstore.file.repository.FsFileEntryRepository.FsFileEntryFetch;
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
 * For removing file entries
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsFileRemover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7432253700241113354L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsHelper fsHelper;
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;	
	
	@Autowired
	private FsDirectoryRepository fsDirectoryRepository;
	
	@Autowired
	private FsFileEntryRepository fsFileEntryRepository;	
	
	public FsFileRemover() {
		
	}
	
	/**
	 * Remove a file
	 * 
	 * @param fileId - if of the file to remove
	 * @throws DatabaseException
	 */
	public void removeFile(Long fileId) throws DatabaseException {
		
		//CmsFile file = getCmsFileById(fileId, CmsFileFetch.FILE_DATA_WITH_META);
		
		FsFileEntry fileEntry = fsFileEntryRepository.getFsFileEntryById(fileId, FsFileEntryFetch.FILE_META);
		
		FsDirectory dir = fsDirectoryRepository.getFsDirectoryByFileId(fileId, FsDirectoryFetch.FILE_NONE);
		FsFileStore store = fsFileStoreRepository.getFsFileStoreByDirId(dir.getDirId());
		
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
	public void remove(FsFileStore store, FsDirectory dir, FsFileEntry fileEntry) throws DatabaseException {
		
		String fileToDelete = fsHelper.getAbsoluteFileString(store, dir, fileEntry);		
		
		logger.info("removing file id => " + fileEntry.getFileId() + ", name => " + fileEntry.getFileName() + 
				", path => " + fileToDelete);
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		try {
			
			//remove(new CmsFile(fileEntry.getFileId())); // needed?  we have CASCADE set to ALL
			//remove(fileEntry);
			
			// delete fs file
			CriteriaDelete<FsFile> fsFileDelete = cb.createCriteriaDelete(FsFile.class);
			Root<FsFile> fsFileRoot = fsFileDelete.from(FsFile.class);
			fsFileDelete.where(cb.equal(fsFileRoot.get(FsFile_.fileId), fileEntry.getFileId()));
			executeUpdate(fsFileDelete);
			
			// delete fs file entry
			CriteriaDelete<FsFileEntry> fsFileEntryDelete = cb.createCriteriaDelete(FsFileEntry.class);
			Root<FsFileEntry> fsFileEntryRoot = fsFileEntryDelete.from(FsFileEntry.class);
			fsFileEntryDelete.where(cb.equal(fsFileEntryRoot.get(FsFileEntry_.fileId), fileEntry.getFileId()));
			executeUpdate(fsFileEntryDelete);
			
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
