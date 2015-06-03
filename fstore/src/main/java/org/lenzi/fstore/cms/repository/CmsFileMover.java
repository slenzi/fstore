package org.lenzi.fstore.cms.repository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
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
 * For moving cms file entries.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class CmsFileMover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -729945866581940183L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FileStoreHelper fileStoreHelper;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;	
	
	
	public CmsFileMover() {
		
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
	public CmsFileEntry moveFile(Long fileId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		// get source information
		CmsDirectory sourceDir = cmsDirectoryRepository.getCmsDirectoryByFileId(fileId, CmsDirectoryFetch.FILE_META);
		CmsFileStore sourceStore = cmsFileStoreRepository.getCmsFileStoreByDirId(sourceDir.getDirId());
		CmsFileEntry sourceEntry = sourceDir.getEntryByFileId(fileId);
		
		// get target information
		CmsDirectory targetDir = cmsDirectoryRepository.getCmsDirectoryById(targetDirId, CmsDirectoryFetch.FILE_META);
		CmsFileStore targetStore = cmsFileStoreRepository.getCmsFileStoreByDirId(targetDir.getDirId());
		CmsFileEntry conflictingTargetEntry = targetDir.getEntryByFileName(sourceEntry.getFileName(), false);
		
		String sourceFilePath = fileStoreHelper.getAbsoluteFileString(sourceStore, sourceDir, sourceEntry);
		String targetFilePath = fileStoreHelper.getAbsoluteFileString(targetStore, targetDir, sourceEntry); // use source file name
		
		// will be true of we need to replace the existing file in the target directory
		boolean needReplace = conflictingTargetEntry != null ? true : false;
		
		// replace existing file in target dir with file from source dir
		if(needReplace && replaceExisting){
			
			String conflictingTargetFilePath = fileStoreHelper.getAbsoluteFileString(targetStore, targetDir, conflictingTargetEntry);
			
			return moveReplace(sourceDir, targetDir, sourceEntry, conflictingTargetEntry, 
					Paths.get(sourceFilePath), Paths.get(targetFilePath), Paths.get(conflictingTargetFilePath));
			
		// user specified not to replace, throw database exception
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Target directory contains a file with the same name, but 'replaceExisting' param "
					+ "was false. Cannot move file to target directory.");
		
		// simply move file to target dir
		}else{
			
			return move(sourceDir, targetDir, sourceEntry, Paths.get(sourceFilePath), Paths.get(targetFilePath));
			
		}
		
	}
	
	/**
	 * Move a file, replacing existing file in target directory
	 * 
	 * @param sourceDir
	 * @param targetDir
	 * @param sourceEntry
	 * @param conflictingTargetEntry
	 * @param sourceFilePath
	 * @param targetFilePath
	 * @param conflictTargetFilePath
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileEntry moveReplace(
			CmsDirectory sourceDir, CmsDirectory targetDir,
			CmsFileEntry sourceEntry, CmsFileEntry conflictingTargetEntry,
			Path sourceFilePath, Path targetFilePath, Path conflictTargetFilePath) throws DatabaseException {
		
		logger.info("File move-replace, source => " + sourceFilePath + ", target (replace) => " + 
				targetFilePath + ", existing => " + conflictTargetFilePath);
		
		// remove existing entry from target dir, then delete it
		CmsFileEntry entryToRemove = targetDir.removeEntryById(conflictingTargetEntry.getFileId());
		logger.info("Remove existing file, id => " + entryToRemove.getFileId());
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<CmsFileEntry> cmsFileDelete = cb.createCriteriaDelete(CmsFileEntry.class);
		Root<CmsFileEntry> cmsFileRoot = cmsFileDelete.from(CmsFileEntry.class);
		cmsFileDelete.where(cb.equal(cmsFileRoot.get(CmsFileEntry_.fileId), entryToRemove.getFileId()));
		executeUpdate(cmsFileDelete);
		//remove(entryToRemove);
		
		logger.info("Move source file, id => " + sourceEntry.getFileId() + ", from source dir, id => " + sourceDir.getDirId() +
				", to target dir, id => " + targetDir.getDirId());
		
		// remove entry from source dir, and update
		sourceDir.removeEntryById(sourceEntry.getFileId());
		sourceDir = (CmsDirectory)merge(sourceDir);
		
		logger.info("Removed entry from source dir...");
		
		// add source entry to new target directory, and update
		targetDir.addFileEntry(sourceEntry);
		sourceEntry.setDirectory(targetDir);
		targetDir = (CmsDirectory)merge(targetDir);
		
		logger.info("Added entry to target dir...");
		
		logger.info("Updating physical files on disk...");
		
		// remove physical conflicting file, and move new file over
		try {
			FileUtil.deletePath(conflictTargetFilePath);
			FileUtil.moveFile(sourceFilePath, targetFilePath);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionMoveError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionMoveError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		}
		
		logger.info("Move done.");
		
		return sourceEntry;
		
	}
	
	/**
	 * Move a file
	 * 
	 * @param sourceDir
	 * @param targetDir
	 * @param sourceEntry
	 * @param sourceFilePath
	 * @param targetFilePath
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileEntry move(
			CmsDirectory sourceDir, CmsDirectory targetDir, CmsFileEntry sourceEntry,
			Path sourceFilePath, Path targetFilePath) throws DatabaseException {
		
		logger.info("Moving file, id => " + sourceEntry.getFileId() + ", to dir, id => " + targetDir.getDirId());
		
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

}
