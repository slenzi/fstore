package org.lenzi.fstore.cms.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
	 * Add file, or replace existing file.
	 * 
	 * @param fileToAdd - the file to add
	 * @param cmsDirId - id of the cms directory where file is to be added
	 * @param replaceExisting - true to replace existing file, false not to. If file already exists, and 'replaceExisting'
	 * 	is set to false, a DatabaseException will be thrown.
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public CmsFileEntry addFile(Path fileToAdd, Long cmsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(!Files.exists(fileToAdd)){
			throw new IOException("File does not exist => " + fileToAdd.toString());
		}
		if(Files.isDirectory(fileToAdd)){
			throw new IOException("Path is a directory => " + fileToAdd.toString());
		}
		
		String fileName = fileToAdd.getFileName().toString();
		
		// get parent dir
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = cmsDirectoryRepository.getCmsDirectoryById(cmsDirId, CmsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve CmsDirectory for cms dir id => " + cmsDirId, e);
		}
		
		// get file store
		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsFileStoreByDirId(cmsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirId, e);
		}
		
		String dirFullPath = fileStoreHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		
		// check if there is an existing file with the same name
		CmsFileEntry existingCmsFileEntry = cmsDirectory.getEntryByFileName(fileName, false);
		
		// file exists, but we are not to replace file. throw error 
		if(existingCmsFileEntry != null && !replaceExisting){
		
			throw new DatabaseException("File " + fileName + " already exists in cms directory " + cmsDirectory.getName() + 
					" at path " + dirFullPath + ". Cannot replace existing file because 'replaceExisting' param is false.");
		
		// file exists, and we need to replace existing one
		}else if(existingCmsFileEntry != null && replaceExisting){
		
			return addReplace(fileToAdd, existingCmsFileEntry, cmsDirectory, cmsStore);
			
		// not existing file. add a new entry
		}else{
			
			return add(fileToAdd, cmsDirectory, cmsStore);
			
		}
		
	}	
	
	/**
	 * Add a list of files, or replace a series of existing files.
	 * 
	 * @param filesToAdd
	 * @param cmsDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public List<CmsFileEntry> addFile(List<Path> filesToAdd, Long cmsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(filesToAdd == null || filesToAdd.size() == 0){
			throw new DatabaseException("Files to add list is null or empty");
		}
		for(Path p : filesToAdd){
			if(!Files.exists(p)){
				throw new IOException("File does not exist => " + p.toString());
			}
			if(Files.isDirectory(p)){
				throw new IOException("Path is a directory => " + p.toString());
			}			
		}
		
		// get parent dir
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = cmsDirectoryRepository.getCmsDirectoryById(cmsDirId, CmsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve CmsDirectory for cms dir id => " + cmsDirId, e);
		}
		
		// get file store
		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsFileStoreByDirId(cmsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirId, e);
		}
		
		String dirFullPath = fileStoreHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		
		CmsFileEntry newCmsFileEntry = null;
		List<CmsFileEntry> newCmsFileEntries = new ArrayList<CmsFileEntry>();
		
		for(Path fileToAdd : filesToAdd){
			
			String fileName = fileToAdd.getFileName().toString();
			
			// check if there is an existing file with the same name
			CmsFileEntry existingCmsFileEntry = cmsDirectory.getEntryByFileName(fileName, false);
			
			// file exists, but we are not to replace file. throw error 
			if(existingCmsFileEntry != null && !replaceExisting){
			
				throw new DatabaseException("File " + fileName + " already exists in cms directory " + cmsDirectory.getName() + 
						" at path " + dirFullPath + ". Cannot replace existing file because 'replaceExisting' param is false.");
			
			// file exists, and we need to replace existing one
			}else if(existingCmsFileEntry != null && replaceExisting){
			
				newCmsFileEntry = addReplace(fileToAdd, existingCmsFileEntry, cmsDirectory, cmsStore);
				
				newCmsFileEntries.add(newCmsFileEntry);
				
			// not existing file. add a new entry
			}else{
				
				newCmsFileEntry = add(fileToAdd, cmsDirectory, cmsStore);
				
				newCmsFileEntries.add(newCmsFileEntry);
				
			}			
			
		}
		
		return newCmsFileEntries;
		
	}
	
	/**
	 * Copy a file.
	 * 
	 * @param fileId - id of the file to copy
	 * @param targetDirId - id of target directory, where file will be copied to.
	 * @param replaceExisting - pass true to replace any existing file with the same name in the target directory,
	 * 	or pass false not to replace. If you pass false, and a file already exists in the target directory, then a
	 * 	database exception will be thrown.
	 * @return
	 * @throws DatabaseException
	 */
	public CmsFileEntry copyFile(Long fileId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		// get source information
		CmsDirectory sourceDir = cmsDirectoryRepository.getCmsDirectoryByFileId(fileId, CmsDirectoryFetch.FILE_META);
		CmsFileStore sourceStore = cmsFileStoreRepository.getCmsFileStoreByDirId(sourceDir.getDirId());
		// also fetch byte data
		CmsFileEntry sourceEntry = getCmsFileEntryById(fileId, CmsFileEntryFetch.FILE_META_WITH_DATA);
		
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
	 * Add new file and replace existing
	 * 
	 * @param newFile
	 * @param existingCmsFileEntry
	 * @param cmsDirectory
	 * @param cmsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public CmsFileEntry addReplace(Path newFile, CmsFileEntry existingCmsFileEntry,
			CmsDirectory cmsDirectory, CmsFileStore cmsStore) throws DatabaseException, IOException {
		
		Long fileId = existingCmsFileEntry.getFileId();
		String newFileName = newFile.getFileName().toString();
		String oldFileName = existingCmsFileEntry.getFileName();
		Long oldFileSize = existingCmsFileEntry.getFileSize();
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(newFile);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + newFile.toString(), e);
		}	
		
		String dirFullPath = fileStoreHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		String existingFilePath = fileStoreHelper.getAbsoluteFileString(cmsStore, cmsDirectory, existingCmsFileEntry);
		
		logger.info("Replacing old file => " + oldFileName + ", size => " + oldFileSize + " bytes , with new file => " + newFileName +
				", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Cms Directory Id => " + cmsDirectory.getDirId() + ", Cms Directory Name => " + cmsDirectory.getName() +
				", File system path => " + dirFullPath);			
		
		// update database
		CmsFile updatedFile = new CmsFile();
		updatedFile.setFileId(fileId);
		updatedFile.setFileData(fileBytes);
		existingCmsFileEntry.setFileName(newFileName);
		existingCmsFileEntry.setFileSize(Files.size(newFile));
		CmsFile updatedCmsFile = (CmsFile)merge(updatedFile);
		CmsFileEntry updatedCmsFileEntry = (CmsFileEntry)merge(existingCmsFileEntry);
		updatedCmsFileEntry.setFile(updatedCmsFile);
		
		// delete old file on disk
		try {
			FileUtil.deletePath(Paths.get(existingFilePath));
		} catch (IOException e) {
			throw new DatabaseException("Could not remove existing file on disk " + existingFilePath);
		}
		
		// add new file on disk
		Path target = Paths.get(dirFullPath + File.separator + newFileName);
		try {
			
			Files.copy(newFile, target);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(newFile, target, cmsDirectory, e);
		}		
		
		return updatedCmsFileEntry;
	}
	
	/**
	 * Add new file
	 * 
	 * @param fileToAdd
	 * @param cmsDirectory
	 * @param cmsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public CmsFileEntry add(Path fileToAdd, CmsDirectory cmsDirectory, CmsFileStore cmsStore) throws DatabaseException, IOException {
		
		String fileName = fileToAdd.getFileName().toString();
		String dirFullPath = fileStoreHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(fileToAdd);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + fileToAdd.toString(), e);
		}
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Cms Directory Id => " + cmsDirectory.getDirId() + ", Cms Directory Name => " + cmsDirectory.getName() +
				", File system path => " + dirFullPath);	
		
		// create cms file entry for meta data
		CmsFileEntry cmsFileEntry = new CmsFileEntry();
		cmsFileEntry.setDirectory(cmsDirectory);
		cmsFileEntry.setFileName(fileName);
		cmsFileEntry.setFileSize(Files.size(fileToAdd));
		persist(cmsFileEntry);
		getEntityManager().flush();

		// update cms directory with new cms file entry (updates linking table)
		cmsDirectory.addFileEntry(cmsFileEntry);
		cmsDirectory = (CmsDirectory)merge(cmsDirectory);
		
		// create cms file object for file byte data, and persist
		CmsFile cmsFile = new CmsFile();
		cmsFile.setFileId(cmsFileEntry.getFileId());
		cmsFile.setFileData(fileBytes);
		persist(cmsFile);
		getEntityManager().flush();
		
		// make sure objects have all data set before returning
		cmsFileEntry.setDirectory(cmsDirectory);
		cmsFileEntry.setFile(cmsFile);
		cmsFile.setFileEntry(cmsFileEntry);
		
		// copy file to directory for CmsDirectory
		Path target = Paths.get(dirFullPath + File.separator + fileName);
		try {
			
			Files.copy(fileToAdd, target);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(fileToAdd, target, cmsDirectory, e);
		}
		
		// not really needed... just a little extra precaution
		if(!Files.exists(target)){
			throw new IOException("Copy proceeded without error, but file copy does not appear to exists..");
		}
		
		return cmsFileEntry;
		
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
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
	public CmsFileEntry moveReplace(
			CmsDirectory sourceDir, CmsDirectory targetDir,
			CmsFileEntry sourceEntry, CmsFileEntry conflictingTargetEntry,
			Path sourceFilePath, Path targetFilePath, Path conflictTargetFilePath) throws DatabaseException {
		
		logger.info("Replace file, id => " + conflictingTargetEntry.getFileId() + ", with file, id => " + sourceEntry.getFileId());
		
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
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
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
	
	private DatabaseException buildDatabaseExceptionCopyError(Path source, Path target, CmsDirectory directory, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error copying source file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Target cms directory, id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
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
