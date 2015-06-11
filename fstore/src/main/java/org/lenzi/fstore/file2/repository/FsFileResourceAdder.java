/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsFileResourceAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1756855144789479319L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
	@Autowired
	private FsFileResourceRepository fsFileResourceRepository;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;

	/**
	 * 
	 */
	public FsFileResourceAdder() {
		
	}
	
	/**
	 * Add or replace file
	 * 
	 * @param fileToAdd
	 * @param fsDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addFileResource(Path fileToAdd, Long fsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(!Files.exists(fileToAdd)){
			throw new IOException("File does not exist => " + fileToAdd.toString());
		}
		if(Files.isDirectory(fileToAdd)){
			throw new IOException("Path is a directory => " + fileToAdd.toString());
		}
		
		String fileName = fileToAdd.getFileName().toString();
		
		FsFileMetaResource existingFileResource = null;
		try {
			existingFileResource = fsFileResourceRepository.haveExistingFile(fileName, fsDirId, false);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to check if directory => " + fsDirId + " already contains file with name => " + fileName, e);
		}

		FsDirectoryResource parentDir = null;
		try {
			parentDir = fsDirectoryResourceRepository.getDirectoryResourceById(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch parent directory, parent dir id => " + fsDirId, e);
		}
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByDirectoryId(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch resource store for dir id => " + fsDirId, e);
		}
		
		boolean needReplace = existingFileResource != null ? true : false;
		Path absoluteDirPath= fsResourceHelper.getAbsoluteDirectoryPath(store, parentDir);
		
		if(needReplace && !replaceExisting){
			
			throw new DatabaseException("File " + fileName + " already exists in directory " + parentDir.getName() + 
					" at path " + absoluteDirPath + ". Cannot replace existing file because 'replaceExisting' param is false.");
			
		}else if(needReplace && replaceExisting){
			
			// do replace
			return addReplace(fileToAdd, existingFileResource, parentDir, store);
			
		}else{
			
			// do add
			return add(fileToAdd, parentDir, store);
			
		}
		
	}
	
	/**
	 * Add or replace a batch of files
	 * 
	 * @param filesToAdd
	 * @param fsDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public List<FsFileMetaResource> addFileResource(List<Path> filesToAdd, Long fsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
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
		
		FsDirectoryResource parentDir = null;
		try {
			parentDir = fsDirectoryResourceRepository.getDirectoryResourceById(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch parent directory, parent dir id => " + fsDirId, e);
		}
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByDirectoryId(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch resource store for dir id => " + fsDirId, e);
		}
		
		FsFileMetaResource addedFile = null;
		List<FsFileMetaResource> addedFiles = new ArrayList<FsFileMetaResource>();
		
		for(Path fileToAdd : filesToAdd){
			
			String fileName = fileToAdd.getFileName().toString();
			
			FsFileMetaResource existingFileResource = null;
			try {
				existingFileResource = fsFileResourceRepository.haveExistingFile(fileName, fsDirId, false);
			} catch (DatabaseException e) {
				throw new DatabaseException("Failed to check if directory => " + fsDirId + " already contains file with name => " + fileName, e);
			}			
			
			boolean needReplace = existingFileResource != null ? true : false;
			Path absoluteDirPath= fsResourceHelper.getAbsoluteDirectoryPath(store, parentDir);
			
			if(needReplace && !replaceExisting){
				
				throw new DatabaseException("File " + fileName + " already exists in directory " + parentDir.getName() + 
						" at path " + absoluteDirPath + ". Cannot replace existing file because 'replaceExisting' param is false.");
				
			}else if(needReplace && replaceExisting){
				
				// do replace
				addedFile = addReplace(fileToAdd, existingFileResource, parentDir, store);
				
				addedFiles.add(addedFile);
				
			}else{
				
				// do add
				addedFile = add(fileToAdd, parentDir, store);
				
				addedFiles.add(addedFile);
				
			}			
			
		}
		
		return addedFiles;
		
	}
	
	/**
	 * Add new file
	 * 
	 * @param fileToAdd
	 * @param fsDirectory
	 * @param fsFileStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource add(Path fileToAdd, FsDirectoryResource fsDirectory, FsResourceStore fsStore) throws DatabaseException, IOException {
		
		String fileName = fileToAdd.getFileName().toString();
		
		Path absoluteDirPath	= fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path absoluteFilePath   = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, fileName);
		String relativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, fileName);	
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(fileToAdd);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + fileToAdd.toString(), e);
		}
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString());		
		
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setName(fileName);
		metaResource.setFileSize(Files.size(fileToAdd));
		metaResource.setRelativePath(relativeFilePath);
		FsFileMetaResource persistedMetaResource = null;
		try {
			persistedMetaResource = (FsFileMetaResource) treeRepository.addChildNode(fsDirectory, metaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error persisting new " + FsFileMetaResource.class.getName() + 
					", file name => " + metaResource.getName() + " to directory " + absoluteDirPath.toString());
		}
		
		// create file entry for byte[] data
		FsFileResource fileResource = new FsFileResource();
		fileResource.setFileId(persistedMetaResource.getFileId());
		fileResource.setFileData(fileBytes);
		persist(fileResource);
		getEntityManager().flush();	
		
		// make sure objects have all data set before returning
		persistedMetaResource.setFileResource(fileResource);
		fileResource.setFileMetaResource(persistedMetaResource);
		
		// copy file to directory
		try {
			
			Files.copy(fileToAdd, absoluteFilePath);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(fileToAdd, absoluteFilePath, fsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(fileToAdd, absoluteFilePath, fsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(fileToAdd, absoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(fileToAdd, absoluteFilePath, fsDirectory, e);
		}
		
		// not really needed... just a little extra precaution
		if(!Files.exists(absoluteFilePath)){
			throw new IOException("Copy proceeded without error, but file copy does not appear to exists..");
		}
		
		return persistedMetaResource;
		
	}
	
	/**
	 * Add new file and replace existing
	 * 
	 * @param newFile
	 * @param existingFsFileEntry
	 * @param fsDirectory
	 * @param fsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addReplace(Path newFile, FsFileMetaResource existingFsFileEntry,
			FsDirectoryResource fsDirectory, FsResourceStore fsStore) throws DatabaseException, IOException {
		
		Long existingFileId = existingFsFileEntry.getFileId();
		String newFileName = newFile.getFileName().toString();
		String existingFileName = existingFsFileEntry.getName();
		Long existingFileSize = existingFsFileEntry.getFileSize();
		
		Path absoluteDirPath = fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path existingAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, existingFileName);
		Path newAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, newFileName);
		String newRelativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, newFileName);			
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(newFile);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + newFile.toString(), e);
		}	
		
		//String dirFullPath = fsHelper.getAbsoluteDirectoryString(fsFileStore, fsDirectory);
		//String existingFilePath = fsHelper.getAbsoluteFileString(fsFileStore, fsDirectory, existingFsFileEntry);
		
		logger.info("Replacing existing file => " + existingFileName + ", size => " + existingFileSize + " bytes , with new file => " + newFileName +
				", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString());			
		
		
		// update database
		FsFileResource updateFileResource = new FsFileResource();
		updateFileResource.setFileId(existingFileId);
		updateFileResource.setFileData(fileBytes);
		existingFsFileEntry.setName(newFileName);
		existingFsFileEntry.setRelativePath(newRelativeFilePath);
		existingFsFileEntry.setDateUpdated(DateUtil.getCurrentTime());
		existingFsFileEntry.setFileSize(Files.size(newFile));
		FsFileResource fsUpdatedFile = (FsFileResource)merge(updateFileResource);
		FsFileMetaResource fsUpdatedMetaFile = (FsFileMetaResource)merge(existingFsFileEntry);
		fsUpdatedMetaFile.setFileResource(fsUpdatedFile);
		fsUpdatedFile.setFileMetaResource(fsUpdatedMetaFile);
		
		// delete old file on disk
		try {
			FileUtil.deletePath(existingAbsoluteFilePath);
		} catch (IOException e) {
			throw new DatabaseException("Could not remove existing file on disk " + existingAbsoluteFilePath.toString());
		}
		
		// add new file on disk
		try {
			
			Files.copy(newFile, newAbsoluteFilePath);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionCopyError(newFile, newAbsoluteFilePath, fsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionCopyError(newFile, newAbsoluteFilePath, fsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionCopyError(newFile, newAbsoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionCopyError(newFile, newAbsoluteFilePath, fsDirectory, e);
		}		
		
		return fsUpdatedMetaFile;
	}
	
	/**
	 * 
	 * @param source
	 * @param target
	 * @param directory
	 * @param e
	 * @return
	 */
	private DatabaseException buildDatabaseExceptionCopyError(Path source, Path target, FsDirectoryResource directory, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error copying source file => " + source.toString() + " to target file => " + target.toString() + cr);
		buf.append("Target directory, id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}	

}
