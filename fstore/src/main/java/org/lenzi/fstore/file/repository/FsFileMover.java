package org.lenzi.fstore.file.repository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.file.repository.model.impl.FsFileEntry_;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
import org.lenzi.fstore.file.repository.FsFileEntryRepository.FsFileEntryFetch;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For moving file entries.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsFileMover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -729945866581940183L;

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
	
	
	public FsFileMover() {
		
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
	public FsFileEntry moveFile(Long fileId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		// get source information
		FsDirectory sourceDir = fsDirectoryRepository.getFsDirectoryByFileId(fileId, FsDirectoryFetch.FILE_META);
		FsFileStore sourceStore = fsFileStoreRepository.getFsFileStoreByDirId(sourceDir.getDirId());
		FsFileEntry sourceEntry = sourceDir.getEntryByFileId(fileId);
		
		// get target information
		FsDirectory targetDir = fsDirectoryRepository.getFsDirectoryById(targetDirId, FsDirectoryFetch.FILE_META);
		FsFileStore targetStore = fsFileStoreRepository.getFsFileStoreByDirId(targetDir.getDirId());
		FsFileEntry conflictingTargetEntry = targetDir.getEntryByFileName(sourceEntry.getFileName(), false);
		
		String sourceFilePath = fsHelper.getAbsoluteFileString(sourceStore, sourceDir, sourceEntry);
		String targetFilePath = fsHelper.getAbsoluteFileString(targetStore, targetDir, sourceEntry); // use source file name
		
		// will be true of we need to replace the existing file in the target directory
		boolean needReplace = conflictingTargetEntry != null ? true : false;
		
		// replace existing file in target dir with file from source dir
		if(needReplace && replaceExisting){
			
			String conflictingTargetFilePath = fsHelper.getAbsoluteFileString(targetStore, targetDir, conflictingTargetEntry);
			
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
	public FsFileEntry move(
			FsDirectory sourceDir, FsDirectory targetDir, FsFileEntry sourceEntry,
			Path sourceFilePath, Path targetFilePath) throws DatabaseException {
		
		logger.info("Moving file, id => " + sourceEntry.getFileId() + ", to dir, id => " + targetDir.getDirId());
		
		// remove entry from source dir
		sourceDir.removeEntryById(sourceEntry.getFileId());
		sourceDir = (FsDirectory)merge(sourceDir);
		
		// add source entry to new target directory
		targetDir.addFileEntry(sourceEntry);
		sourceEntry.setDirectory(targetDir);
		targetDir = (FsDirectory)merge(targetDir);
		
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
	public FsFileEntry moveReplace(
			FsDirectory sourceDir, FsDirectory targetDir,
			FsFileEntry sourceEntry, FsFileEntry conflictingTargetEntry,
			Path sourceFilePath, Path targetFilePath, Path conflictTargetFilePath) throws DatabaseException {
		
		logger.info("File move-replace, source => " + sourceFilePath + ", target (replace) => " + 
				targetFilePath + ", existing => " + conflictTargetFilePath);
		
		// remove existing entry from target dir, then delete it
		FsFileEntry entryToRemove = targetDir.removeEntryById(conflictingTargetEntry.getFileId());
		logger.info("Remove existing file, id => " + entryToRemove.getFileId());
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<FsFileEntry> cmsFileDelete = cb.createCriteriaDelete(FsFileEntry.class);
		Root<FsFileEntry> cmsFileRoot = cmsFileDelete.from(FsFileEntry.class);
		cmsFileDelete.where(cb.equal(cmsFileRoot.get(FsFileEntry_.fileId), entryToRemove.getFileId()));
		executeUpdate(cmsFileDelete);
		//remove(entryToRemove);
		
		logger.info("Move source file, id => " + sourceEntry.getFileId() + ", from source dir, id => " + sourceDir.getDirId() +
				", to target dir, id => " + targetDir.getDirId());
		
		// remove entry from source dir, and update
		sourceDir.removeEntryById(sourceEntry.getFileId());
		sourceDir = (FsDirectory)merge(sourceDir);
		
		logger.info("Removed entry from source dir...");
		
		// add source entry to new target directory, and update
		targetDir.addFileEntry(sourceEntry);
		sourceEntry.setDirectory(targetDir);
		targetDir = (FsDirectory)merge(targetDir);
		
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
	 * Used when moving directories.
	 * 
	 * @param sourceFileEntryId - id of file to move
	 * @param sourceDirId - id of directory where source file is located
	 * @param targetDirId - id of directory where file will be moved
	 * @param sourceStore - store for source directory
	 * @param targetStore - store for target directory
	 * @param replaceExisting - true to replace any existing file in the target directory if one exists with the same name,
	 * 	false not to replace. If you pass false, and a file does exists in the target directory with the same name, then
	 * 	a FileAlreadyExistsException will be thrown.
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileEntry moveReplaceTraversal(
			Long sourceFileEntryId, Long sourceDirId, Long targetDirId,
			FsFileStore sourceStore, FsFileStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		logger.info("File move-replace traversal, source file id => " + sourceFileEntryId + ", source dir id => " + 
				sourceDirId + ", target dir id => " + targetDirId + ", replace existing? => " + replaceExisting);
		
		FsFileEntry entryToMove    = null;
		FsFileEntry existingEntry  = null;
		FsDirectory sourceDir      = null;
		FsDirectory targetDir      = null;
		Path sourceFilePath	   	   = null;
		Path targetFilePath        = null;
		Path existingPath          = null;
		Path targetDirPath         = null;
		
		sourceDir = fsDirectoryRepository.getFsDirectoryById(sourceDirId, FsDirectoryFetch.FILE_META);
		targetDir = fsDirectoryRepository.getFsDirectoryById(targetDirId, FsDirectoryFetch.FILE_META);
		
		entryToMove = fsFileEntryRepository.getFsFileEntryById(sourceFileEntryId, FsFileEntryFetch.FILE_META_WITH_DATA);
		
		existingEntry = targetDir.getEntryByFileName(entryToMove.getFileName(), false);
		boolean needReplace = existingEntry != null ? true : false;
		
		sourceFilePath = fsHelper.getAbsoluteFilePath(sourceStore, sourceDir, entryToMove);
		targetFilePath = fsHelper.getAbsoluteFilePath(targetStore, targetDir, entryToMove); // use same name
		targetDirPath  = fsHelper.getAbsoluteDirectoryPath(targetStore, targetDir);
		
		if(needReplace && replaceExisting){
			
			existingPath = fsHelper.getAbsoluteFilePath(targetStore, targetDir, existingEntry);
			
			return moveReplace(sourceDir, targetDir, entryToMove, existingEntry, sourceFilePath, targetFilePath, existingPath);
			
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Cannot move source file => " + sourceFilePath + " to target dir => " + 
					targetDirPath + ". File already exists at => " + existingPath);
			
		}else{

			return move(sourceDir, targetDir, entryToMove, sourceFilePath, targetFilePath);
			
		}

	}	
	
	private DatabaseException buildDatabaseExceptionMoveError(Path source, Path target, FsDirectory sourceDir, FsDirectory targetDir, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error moving file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Source directory, id => " + sourceDir.getDirId() + ", name => " + sourceDir.getName() + cr);
		buf.append("Target directory, id => " + targetDir.getDirId() + ", name => " + targetDir.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}	

}
