package org.lenzi.fstore.file2.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNodeVisitException;
import org.lenzi.fstore.core.tree.Trees;
import org.lenzi.fstore.core.tree.Trees.WalkOption;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.FsFile;
import org.lenzi.fstore.file2.repository.FsDirectoryResourceAdder;
import org.lenzi.fstore.file2.repository.FsDirectoryResourceCopier;
import org.lenzi.fstore.file2.repository.FsDirectoryResourceMover;
import org.lenzi.fstore.file2.repository.FsDirectoryResourceRemover;
import org.lenzi.fstore.file2.repository.FsDirectoryResourceRepository;
import org.lenzi.fstore.file2.repository.FsFileResourceAdder;
import org.lenzi.fstore.file2.repository.FsFileResourceCopier;
import org.lenzi.fstore.file2.repository.FsFileResourceMover;
import org.lenzi.fstore.file2.repository.FsFileResourceRemover;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.FsResourceStoreAdder;
import org.lenzi.fstore.file2.repository.FsResourceStoreRemover;
import org.lenzi.fstore.file2.repository.FsResourceStoreRepository;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author sal
 *
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsResourceService {

	@InjectLogger
	private Logger logger;
	
	// access classpath resources
	@Autowired
	private ResourceLoader resourceLoader;
	
    @Autowired
    private FsResourceHelper fsResourceHelper;	
	
	//
	// resource store operators
	//
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	@Autowired
	private FsResourceStoreAdder fsResourceStoreAdder;
	@Autowired
	private FsResourceStoreRemover fsResourceStoreRemover;
	
	//
	// file resource operators
	//
	@Autowired
	private FsFileResourceRepository fsFileResourceRepository;
	@Autowired
	private FsFileResourceAdder fsFileResourceAdder;
	@Autowired
	private FsFileResourceCopier fsFileResourceCopier;
	@Autowired
	private FsFileResourceMover fsFileResourceMover;	
	@Autowired
	private FsFileResourceRemover fsFileResourceRemover;
	
	//
	// directory resource operators
	//
	@Autowired
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	@Autowired
	private FsDirectoryResourceAdder fsDirectoryResourceAdder;
	@Autowired
	private FsDirectoryResourceRemover fsDirectoryResourceRemover;
	@Autowired
	private FsDirectoryResourceCopier fsDirectoryResourceCopier;
	@Autowired
	private FsDirectoryResourceMover fsDirectoryResourceMover;
	
	public FsResourceService() {
		
	}
	
	/**
	 * Get tree
	 * 
	 * @param dirId - id of directory resource
	 * @return
	 * @throws ServiceException
	 */
	public Tree<FsPathResource> getTree(Long dirId) throws ServiceException {
		
		Tree<FsPathResource> resourceTree = null;
		try {
			resourceTree = fsDirectoryResourceRepository.getTree(dirId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource tree, dir id => " + dirId + ". " + e.getMessage(), e);
		}
		return resourceTree;
		
	}
	
	/**
	 * Get tree, up to the max depth
	 * 
	 * @param dirId
	 * @param maxDepth
	 * @return
	 * @throws ServiceException
	 */
	public Tree<FsPathResource> getPathResourceTree(Long dirId, int maxDepth) throws ServiceException {
		
		Tree<FsPathResource> resourceTree = null;
		try {
			resourceTree = fsDirectoryResourceRepository.getTree(dirId, maxDepth);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource tree, dir id => " + dirId + ". " + e.getMessage(), e);
		}
		return resourceTree;
		
	}
	
	/**
	 * Get parent tree (parent data of node)
	 * 
	 * @param dirId
	 * @return
	 * @throws ServiceException
	 */
	public Tree<FsPathResource> getParentPathResourceTree(Long dirId) throws ServiceException {
		
		Tree<FsPathResource> resourceTree = null;
		try {
			resourceTree = fsDirectoryResourceRepository.getParentTree(dirId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching parent resource tree, dir id => " + dirId + ". " + e.getMessage(), e);
		}
		return resourceTree;
		
	}
	
	/**
	 * Get directory for file
	 * 
	 * @param fileId - id of the file path resource
	 * @return
	 * @throws ServiceException
	 */
	public FsDirectoryResource getDirectoryResourceByFileId(Long fileId) throws ServiceException {
		
		FsDirectoryResource resource = null;
		try {
			resource = fsDirectoryResourceRepository.getDirectoryResourceByFileId(fileId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching directory resource for fileId => " + fileId + ". " + e.getMessage(), e);
		}
		return resource;
		
	}
	
	
	/**
	 * Fetch file resource
	 * 
	 * @param fileId - id of file to fetch
	 * @param fetch - specify file data to fetch
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource getFileResourceById(Long fileId, FsFileResourceFetch fetch) throws ServiceException {
		
		FsFileMetaResource resource = null;
		try {
			resource = fsFileResourceRepository.getFileEntry(fileId, fetch);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching file resource, id => " + fileId + ". " + e.getMessage(), e);
		}
		return resource;
		
	}
	
	/**
	 * Fetch file resource
	 * 
	 * @param path - path of file resource (resource store root dir name + file relative path)
	 * @param fetch - specify file data to fetch
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource getFileResourceByPath(String path, FsFileResourceFetch fetch) throws ServiceException {
		
		FsFileMetaResource resource = null;
		try {
			resource = fsFileResourceRepository.getFileEntryByPath(path, fetch);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching file resource, path => " + path + ". " + e.getMessage(), e);
		}
		return resource;
		
	}
	
	/**
	 * Get file data
	 * 
	 * @param fileId - the id of the file path resource
	 * @return An FsFile object
	 * @throws ServiceException
	 */
	public FsFile getFsFileById(Long fileId) throws ServiceException {
		
		FsFileMetaResource fileResource = getFileResourceById(fileId, FsFileResourceFetch.FILE_META_WITH_DATA);
		FsResourceStore store = getResourceStoreByPathResourceId(fileId);
		java.nio.file.Path filePath = fsResourceHelper.getAbsoluteFilePath(store, fileResource);
		
		byte[] fileData = null;
		
		boolean isFileDataInDatabase = fileResource.isFileDataInDatabase();
		if(isFileDataInDatabase){
			fileData = fileResource.getFileResource().getFileData();
		}else{
			// file probably too big to store in database, get file data from file system
			try {
				fileData = Files.readAllBytes(filePath);
			} catch (IOException e) {
				throw new ServiceException("Failed to read file " + filePath, e);
			}
		}
		
		FsFile fsFile = new FsFile();
		fsFile.setFileName(fileResource.getName());
		fsFile.setFilePath(filePath);
		fsFile.setMimeType(fileResource.getMimeType());
		fsFile.setBytes(fileData);
		
		return fsFile;
	}
	
	/**
	 * Get file data
	 * 
	 * @param path - path of file resource (resource store root dir name + file relative path)
	 * @return
	 * @throws ServiceException
	 */
	public FsFile getFsFileByPath(String path) throws ServiceException {
		
		FsFileMetaResource fileResource = getFileResourceByPath(path, FsFileResourceFetch.FILE_META_WITH_DATA);
		FsResourceStore store = getResourceStoreByPathResourceId(fileResource.getFileId());
		java.nio.file.Path filePath = fsResourceHelper.getAbsoluteFilePath(store, fileResource);
		
		byte[] fileData = null;
		
		boolean isFileDataInDatabase = fileResource.isFileDataInDatabase();
		if(isFileDataInDatabase){
			fileData = fileResource.getFileResource().getFileData();
		}else{
			// file probably too big to store in database, get file data from file system
			try {
				fileData = Files.readAllBytes(filePath);
			} catch (IOException e) {
				throw new ServiceException("Failed to read file " + filePath, e);
			}
		}
		
		FsFile fsFile = new FsFile();
		fsFile.setFileName(fileResource.getName());
		fsFile.setFilePath(filePath);
		fsFile.setMimeType(fileResource.getMimeType());
		fsFile.setBytes(fileData);
		
		return fsFile;		
	}
	
	/**
	 * Create new resource store
	 * 
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore createResourceStore(Path storePath, String name, String description, boolean clearIfExists) throws ServiceException {
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreAdder.createResourceStore(storePath, name, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new ServiceException("Error creating store. " + e.getMessage(), e);
		}
		return store;
		
	}
	
	/**
	 * Remove resource store, plus all child directories and files. Deletes everything!
	 * 
	 * @param storeId
	 * @throws ServiceException
	 */
	public void removeResourceStore(Long storeId) throws ServiceException {
	
		try {
			fsResourceStoreRemover.removeStore(storeId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error removing store. " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Add new directory
	 * 
	 * @param parentDirId
	 * @param name
	 * @return
	 * @throws ServiceException
	 */
	public FsDirectoryResource addDirectoryResource(Long parentDirId, String name) throws ServiceException {
		
		FsDirectoryResource dirResource = null;
		try {
			dirResource = fsDirectoryResourceAdder.addDirectoryResource(parentDirId, name);
		} catch (DatabaseException e) {
			throw new ServiceException("Error adding directory to parent directory, id => " + parentDirId, e);
		}
		return dirResource;
		
	}
	
	/**
	 * Add or replace file. This method stores the file binary data in the database.
	 * 
	 * @param fileToAdd
	 * @param parentDirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource addFileResource(Path fileToAdd, Long parentDirId, boolean replaceExisting) throws ServiceException {
		
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsFileResourceAdder.addFileResource(fileToAdd, parentDirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new ServiceException("Database error adding file resource => " + fileToAdd.toString() + ", to directory, id => " + parentDirId, e);
		} catch (IOException e) {
			throw new ServiceException("IO error adding file resource => " + fileToAdd.toString() + ", to directory, id => " + parentDirId, e);
		}
		
		return fileResource;
		
	}
	
	/**
	 * Add or replace file. This method provides the option of storing the file binary data in the database.
	 * 
	 * @param fileName
	 * @param fileBytes
	 * @param parentDirId
	 * @param replaceExisting - replace existing file if one already exists
	 * @param storeInDatabase - true to store file in database AND on file system, false to only store file on file system.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource addFileResource(String fileName, byte[] fileBytes, Long parentDirId, boolean replaceExisting, boolean storeInDatabase) throws ServiceException {
		
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsFileResourceAdder.addFileResource(fileName, fileBytes, parentDirId, replaceExisting, storeInDatabase);
		} catch (DatabaseException e) {
			throw new ServiceException("Database error adding file resource => " + fileName + ", to directory, id => " + parentDirId, e);
		} catch (IOException e) {
			throw new ServiceException("IO error adding file resource => " + fileName + ", to directory, id => " + parentDirId, e);
		}
		
		return fileResource;
		
	}	
	
	/**
	 * Add or replace list of files. This method provides the option of storing the file binary data in the database.
	 * 
	 * @param filesToAdd
	 * @param parentDirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public List<FsFileMetaResource> addFileResource(List<Path> filesToAdd, Long parentDirId, boolean replaceExisting) throws ServiceException {
		
		List<FsFileMetaResource> fileResources = null;
		try {
			fileResources = fsFileResourceAdder.addFileResource(filesToAdd, parentDirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new ServiceException("Database error adding file resources to directory, id => " + parentDirId, e);
		} catch (IOException e) {
			throw new ServiceException("IO error adding file resources to directory, id => " + parentDirId, e);
		}
		
		return fileResources;
		
	}
	
	/**
	 * Add or replace file. This method does not store the file binary data in the database. A 1-byte placeholder will be
	 * added in place of the binary data.
	 * 
	 * @param fileToAdd
	 * @param parentDirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource addFileResourceMeta(Path fileToAdd, Long parentDirId, boolean replaceExisting) throws ServiceException {
		
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsFileResourceAdder.addFileResourceMeta(fileToAdd, parentDirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new ServiceException("Database error adding file meta resource => " + fileToAdd.toString() + ", to directory, id => " + parentDirId, e);
		} catch (IOException e) {
			throw new ServiceException("IO error adding file meta resource => " + fileToAdd.toString() + ", to directory, id => " + parentDirId, e);
		}
		
		return fileResource;
		
	}
	
	/**
	 * Reads the file data from disk and re-writes it to the database for the existing file entry.
	 * 
	 * @param fileId
	 * @param store
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource syncDatabaseBinary(Long fileId, FsResourceStore store) throws ServiceException {
		
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsFileResourceAdder.syncDatabaseBinary(fileId, store);
		} catch (DatabaseException e) {
			throw new ServiceException("Database error syncing binary data for file meta resource with id => " + fileId, e);
		} catch (IOException e) {
			throw new ServiceException("IO error syncing binary data for file meta resource with id => " + fileId, e);
		}
		
		return fileResource;
		
	}
	
	/**
	 * Copy file
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource copyFileResource(Long fileId, Long dirId, boolean replaceExisting) throws ServiceException {
		
		FsFileMetaResource copyResource = null;
		try {
			copyResource = fsFileResourceCopier.copyFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new ServiceException("File already exists exception when copying file resource, id => " + fileId + " to directory, id => " + dirId, e);
		} catch (DatabaseException e) {
			throw new ServiceException("Database exception when copying file resource, id => " + fileId + " to directory, id => " + dirId, e);
		}
		return copyResource;
		
	}
	
	/**
	 * Move file
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource moveFileResource(Long fileId, Long dirId, boolean replaceExisting) throws ServiceException {
		
		FsFileMetaResource copyResource = null;
		try {
			copyResource = fsFileResourceMover.moveFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new ServiceException("File already exists exception when moving file resource, id => " + fileId + " to directory, id => " + dirId, e);
		} catch (DatabaseException e) {
			throw new ServiceException("Database exception when moving file resource, id => " + fileId + " to directory, id => " + dirId, e);
		}
		return copyResource;
		
	}	
	
	/**
	 * Remove file
	 * 
	 * @param fileId
	 * @throws ServiceException
	 */
	public void removeFileResource(Long fileId) throws ServiceException {
		
		try {
			fsFileResourceRemover.removeFile(fileId);
		} catch (DatabaseException e) {
			throw new ServiceException("Database error removing file resource, id => " + fileId, e);
		}
		
	}
	
	/**
	 * Remove directory, along with all child directories and files.
	 * 
	 * @param dirId
	 * @throws ServiceException
	 */
	public void removeDirectoryResource(Long dirId) throws ServiceException {
		
		try {
			fsDirectoryResourceRemover.removeDirectory(dirId);
		} catch (DatabaseException e) {
			throw new ServiceException("Database error removing directory resource, id => " + dirId, e);
		}
		
	}
	
	/**
	 * Copy directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 * @throws ServiceException
	 */
	public void copyDirectoryResource(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws ServiceException {
		
		try {
			fsDirectoryResourceCopier.copyDirectory(sourceDirId, targetDirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new ServiceException("File already exists exception when copying source directory, id => " + sourceDirId + " to targey directory, id => " + targetDirId, e);
		} catch (DatabaseException e) {
			throw new ServiceException("Database exception when copying source directory, id => " + sourceDirId + " to targey directory, id => " + targetDirId, e);
		}
		
	}
	
	/**
	 * Move directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 * @throws ServiceException
	 */
	public void moveDirectoryResource(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws ServiceException {
		
		try {
			fsDirectoryResourceMover.moveDirectory(sourceDirId, targetDirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new ServiceException("File already exists exception when moving source directory, id => " + sourceDirId + " to targey directory, id => " + targetDirId, e);
		} catch (DatabaseException e) {
			throw new ServiceException("Database exception when moving source directory, id => " + sourceDirId + " to targey directory, id => " + targetDirId, e);
		}
		
	}
	
	/**
	 * Add directory 
	 * 
	 * @deprecated - remove
	 * 
	 * @param dirName
	 * @return
	 * @throws ServiceException
	 */
	/*
	public FsDirectoryResource addRootDirectory(String dirName) throws ServiceException {
		
		FsDirectoryResource dirResource = null;
		
		try {
			dirResource = fsDirectoryResourceAdder.addRootDirectoryResource(dirName);
		} catch (DatabaseException e) {
			throw new ServiceException("Error adding root directory", e);
		}
		
		return dirResource;
		
	}
	*/
	
	/**
	 * Get resource store by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getResourceStoreById(Long storeId) throws ServiceException {
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByStoreId(storeId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource store for store id " + storeId, e);
		}
		return store;
		
	}
	
	/**
	 * Get resource store by store name. Store names should be unique, so only
	 * one store object is returned (if a store with the provided name exists.)
	 * 
	 * @param storeName
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getStoreByName(String storeName) throws ServiceException {
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByStoreName(storeName);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource store for store name '" + storeName + "'.", e);
		}
		return store;
		
	}	
	
	/**
	 * Get all resource stores
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public List<FsResourceStore> getAllStores() throws ServiceException {
		
		List<FsResourceStore> stores = null;
		try {
			stores = fsResourceStoreRepository.getAllStores();
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource stores", e);
		}
		return stores;
	}
	
	/**
	 * Fetch the resource store for a specific path resource, e.g. for any directory resource or file
	 * meta resource.
	 * 
	 * @param resourceId - the id of the resource, e.g. id of a FsDirectoryResource, or FsFileMetaResource,
	 * 	or some other resource in the tree which extends from FsPathResource.
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getResourceStoreByPathResourceId(Long resourceId) throws ServiceException {
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByPathResourceId(resourceId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource store for by path resource id " + resourceId, e);
		}
		return store;
		
	}
	
	/**
	 * Updates the binary data in the database
	 * 
	 * @param fileId - ID of file in database to update binary data
	 * @param file - path to file on disk. data from this file will be written to the database file entry, uopdating
	 * 	any exists binary data in the database
	 * @throws ServiceException
	 */
	public void updateFileResourceBinary(Long fileId, Path file) throws ServiceException {
		
	}
	
	/**
	 * Creates a sample file store with some file resources from classpath src/main/resources/images directory.
	 * 
	 * @param storePath - path where store will be created
	 * @return reference to newly created file store
	 * @throws ServiceException
	 */
	public FsResourceStore createSampleResourceStore(Path storePath) throws ServiceException {
		
		FsResourceStore fsStore = null;
		
		LocalDateTime timePoint = LocalDateTime.now();
		
		String pathPostfix = String.format("_%d.%s.%d_%d.%d.%d",
				timePoint.getYear(), timePoint.getMonth(), timePoint.getDayOfMonth(),
				timePoint.getHour(), timePoint.getMinute(), timePoint.getSecond());
		
		String dateTime = String.format("%s %s", timePoint.format( DateTimeFormatter.ISO_DATE ), 
				timePoint.format( DateTimeFormatter.ISO_TIME ));
		
		Path fullStorePath = Paths.get(storePath.toString() + pathPostfix);
		
		logger.info("Creating sample file store at => " + fullStorePath);

		fsStore = createResourceStore(fullStorePath, "Example File Store " + dateTime, 
				"This is an example file store, created at " + dateTime, true);
		
		logger.info("Store, name => " + fsStore.getName() + " was successfully created at, path => " + 
				fsStore.getStorePath());
		logger.info("Store root directory, id => " + fsStore.getRootDirectoryResource().getDirId() + 
				", name => " + fsStore.getRootDirectoryResource().getName() + 
				", relative path => " + fsStore.getRootDirectoryResource().getRelativePath());
	
		logger.info("Adding some directories...");
	
		FsDirectoryResource sampleDir1 = addDirectoryResource(fsStore.getRootDirectoryResource().getDirId(), "Sample directory 1");
			addDirectoryResource(sampleDir1.getDirId(), "Sample directory 1-1");
			addDirectoryResource(sampleDir1.getDirId(), "Sample directory 1-2");
		
		FsDirectoryResource sampleDir2 = addDirectoryResource(fsStore.getRootDirectoryResource().getDirId(), "Sample directory 2");
			addDirectoryResource(sampleDir2.getDirId(), "Sample directory 2-1");
			addDirectoryResource(sampleDir2.getDirId(), "Sample directory 2-2");
		
		FsDirectoryResource sampleDir3 = addDirectoryResource(fsStore.getRootDirectoryResource().getDirId(), "Sample directory 3");
			addDirectoryResource(sampleDir3.getDirId(), "Sample directory 3-1");
			addDirectoryResource(sampleDir3.getDirId(), "Sample directory 3-2");
		
		Resource sampleImageResource = resourceLoader.getResource("classpath:image/");
		
		Path sampleImagePath = null;
		try {
			sampleImagePath = Paths.get(sampleImageResource.getFile().getAbsolutePath());
		} catch (IOException e) {
			throw new ServiceException("Error attempting to get parent path for classpath images at src/main/resource/images", e);
		}
		
		// all images, at depth 1
		List<Path> listSampleImages = null;
		try {
			listSampleImages = FileUtil.listFilesToDepth(sampleImagePath, 1);
		} catch (IOException e) {
			throw new ServiceException("Error attempting to get list of paths for sample images in src/main/resource/images", e);
		}
		
		if(listSampleImages == null || listSampleImages.size() < 9){
			throw new ServiceException("No images in classpath images folder at src/main/resources/images, or less than 9 images. Need at least 9.");
		}
		
		Iterator<Path> imagePathItr = listSampleImages.iterator();
			
		Tree<FsPathResource> pathTree = getTree(fsStore.getRootDirectoryResource().getDirId());
		
		// walk tree and add sample images to each of the directories
		try {
			
			Trees.walkTree(pathTree,
					(treeNode) -> {
						
						if(treeNode.getData().getPathType().equals(FsPathType.DIRECTORY)){
						
							// skip root directory for file store and only add files to child directories
							if(!treeNode.getData().isRootNode()){
							
								Path nextImagePath = imagePathItr.hasNext() ? imagePathItr.next() : null;
								
								if(nextImagePath != null){
									try {
										
										addFileResource(nextImagePath, treeNode.getData().getNodeId(), true);
										
									} catch (ServiceException e) {
										throw new TreeNodeVisitException("Error while adding file " + nextImagePath + 
												" to directory " + treeNode.getData().getName(), e);
									}
								}
								
							}							
							
						}
						
					},
					WalkOption.PRE_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new ServiceException("Error while adding sample images to sample file store.", e);
		}
		
		return fsStore;
		
	}

}
