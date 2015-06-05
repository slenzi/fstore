package org.lenzi.fstore.cms.repository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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
import org.lenzi.fstore.cms.service.CmsFileStoreHelper;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For copying cms file entries.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class CmsFileCopier extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886994509925778014L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;
	
	@Autowired
	private CmsFileEntryRepository cmsFileEntryRepository;
	
	@Autowired
	private CmsFileStoreHelper fileStoreHelper;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;	
	
	public CmsFileCopier() {
		
	}
	
	public CmsFileEntry copyFile(Long fileId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		// get source information
		CmsDirectory sourceDir = cmsDirectoryRepository.getCmsDirectoryByFileId(fileId, CmsDirectoryFetch.FILE_META);
		CmsFileStore sourceStore = cmsFileStoreRepository.getCmsFileStoreByDirId(sourceDir.getDirId());
		// also fetch byte data
		CmsFileEntry sourceEntry = cmsFileEntryRepository.getCmsFileEntryById(fileId, CmsFileEntryFetch.FILE_META_WITH_DATA);
		
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
			
			return copyReplace(sourceDir, targetDir, sourceEntry, conflictingTargetEntry, 
					Paths.get(sourceFilePath), Paths.get(targetFilePath), Paths.get(conflictingTargetFilePath));
			
		// user specified not to replace, throw database exception
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Target directory contains a file with the same name, but 'replaceExisting' param "
					+ "was false. Cannot move file to target directory.");
		
		// simply copy file to target dir
		}else{
			
			return copy(sourceDir, targetDir, sourceEntry, Paths.get(sourceFilePath), Paths.get(targetFilePath));
			
		}		
		
	}
	
	/**
	 * Copy file entry from source dir to target dir.
	 * 
	 * @param sourceDir
	 * @param targetDir
	 * @param sourceEntry
	 * @param sourceFilePath
	 * @param targetFilePath
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileEntry copy(
			CmsDirectory sourceDir, CmsDirectory targetDir, CmsFileEntry sourceEntry,
			Path sourceFilePath, Path targetFilePath) throws DatabaseException {
	
		if(sourceEntry.getFile() == null){
			throw new DatabaseException("Cannot copy file. CmsFileEntry object with id " + 
					sourceEntry.getFileId() + " is missing it's CmsFile object. Need this data for copy.");
		}
		
		logger.info("File copy, source => " + sourceFilePath + ", target => " + targetFilePath);
		
		// create cms file entry for meta data
		CmsFileEntry cmsFileEntryCopy = new CmsFileEntry();
		cmsFileEntryCopy.setDirectory(targetDir);
		cmsFileEntryCopy.setFileName(sourceEntry.getFileName());
		cmsFileEntryCopy.setFileSize(sourceEntry.getFileSize());
		persist(cmsFileEntryCopy);
		getEntityManager().flush();

		// update target cms directory with new cms file entry copy (updates linking table)
		targetDir.addFileEntry(cmsFileEntryCopy);
		targetDir = (CmsDirectory)merge(targetDir);
		
		// create cms file copy object for file byte data, and persist
		CmsFile cmsFileCopy = new CmsFile();
		cmsFileCopy.setFileId(cmsFileEntryCopy.getFileId());
		cmsFileCopy.setFileData(sourceEntry.getFile().getFileData());
		persist(cmsFileCopy);
		getEntityManager().flush();
		
		// make sure objects have all data set before returning
		cmsFileEntryCopy.setDirectory(targetDir);
		cmsFileEntryCopy.setFile(cmsFileCopy);
		cmsFileCopy.setFileEntry(cmsFileEntryCopy);
		
		// move file to new directory
		try {
			FileUtil.copyFile(sourceFilePath, targetFilePath);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		}
		
		return cmsFileEntryCopy;
	}	
	
	/**
	 * Copy file entry from source dir to target dir, replacing existing file in target dir.
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
	public CmsFileEntry copyReplace(
			CmsDirectory sourceDir, CmsDirectory targetDir,
			CmsFileEntry sourceEntry, CmsFileEntry conflictingTargetEntry,
			Path sourceFilePath, Path targetFilePath, Path conflictTargetFilePath) throws DatabaseException {
		
		if(sourceEntry.getFile() == null){
			throw new DatabaseException("Cannot copy file. CmsFileEntry object with id " + 
					sourceEntry.getFileId() + " is missing it's CmsFile object. Need this data for copy.");
		}
		
		logger.info("File copy-replace, source => " + sourceFilePath + ", target (replace) => " + 
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
		
		// remove entry from source dir, and update
		sourceDir.removeEntryById(sourceEntry.getFileId());
		sourceDir = (CmsDirectory)merge(sourceDir);	
		
		// create cms file entry for meta data
		CmsFileEntry cmsFileEntryCopy = new CmsFileEntry();
		cmsFileEntryCopy.setDirectory(targetDir);
		cmsFileEntryCopy.setFileName(sourceEntry.getFileName());
		cmsFileEntryCopy.setFileSize(sourceEntry.getFileSize());
		persist(cmsFileEntryCopy);
		getEntityManager().flush();

		// update target cms directory with new cms file entry copy (updates linking table)
		targetDir.addFileEntry(cmsFileEntryCopy);
		targetDir = (CmsDirectory)merge(targetDir);
		
		// create cms file copy object for file byte data, and persist
		CmsFile cmsFileCopy = new CmsFile();
		cmsFileCopy.setFileId(cmsFileEntryCopy.getFileId());
		cmsFileCopy.setFileData(sourceEntry.getFile().getFileData());
		persist(cmsFileCopy);
		getEntityManager().flush();
		
		// make sure objects have all data set before returning
		cmsFileEntryCopy.setDirectory(targetDir);
		cmsFileEntryCopy.setFile(cmsFileCopy);
		cmsFileCopy.setFileEntry(cmsFileEntryCopy);
		
		// remove conflicting file, then copy over new file
		try {
			FileUtil.deletePath(conflictTargetFilePath);
			FileUtil.copyFile(sourceFilePath, targetFilePath);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(sourceFilePath, targetFilePath, sourceDir, targetDir, e);
		}
		
		return cmsFileEntryCopy;

	}
	
	/**
	 * Used when copying directories.
	 * 
	 * @param sourceFileEntryId - id of file to copy
	 * @param sourceDirId - id of directory where source file is located
	 * @param targetDirId - id of directory where copy will be created
	 * @param sourceStore - store for source directory
	 * @param targetStore - store for target directory
	 * @param replaceExisting - true to replace any existing file in the target directory if one exists with the same name,
	 * 	false not to replace. If you pass false, and a file does exists in the target directory with the same name, then
	 * 	a FileAlreadyExistsException will be thrown.
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileEntry copyReplaceTraversal(
			Long sourceFileEntryId, Long sourceDirId, Long targetDirId,
			CmsFileStore sourceStore, CmsFileStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		logger.info("File copy-replace traversal, source file id => " + sourceFileEntryId + ", source dir id => " + 
				sourceDirId + ", target dir id => " + targetDirId + ", replace existing? => " + replaceExisting);
		
		CmsFileEntry entryToCopy   = null;
		CmsFileEntry existingEntry = null;
		CmsDirectory sourceDir     = null;
		CmsDirectory targetDir     = null;
		Path sourceFilePath	   	   = null;
		Path targetFilePath        = null;
		Path existingPath          = null;
		Path targetDirPath         = null;
		
		sourceDir = cmsDirectoryRepository.getCmsDirectoryById(sourceDirId, CmsDirectoryFetch.FILE_META);
		targetDir = cmsDirectoryRepository.getCmsDirectoryById(targetDirId, CmsDirectoryFetch.FILE_META);
		
		entryToCopy = cmsFileEntryRepository.getCmsFileEntryById(sourceFileEntryId, CmsFileEntryFetch.FILE_META_WITH_DATA);
		
		existingEntry = targetDir.getEntryByFileName(entryToCopy.getFileName(), false);
		boolean needReplace = existingEntry != null ? true : false;
		
		sourceFilePath = fileStoreHelper.getAbsoluteFilePath(sourceStore, sourceDir, entryToCopy);
		targetFilePath = fileStoreHelper.getAbsoluteFilePath(targetStore, targetDir, entryToCopy); // use same name
		targetDirPath  = fileStoreHelper.getAbsoluteDirectoryPath(targetStore, targetDir);
		
		if(needReplace && replaceExisting){
			
			existingPath = fileStoreHelper.getAbsoluteFilePath(targetStore, targetDir, existingEntry);
			
			return copyReplace(sourceDir, targetDir, entryToCopy, existingEntry, sourceFilePath, targetFilePath, existingPath);
			
		}else if(needReplace && !replaceExisting){
			
			throw new FileAlreadyExistsException("Cannot copy source file => " + sourceFilePath + " to target dir => " + 
					targetDirPath + ". File already exists at => " + existingPath);
			
		}else{

			return copy(sourceDir, targetDir, entryToCopy, sourceFilePath, targetFilePath);
			
		}

	}
	
	private DatabaseException buildDatabaseExceptionCopyError(Path source, Path target, CmsDirectory sourceDir, CmsDirectory targetDir, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error copying file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Source cms directory, id => " + sourceDir.getDirId() + ", name => " + sourceDir.getName() + cr);
		buf.append("Target cms directory, id => " + targetDir.getDirId() + ", name => " + targetDir.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}	

}
