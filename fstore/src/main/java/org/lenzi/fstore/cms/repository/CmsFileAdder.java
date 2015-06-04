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

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFile;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.FileStoreHelper;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For adding cms file entries.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class CmsFileAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4805164844876464459L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;	
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;	
	
	@Autowired
	private FileStoreHelper fileStoreHelper;	
	
	public CmsFileAdder() {
		
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
	
	private DatabaseException buildDatabaseExceptionCopyError(Path source, Path target, CmsDirectory directory, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error copying source file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Target cms directory, id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}	

}
