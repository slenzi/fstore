package org.lenzi.fstore.file.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
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
 * For adding file entries.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsFileAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4805164844876464459L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;	
	
	@Autowired
	private FsDirectoryRepository fsDirectoryRepository;	
	
	@Autowired
	private FsHelper fsHelper;	
	
	public FsFileAdder() {
		
	}
	
	/**
	 * Add file, or replace existing file.
	 * 
	 * @param fileToAdd - the file to add
	 * @param cmsDirId - id of the directory where file is to be added
	 * @param replaceExisting - true to replace existing file, false not to. If file already exists, and 'replaceExisting'
	 * 	is set to false, a DatabaseException will be thrown.
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileEntry addFile(Path fileToAdd, Long cmsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(!Files.exists(fileToAdd)){
			throw new IOException("File does not exist => " + fileToAdd.toString());
		}
		if(Files.isDirectory(fileToAdd)){
			throw new IOException("Path is a directory => " + fileToAdd.toString());
		}
		
		String fileName = fileToAdd.getFileName().toString();
		
		// get parent dir
		FsDirectory cmsDirectory = null;
		try {
			cmsDirectory = fsDirectoryRepository.getFsDirectoryById(cmsDirId, FsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve FsDirectory for dir id => " + cmsDirId, e);
		}
		
		// get file store
		FsFileStore cmsStore = null;
		try {
			cmsStore = fsFileStoreRepository.getFsFileStoreByDirId(cmsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for dir id => " + cmsDirId, e);
		}
		
		String dirFullPath = fsHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		
		// check if there is an existing file with the same name
		FsFileEntry existingCmsFileEntry = cmsDirectory.getEntryByFileName(fileName, false);
		
		// file exists, but we are not to replace file. throw error 
		if(existingCmsFileEntry != null && !replaceExisting){
		
			throw new DatabaseException("File " + fileName + " already exists in directory " + cmsDirectory.getName() + 
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
	public List<FsFileEntry> addFile(List<Path> filesToAdd, Long cmsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
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
		FsDirectory cmsDirectory = null;
		try {
			cmsDirectory = fsDirectoryRepository.getFsDirectoryById(cmsDirId, FsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve FsDirectory for dir id => " + cmsDirId, e);
		}
		
		// get file store
		FsFileStore cmsStore = null;
		try {
			cmsStore = fsFileStoreRepository.getFsFileStoreByDirId(cmsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for dir id => " + cmsDirId, e);
		}
		
		String dirFullPath = fsHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		
		FsFileEntry newCmsFileEntry = null;
		List<FsFileEntry> newCmsFileEntries = new ArrayList<FsFileEntry>();
		
		for(Path fileToAdd : filesToAdd){
			
			String fileName = fileToAdd.getFileName().toString();
			
			// check if there is an existing file with the same name
			FsFileEntry existingCmsFileEntry = cmsDirectory.getEntryByFileName(fileName, false);
			
			// file exists, but we are not to replace file. throw error 
			if(existingCmsFileEntry != null && !replaceExisting){
			
				throw new DatabaseException("File " + fileName + " already exists in directory " + cmsDirectory.getName() + 
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
	public FsFileEntry addReplace(Path newFile, FsFileEntry existingCmsFileEntry,
			FsDirectory cmsDirectory, FsFileStore cmsStore) throws DatabaseException, IOException {
		
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
		
		String dirFullPath = fsHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		String existingFilePath = fsHelper.getAbsoluteFileString(cmsStore, cmsDirectory, existingCmsFileEntry);
		
		logger.info("Replacing old file => " + oldFileName + ", size => " + oldFileSize + " bytes , with new file => " + newFileName +
				", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Directory Id => " + cmsDirectory.getDirId() + ", Directory Name => " + cmsDirectory.getName() +
				", File system path => " + dirFullPath);			
		
		// update database
		FsFile updatedFile = new FsFile();
		updatedFile.setFileId(fileId);
		updatedFile.setFileData(fileBytes);
		existingCmsFileEntry.setFileName(newFileName);
		existingCmsFileEntry.setFileSize(Files.size(newFile));
		FsFile updatedCmsFile = (FsFile)merge(updatedFile);
		FsFileEntry updatedCmsFileEntry = (FsFileEntry)merge(existingCmsFileEntry);
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
	public FsFileEntry add(Path fileToAdd, FsDirectory cmsDirectory, FsFileStore cmsStore) throws DatabaseException, IOException {
		
		String fileName = fileToAdd.getFileName().toString();
		String dirFullPath = fsHelper.getAbsoluteDirectoryString(cmsStore, cmsDirectory);
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(fileToAdd);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + fileToAdd.toString(), e);
		}
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Directory Id => " + cmsDirectory.getDirId() + ", Directory Name => " + cmsDirectory.getName() +
				", File system path => " + dirFullPath);	
		
		// create file entry for meta data
		FsFileEntry cmsFileEntry = new FsFileEntry();
		cmsFileEntry.setDirectory(cmsDirectory);
		cmsFileEntry.setFileName(fileName);
		cmsFileEntry.setFileSize(Files.size(fileToAdd));
		persist(cmsFileEntry);
		getEntityManager().flush();

		// update directory with new file entry (updates linking table)
		cmsDirectory.addFileEntry(cmsFileEntry);
		cmsDirectory = (FsDirectory)merge(cmsDirectory);
		
		// create file object for file byte data, and persist
		FsFile cmsFile = new FsFile();
		cmsFile.setFileId(cmsFileEntry.getFileId());
		cmsFile.setFileData(fileBytes);
		persist(cmsFile);
		getEntityManager().flush();
		
		// make sure objects have all data set before returning
		cmsFileEntry.setDirectory(cmsDirectory);
		cmsFileEntry.setFile(cmsFile);
		cmsFile.setFileEntry(cmsFileEntry);
		
		// copy file to directory for FsDirectory
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
	
	private DatabaseException buildDatabaseExceptionCopyError(Path source, Path target, FsDirectory directory, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error copying source file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Target directory, id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}	

}
