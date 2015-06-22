package org.lenzi.fstore.file2.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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
	
	@Autowired
	private ResourceLoader resourceLoader;
	
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
	 * Fetch file resource
	 * 
	 * @param fileId
	 * @param fetch
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource getFileResource(Long fileId, FsFileResourceFetch fetch) throws ServiceException {
		
		FsFileMetaResource resource = null;
		try {
			resource = fsFileResourceRepository.getFileEntry(fileId, fetch);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching file resource, id => " + fileId + ". " + e.getMessage(), e);
		}
		return resource;
		
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
	 * Add or replace file
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
	 * Add or replace list of files
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
	public FsResourceStore getStoreById(Long storeId) throws ServiceException {
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByStoreId(storeId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource store for store id " + storeId, e);
		}
		return store;
		
	}
	
	/**
	 * Fetch the resource store for a specific path resource, e.g. for any directory resource or file
	 * mete resource.
	 * 
	 * @param resourceId - the id of the resource, e.g. id of a FsDirectoryResource, or FsFileMetaResource,
	 * 	or some other resource in the tree which extends from FsPathResource.
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getStoreByPathResourceId(Long resourceId) throws ServiceException {
		
		FsResourceStore store = null;
		try {
			store = fsResourceStoreRepository.getStoreByPathResourceId(resourceId);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching resource store for by path resource id " + resourceId, e);
		}
		return store;
		
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
