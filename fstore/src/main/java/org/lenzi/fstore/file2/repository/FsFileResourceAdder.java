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
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
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
	
	// access classpath resources
	@Autowired
	private ResourceLoader resourceLoader;

	/**
	 * 
	 */
	public FsFileResourceAdder() {
		
	}
	
	/**
	 * Add or replace file
	 * 
	 * @param fileToAdd - path of the file to add
	 * @param fsDirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file if a file with the same name already exists, false not to replace.
	 * 
	 * @return reference to the newly added file
	 * 
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
		FsDirectoryResource parentDir = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(fsDirId, 1);
		FsFileMetaResource existingFileResource = fsFileResourceRepository.haveExistingFile(fileName, parentDir, false);
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByPathResourceId(fsDirId);
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
	 * Add or replace file. This method provides the option of storing the file byte data in the database.
	 * 
	 * @param fileName - name of the file
	 * @param fileBytes - file byte data
	 * @param fsDirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file if a file with the same name already exists, false not to replace.
	 * @param storeInDatabase - true to store file in database AND on file system, false to only store file on file system.
	 * 
	 * @return reference to the newly added file
	 * 
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addFileResource(String fileName, byte[] fileBytes, Long fsDirId, boolean replaceExisting, boolean storeInDatabase) throws DatabaseException, IOException {
		
		if(fileName == null){
			throw new DatabaseException("Missing file name. param is null.");
		}else if(fileBytes == null || fileBytes.length == 0){
			throw new DatabaseException("Missing file data. byte array is null or length 0.");
		}
		
		FsDirectoryResource parentDir = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(fsDirId, 1);
		FsFileMetaResource existingFileResource = fsFileResourceRepository.haveExistingFile(fileName, parentDir, false);
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByPathResourceId(fsDirId);
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
			return addReplace(fileName, fileBytes, existingFileResource, parentDir, store, storeInDatabase);
			
		}else{
			
			// do add
			return add(fileName, fileBytes, parentDir, store, storeInDatabase);
			
		}		
		
	}
	
	/**
	 * Adds or replaces an existing file. This method adds the new file meta data to the database, plus a 1-byte placeholder for
	 * the binary data.
	 * 
	 * @param fileToAdd
	 * @param fsDirId
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileMetaResource addFileResourceMeta(Path fileToAdd, Long fsDirId, boolean replaceExisting) throws DatabaseException, IOException {
		
		if(fileToAdd == null){
			throw new DatabaseException("Missing file to add. param is null.");
		}
		
		String fileName = fileToAdd.getFileName().toString();
		
		FsDirectoryResource parentDir = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(fsDirId, 1);
		FsFileMetaResource existingFileResource = fsFileResourceRepository.haveExistingFile(fileName, parentDir, false);
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByPathResourceId(fsDirId);
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
			return addReplaceMeta(fileToAdd, existingFileResource, parentDir, store);
			
		}else{
			
			// do add
			return addMeta(fileToAdd, parentDir, store);
			
		}
		
	}
	
	/**
	 * Add or replace a batch of files. This method will store the file binary data in the database.
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
		
		FsDirectoryResource parentDir = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(fsDirId, 1);
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByPathResourceId(fsDirId);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch resource store for dir id => " + fsDirId, e);
		}
		
		FsFileMetaResource addedFile = null;
		List<FsFileMetaResource> addedFiles = new ArrayList<FsFileMetaResource>();
		
		for(Path fileToAdd : filesToAdd){
			
			String fileName = fileToAdd.getFileName().toString();
			
			FsFileMetaResource existingFileResource = null;
			try {
				existingFileResource = fsFileResourceRepository.haveExistingFile(fileName, parentDir, false);
			} catch (DatabaseException e) {
				throw new DatabaseException("Failed to check if directory => " + parentDir.getDirId() + 
						" already contains file with name => " + fileName, e);
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
	 * Add new file from path object. This method will store the file binary data in the database.
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
		
		//String contentType = Files.probeContentType(fileToAdd);
		String contentType = FileUtil.detectMimeType(fileToAdd);
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString());
		
		// get next node id
		long nodeId = treeRepository.getNextNodeId();
		
		// create file entry for byte[] data
		FsFileResource fileResource = new FsFileResource();
		fileResource.setFileId(nodeId);
		fileResource.setFileData(fileBytes);
		//persist(fileResource);		
		
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setNodeId(nodeId);
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setStoreId(fsStore.getStoreId());
		metaResource.setName(fileName);
		metaResource.setMimeType(contentType);
		metaResource.setFileSize((long)fileBytes.length); // TODO check this
		metaResource.setRelativePath(relativeFilePath);		
		
		metaResource.setFileResource(fileResource);
		
		//fileResource.setFileMetaResource(metaResource);
		
		FsFileMetaResource persistedMetaResource = null;
		try {
			persistedMetaResource = (FsFileMetaResource) treeRepository.addChildNode(fsDirectory, metaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error persisting new " + FsFileMetaResource.class.getName() + 
					", file name => " + metaResource.getName() + " to directory " + absoluteDirPath.toString() + 
					": " + e.getMessage());
		}
	
		getEntityManager().flush();		
		
		/*
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setStoreId(fsStore.getStoreId());
		metaResource.setName(fileName);
		metaResource.setMimeType(contentType);
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
		*/
		
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
	 * Add new file from byte[]
	 * 
	 * @param fileName
	 * @param fileData
	 * @param fsDirectory
	 * @param fsStore
	 * @param storeInDatabase - true to store file in database AND on file system, false to only store file on file system. A 1-byte entry
	 * will be added as a placeholder for the file binary data.
	 * 
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource add(String fileName, byte[] fileBytes, FsDirectoryResource fsDirectory, FsResourceStore fsStore, boolean storeInDatabase) throws DatabaseException, IOException {
		
		Path absoluteDirPath	= fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path absoluteFilePath   = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, fileName);
		String relativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, fileName);	
		
		String contentType = FileUtil.detectMimeType(fileBytes);
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString() +
				", Store in Database => " + storeInDatabase);
		
		// get next node id
		long nodeId = treeRepository.getNextNodeId();
		
		// create file entry for byte[] data
		FsFileResource fileResource = new FsFileResource();
		fileResource.setFileId(nodeId);
		fileResource.setFileData( storeInDatabase ? fileBytes : new byte[]{0x00} );
		//persist(fileResource);		
		
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setNodeId(nodeId);
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setStoreId(fsStore.getStoreId());
		metaResource.setName(fileName);
		metaResource.setMimeType(contentType);
		metaResource.setFileDataInDatabase(storeInDatabase);
		metaResource.setFileSize((long)fileBytes.length); // TODO check this
		metaResource.setRelativePath(relativeFilePath);
		
		metaResource.setFileResource(fileResource);
		
		//fileResource.setFileMetaResource(metaResource);
		
		FsFileMetaResource persistedMetaResource = null;
		try {
			persistedMetaResource = (FsFileMetaResource) treeRepository.addChildNode(fsDirectory, metaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error persisting new " + FsFileMetaResource.class.getName() + 
					", file name => " + metaResource.getName() + " to directory " + absoluteDirPath.toString() + 
					": " + e.getMessage());
		}
	
		getEntityManager().flush();
		
		/*
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setStoreId(fsStore.getStoreId());
		metaResource.setName(fileName);
		metaResource.setMimeType(contentType);
		metaResource.setFileSize((long)fileBytes.length); // TODO check this
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
		*/		
		
		// make sure objects have all data set before returning
		persistedMetaResource.setFileResource(fileResource);
		fileResource.setFileMetaResource(persistedMetaResource);
		
		// write file to disk
		try {
			
			Files.write(absoluteFilePath, fileBytes);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		}
		
		// not really needed... just a little extra precaution
		if(!Files.exists(absoluteFilePath)){
			throw new IOException("Write proceeded without error, but file does not appear to exist in target directory...");
		}
		
		return persistedMetaResource;		
		
	}
	
	/**
	 * Adds file meta data to database, and 1-byte placeholder for binary data.
	 * 
	 * @param fileToAdd
	 * @param fsDirectory
	 * @param fsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addMeta(Path fileToAdd, FsDirectoryResource fsDirectory, FsResourceStore fsStore) throws DatabaseException, IOException {
		
		String fileName = fileToAdd.getFileName().toString();
		String contentType = FileUtil.detectMimeType(fileToAdd);
		Long fileSize = FileUtil.getFileSize(fileToAdd);
		
		Path absoluteDirPath	= fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path absoluteFilePath   = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, fileName);
		String relativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, fileName);
		
		logger.info("Adding file => " + fileName + ", size => " + ((fileSize != null) ? fileSize + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString() +
				", Store in Database => " + false);
		
		// get next node id
		long nodeId = treeRepository.getNextNodeId();
		
		// create file entry for byte[] data
		FsFileResource fileResource = new FsFileResource();
		fileResource.setFileId(nodeId);
		fileResource.setFileData( new byte[]{0x00} ); // placeholder
		//persist(fileResource);		
		
		// create file entry for meta data
		FsFileMetaResource metaResource = new FsFileMetaResource();
		metaResource.setNodeId(nodeId);
		metaResource.setPathType(FsPathType.FILE);
		metaResource.setStoreId(fsStore.getStoreId());
		metaResource.setName(fileName);
		metaResource.setMimeType(contentType);
		metaResource.setFileDataInDatabase(false);
		metaResource.setFileSize(fileSize);
		metaResource.setRelativePath(relativeFilePath);
		
		metaResource.setFileResource(fileResource);
		
		//fileResource.setFileMetaResource(metaResource);
		
		FsFileMetaResource persistedMetaResource = null;
		try {
			persistedMetaResource = (FsFileMetaResource) treeRepository.addChildNode(fsDirectory, metaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error persisting new " + FsFileMetaResource.class.getName() + 
					", file name => " + metaResource.getName() + " to directory " + absoluteDirPath.toString() + 
					": " + e.getMessage());
		}
	
		getEntityManager().flush();	
		
		// make sure objects have all data set before returning
		persistedMetaResource.setFileResource(fileResource);
		fileResource.setFileMetaResource(persistedMetaResource);
		
		try {
			FileUtil.copyFile(fileToAdd, absoluteFilePath);
		} catch (IOException e) {
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionWriteError(absoluteFilePath, fsDirectory, e);
		}
		
		// not really needed... just a little extra precaution
		if(!Files.exists(absoluteFilePath)){
			throw new IOException("Write proceeded without error, but file does not appear to exist in target directory...");
		}
		
		return persistedMetaResource;		
		
	}
	
	/**
	 * Replaces existing file meta data in database, adds 1-byte place holder for new file binary data.
	 * 
	 * @param fileToAdd
	 * @param existingFsFileEntry
	 * @param fsDirectory
	 * @param fsStore
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addReplaceMeta(Path fileToAdd, FsFileMetaResource existingFsFileEntry,
			FsDirectoryResource fsDirectory, FsResourceStore fsStore) throws DatabaseException, IOException {
		
		Long existingFileId = existingFsFileEntry.getFileId();
		String existingFileName = existingFsFileEntry.getName();
		Long existingFileSize = existingFsFileEntry.getFileSize();
		
		String contentType = FileUtil.detectMimeType(fileToAdd);
		String newFileName = fileToAdd.getFileName().toString();
		Long newFileSize = FileUtil.getFileSize(fileToAdd);
		
		Path absoluteDirPath = fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path existingAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, existingFileName);
		Path newAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, newFileName);
		String newRelativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, newFileName);
		
		logger.info("Replacing existing file => " + existingFileName +
				", size => " + existingFileSize + " bytes " +
				", with new file => " + newFileName +
				", size => " + ((newFileSize != null) ? newFileSize + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString() +
				", Store in Database => " + false);
		
		// update database
		FsFileResource updateFileResource = new FsFileResource();
		updateFileResource.setFileId(existingFileId);
		updateFileResource.setFileData( new byte[]{0x00} ); // placeholder
		existingFsFileEntry.setStoreId(fsStore.getStoreId()); // not really necessary, same store
		existingFsFileEntry.setName(newFileName);
		existingFsFileEntry.setMimeType(contentType);
		existingFsFileEntry.setFileDataInDatabase(false);
		existingFsFileEntry.setRelativePath(newRelativeFilePath);
		existingFsFileEntry.setDateUpdated(DateUtil.getCurrentTime());
		existingFsFileEntry.setFileSize(newFileSize);
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
		
		// copy over new file
		try {
			FileUtil.copyFile(fileToAdd, newAbsoluteFilePath);
		} catch (IOException e) {
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		}	
		
		return fsUpdatedMetaFile;		
		
	}
	
	/**
	 * Add new file and replace existing. This method stores the file binary data in the database.
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
		
		//String contentType = Files.probeContentType(newFile);
		String contentType = FileUtil.detectMimeType(newFile);
		
		logger.info("Replacing existing file => " + existingFileName + ", size => " + existingFileSize + " bytes , with new file => " + newFileName +
				", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString());
		
		// update database
		FsFileResource updateFileResource = new FsFileResource();
		updateFileResource.setFileId(existingFileId);
		updateFileResource.setFileData(fileBytes);
		existingFsFileEntry.setStoreId(fsStore.getStoreId()); // not really necessary, same store
		existingFsFileEntry.setName(newFileName);
		existingFsFileEntry.setMimeType(contentType);
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
	 * @param fileName
	 * @param fileData
	 * @param existingFsFileEntry
	 * @param fsDirectory
	 * @param fsStore
	 * @param storeInDatabase - true to store file in database AND on file system, false to only store file on file system.
	 * 
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource addReplace(String newFileName, byte[] fileBytes, FsFileMetaResource existingFsFileEntry,
			FsDirectoryResource fsDirectory, FsResourceStore fsStore, boolean storeInDatabase) throws DatabaseException, IOException {
		
		Long existingFileId = existingFsFileEntry.getFileId();
		String existingFileName = existingFsFileEntry.getName();
		Long existingFileSize = existingFsFileEntry.getFileSize();
		
		Path absoluteDirPath = fsResourceHelper.getAbsoluteDirectoryPath(fsStore, fsDirectory);
		Path existingAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, existingFileName);
		Path newAbsoluteFilePath = fsResourceHelper.getAbsolutePath(fsStore, fsDirectory, newFileName);
		String newRelativeFilePath = fsResourceHelper.getRelativePath(fsStore, fsDirectory, newFileName);				
		
		String contentType = FileUtil.detectMimeType(fileBytes);
		
		logger.info("Replacing existing file => " + existingFileName + ", size => " + existingFileSize + " bytes , with new file => " + newFileName +
				", size => " + ((fileBytes != null) ? fileBytes.length + " bytes" : "null bytes") +
				", mime type => " + contentType +
				", Directory Id => " + fsDirectory.getDirId() + ", Directory Name => " + fsDirectory.getName() +
				", File system path => " + absoluteDirPath.toString() +
				", Store in Database => " + storeInDatabase);
		
		// update database
		FsFileResource updateFileResource = new FsFileResource();
		updateFileResource.setFileId(existingFileId);
		updateFileResource.setFileData( storeInDatabase ? fileBytes : new byte[]{0x00} );
		existingFsFileEntry.setStoreId(fsStore.getStoreId()); // not really necessary, same store
		existingFsFileEntry.setName(newFileName);
		existingFsFileEntry.setMimeType(contentType);
		existingFsFileEntry.setFileDataInDatabase(storeInDatabase);
		existingFsFileEntry.setRelativePath(newRelativeFilePath);
		existingFsFileEntry.setDateUpdated(DateUtil.getCurrentTime());
		existingFsFileEntry.setFileSize((long)fileBytes.length); // TODO - check this
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
		
		// write file to disk
		try {
			
			Files.write(newAbsoluteFilePath, fileBytes);
			
		} catch (FileAlreadyExistsException e){
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		} catch (DirectoryNotEmptyException e){
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		} catch (IOException e) {
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		} catch (SecurityException e) {
			throw buildDatabaseExceptionWriteError(newAbsoluteFilePath, fsDirectory, e);
		}		
		
		return fsUpdatedMetaFile;		
		
	}
	
	/**
	 * Updates the binary data in the database for the existing file resource. Reads the file data from disk and updates
	 * the binary data in the db.
	 * 
	 * @param fileId - thid of existing file entry to update
	 * @param fsStore - the resource store for the file entry
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public FsFileMetaResource syncDatabaseBinary(Long fileId, FsResourceStore fsStore) throws DatabaseException, IOException {
		
		FsFileMetaResource existingFsFileEntry = fsFileResourceRepository.getFileEntry(fileId, FsFileResourceFetch.FILE_META);
		
		Long existingFileId = existingFsFileEntry.getFileId();
		String existingFileName = existingFsFileEntry.getName();
		Long existingFileSize = existingFsFileEntry.getFileSize();
		String existingMimeType = existingFsFileEntry.getMimeType();
		Path existingFilePath = fsResourceHelper.getAbsoluteFilePath(fsStore, existingFsFileEntry);
		
		boolean canWriteBinaryToStore = existingFileSize > fsStore.getMaxFileSizeInDb() ? false : true;
		
		if(!canWriteBinaryToStore){
			logger.warn("Size of file " + existingFilePath.toString() + " is greater than the resource store allows. File size is " + 
					existingFileSize + " bytes, store max file size is " + fsStore.getMaxFileSizeInDb() + " bytes." );
			return existingFsFileEntry;
		}
		
		logger.info("Syncing binary data for existing file => " + existingFileName +
				", size => " + existingFileSize + " bytes " +
				", mime type => " + existingMimeType +
				", File system path => " + existingFilePath.toString());
		
		// read in file data
		// TODO - look into reading the file in chunks... not good to read entire file if file is large.
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(existingFilePath);
		} catch (IOException e) {
			throw new IOException("Error reading data from file => " + existingFilePath.toString(), e);
		}
		
		logger.info("Read " + fileBytes.length + " bytes from file => " + existingFilePath.toString());
		
		// update database
		FsFileResource updateFileResource = new FsFileResource();
		updateFileResource.setFileId(existingFileId);
		updateFileResource.setFileData( fileBytes );
		//existingFsFileEntry.setStoreId(fsStore.getStoreId()); // not really necessary, same store
		existingFsFileEntry.setName(existingFilePath.getFileName().toString());
		//existingFsFileEntry.setMimeType(contentType);
		existingFsFileEntry.setFileDataInDatabase(true);
		//existingFsFileEntry.setRelativePath(newRelativeFilePath);
		existingFsFileEntry.setDateUpdated(DateUtil.getCurrentTime());
		existingFsFileEntry.setFileSize((long)fileBytes.length);
		FsFileResource fsUpdatedFile = (FsFileResource)merge(updateFileResource);
		FsFileMetaResource fsUpdatedMetaFile = (FsFileMetaResource)merge(existingFsFileEntry);
		fsUpdatedMetaFile.setFileResource(fsUpdatedFile);
		fsUpdatedFile.setFileMetaResource(fsUpdatedMetaFile);	
		
		return fsUpdatedMetaFile;		
		
	}
	
	/**
	 * Builds exception for copy error
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
	
	/**
	 * Builds exception for write error
	 * 
	 * @param target
	 * @param directory
	 * @param e
	 * @return
	 */
	private DatabaseException buildDatabaseExceptionWriteError(Path target, FsDirectoryResource directory, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("Error writing target file => " + target.toString() + cr);
		buf.append("Target directory, id => " + directory.getDirId() + ", name => " + directory.getName() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new DatabaseException(buf.toString(), e);
		
	}		

}
