package org.lenzi.fstore.cms.repository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.CmsFileEntryRepository.CmsFileEntryFetch;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFile;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
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
	private FileStoreHelper fileStoreHelper;	
	
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
		
		// remove existing entry from target dir, then delete it
		CmsFileEntry entryToRemove = targetDir.removeEntryById(conflictingTargetEntry.getFileId());
		remove(entryToRemove);
		
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
