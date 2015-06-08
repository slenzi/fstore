package org.lenzi.fstore.file.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNodeVisitException;
import org.lenzi.fstore.core.tree.Trees;
import org.lenzi.fstore.core.tree.Trees.WalkOption;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file.repository.FsDirectoryAdder;
import org.lenzi.fstore.file.repository.FsDirectoryCopier;
import org.lenzi.fstore.file.repository.FsDirectoryMover;
import org.lenzi.fstore.file.repository.FsDirectoryRemover;
import org.lenzi.fstore.file.repository.FsDirectoryRepository;
import org.lenzi.fstore.file.repository.FsFileAdder;
import org.lenzi.fstore.file.repository.FsFileCopier;
import org.lenzi.fstore.file.repository.FsFileEntryRepository;
import org.lenzi.fstore.file.repository.FsFileMover;
import org.lenzi.fstore.file.repository.FsFileRemover;
import org.lenzi.fstore.file.repository.FsFileStoreAdder;
import org.lenzi.fstore.file.repository.FsFileStoreRepository;
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
import org.lenzi.fstore.file.repository.FsFileEntryRepository.FsFileEntryFetch;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main service for working with cms stores, directories, and files.
 * 
 * @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
 * @Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
 * @Transactional(propagation=Propagation.PROPAGATION_MANDATORY, rollbackFor=Throwable.class)
 * 
 * @author sal
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;
	
	@Autowired
	private FsDirectoryRepository fsDirectoryRepository;
	
	@Autowired
	private FsFileEntryRepository fsFileEntryRepository;
	
	@Autowired
	private FsFileStoreAdder fsFileStoreAdder;

	@Autowired
	private FsFileAdder fsFileAdder;	
	
	@Autowired
	private FsFileCopier fsFileCopier;
	
	@Autowired
	private FsFileMover fsFileMover;
	
	@Autowired
	private FsFileRemover fsFileRemover;
	
	@Autowired
	private FsDirectoryAdder fsDirectoryAdder;
	
	@Autowired
	private FsDirectoryCopier fsDirectoryCopier;

	@Autowired
	private FsDirectoryMover fsDirectoryMover;	
	
	@Autowired
	private FsDirectoryRemover fsDirectoryRemover;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	
	public FsService() {
		
	}
	
	/**
	 * Get printable tree in string format
	 */
	public String printTree(Long dirId) throws FsServiceException {
		
		return getTree(dirId).printTree();
		
	}
	
	/**
	 * Get tree for directory
	 * 
	 * @param dirId
	 * @return
	 * @throws FsServiceException
	 */
	public Tree<FsDirectory> getTree(Long dirId) throws FsServiceException {
		
		try {
			return fsDirectoryRepository.getTree(dirId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching tree for directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Get tree for directory, and include CmsFileEntry objects for each directory (just meta, no binary data.)
	 * 
	 * @param dirId
	 * @return
	 * @throws FsServiceException
	 */
	public Tree<FsDirectory> getTreeWithFileMeta(Long dirId) throws FsServiceException {
		
		try {
			return fsDirectoryRepository.getTreeWithFileMeta(dirId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching tree for directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Get full path for directory
	 * 
	 * @param nodeId
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(Long dirId) throws FsServiceException {
		
		Path path = null;
		try {
			path = fsDirectoryRepository.getAbsoluteDirectoryPath(dirId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching path for directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return path;
		
	}
	
	/**
	 * Get full path for directory
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(FsFileStore cmsStore, FsDirectory cmsDirectory) {
		
		return fsDirectoryRepository.getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
	}
	
	/**
	 * Get full path for file
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @param cmsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(FsFileStore cmsStore, FsDirectory cmsDirectory, FsFileEntry cmsFileEntry){
		
		return fsFileEntryRepository.getAbsoluteFilePath(cmsStore, cmsDirectory, cmsFileEntry);
		
	}
	
	/**
	 * Create new store.
	 * 
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileStore createFileStore(Path storePath, String name, String description, boolean clearIfExists) throws FsServiceException {
		
		FsFileStore cmsStore = null;
		try {
			cmsStore = fsFileStoreAdder.createFileStore(storePath, name, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error creating store. " + e.getMessage(), e);
		}
		return cmsStore;
		
	}

	/**
	 * Get store by id
	 * 
	 * @param storeId
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileStore getFsStoreByStoreId(long storeId) throws FsServiceException {
		
		FsFileStore cmsStore = null;
		try {
			cmsStore = fsFileStoreRepository.getFsStoreByStoreId(storeId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching store, id => " + storeId + ". " + e.getMessage(), e);
		}
		return cmsStore;
		
	}

	/**
	 * Get store by it's root directory
	 * 
	 * @param rootDirId
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileStore getFsStoreByRootDirId(long rootDirId) throws FsServiceException {

		FsFileStore cmsStore = null;
		try {
			cmsStore = fsFileStoreRepository.getFsStoreByRootDirId(rootDirId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching store by root directory, id => " + rootDirId + ". " + e.getMessage(), e);
		}
		return cmsStore;		
		
	}
	
	/**
	 * Get store by any directory. does not have to be a root directory.
	 * 
	 * @param dirId
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileStore getFsStoreByDirId(long dirId) throws FsServiceException {

		FsFileStore cmsStore = null;
		try {
			cmsStore = fsFileStoreRepository.getFsFileStoreByDirId(dirId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching store by directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return cmsStore;		
		
	}

	/**
	 * Fetch directory by id
	 * 
	 * @param dirId
	 * @param fetch
	 * @return
	 * @throws FsServiceException
	 */
	public FsDirectory getFsDirectoryById(long dirId, FsDirectoryFetch fetch) throws FsServiceException {
		
		FsDirectory dir = null;
		try {
			dir = fsDirectoryRepository.getFsDirectoryById(dirId, fetch);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return dir;
		
	}

	/**
	 * Add new directory
	 * 
	 * @param parentDirId
	 * @param dirName
	 * @return
	 * @throws FsServiceException
	 */
	public FsDirectory addDirectory(Long parentDirId, String dirName) throws FsServiceException {

		FsDirectory dir = null;
		try {
			dir = fsDirectoryAdder.addDirectory(parentDirId, dirName);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error adding new directory. " + e.getMessage(), e);
		}
		return dir;
		
	}

	/**
	 * Add/replace new file
	 * 
	 * @param sourcePath
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileEntry addFile(Path sourcePath, Long dirId, boolean replaceExisting) throws FsServiceException {

		FsFileEntry fileEntry = null;
		try {
			fileEntry = fsFileAdder.addFile(sourcePath, dirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new FsServiceException("Database error when adding new file. " + e.getMessage(), e);
		} catch (IOException e) {
			throw new FsServiceException("I/O error when adding new file. " + e.getMessage(), e);
		}
		return fileEntry;
		
	}
	
	/**
	 * Add files
	 * 
	 * @param filePaths
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws FsServiceException
	 */
	public List<FsFileEntry> addFile(List<Path> filePaths, Long dirId, boolean replaceExisting) throws FsServiceException {
		
		List<FsFileEntry> entries = null;
		try {
			entries = fsFileAdder.addFile(filePaths, dirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new FsServiceException("Database error when adding new files. " + e.getMessage(), e);
		} catch (IOException e) {
			throw new FsServiceException("I/O error when adding new files. " + e.getMessage(), e);
		}
		return entries;
		
	}

	/**
	 * Get a file entry
	 * 
	 * @param fileId
	 * @param fetch
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileEntry getFsFileEntryById(Long fileId, FsFileEntryFetch fetch) throws FsServiceException {

		FsFileEntry fileEntry = null;
		try {
			fileEntry = fsFileEntryRepository.getFsFileEntryById(fileId, fetch);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error fetching file, id => " + fileId + ". " + e.getMessage(), e);
		}
		return fileEntry;		
		
	}

	/**
	 * Copy file
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileEntry copyFile(Long fileId, Long dirId, boolean replaceExisting) throws FsServiceException {
		
		FsFileEntry copy = null;
		try {
			copy = fsFileCopier.copyFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new FsServiceException("Error, file already exists. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new FsServiceException("Database error when copying file. " + e.getMessage(), e);
		}
		return copy;
		
	}

	/**
	 * Moev file
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileEntry moveFile(Long fileId, Long dirId, boolean replaceExisting) throws FsServiceException {

		FsFileEntry move = null;
		try {
			move = fsFileMover.moveFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new FsServiceException("Error, file already exists. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new FsServiceException("Database error when moving file. " + e.getMessage(), e);
		}
		return move;		
		
	}

	/**
	 * Remove file
	 * 
	 * @param fileId
	 * @throws FsServiceException
	 */
	public void removeFile(Long fileId) throws FsServiceException {
		
		try {
			fsFileRemover.removeFile(fileId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Database error when removing file, id => " + fileId + ". " + e.getMessage(), e);
		}
		
	}

	/**
	 * Remove directory
	 * 
	 * @param dirId
	 * @throws FsServiceException
	 */
	public void removeDirectory(Long dirId) throws FsServiceException {
		
		try {
			fsDirectoryRemover.removeDirectory(dirId);
		} catch (DatabaseException e) {
			throw new FsServiceException("Database error when removing directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		
	}

	/**
	 * Copy directory
	 * 
	 * @param dirId
	 * @param dirId2
	 * @param replaceExisting
	 * @throws FsServiceException
	 */
	public void copyDirectory(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws FsServiceException {
		
		try {
			fsDirectoryCopier.copyDirectory(sourceDirId, targetDirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new FsServiceException("Error copying directory, file already exists in target dir. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error copying directory. " + e.getMessage(), e);
		}
		
	}

	/**
	 * Move directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 */
	public void moveDirectory(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws FsServiceException {
		
		try {
			fsDirectoryMover.moveDirectory(sourceDirId, targetDirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new FsServiceException("Error moving directory, file already exists in target dir. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new FsServiceException("Error moving directory. " + e.getMessage(), e);
		}		
		
	}
	
	/**
	 * Creates a sample file store, with some directories and files.
	 * 
	 * Directly uses CmsFileStoreAdder, CmsDirectoryAdder, and CmsFileAdder
	 * 
	 * @param storePath
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileStore createSampleFileStore(Path storePath) throws FsServiceException {
		
		FsFileStore fileStore = null;
		
		LocalDateTime timePoint = LocalDateTime.now();
		
		String pathPostfix = String.format("_%d.%s.%d_%d.%d.%d",
				timePoint.getYear(), timePoint.getMonth(), timePoint.getDayOfMonth(),
				timePoint.getHour(), timePoint.getMinute(), timePoint.getSecond());
		
		String dateTime = String.format("%s %s", timePoint.format( DateTimeFormatter.ISO_DATE ), 
				timePoint.format( DateTimeFormatter.ISO_TIME ));
		
		Path fullStorePath = Paths.get(storePath.toString() + pathPostfix);
		
		logger.info("Creating sample file store at => " + fullStorePath);

		// use cmsFileStoreRepository directly rather than calling service method, this will uses the
		// spring proxy and correct transaction
		try {
			
			fileStore = fsFileStoreAdder.createFileStore(
					fullStorePath, "Example File Store " + dateTime, 
					"This is an example file store, created at " + dateTime, true);
			
		} catch (DatabaseException e) {
			throw new FsServiceException("Error creating sample file store at => " + fullStorePath, e);
		}
		
		logger.info("Store, name => " + fileStore.getName() + " was successfully created at, path => " + fileStore.getStorePath());
		logger.info("Store root directory, id => " + fileStore.getRooDirId() + ", name => " + fileStore.getRootDir().getDirName() + 
				", relative path => " + fileStore.getRootDir().getRelativeDirPath());
	
		logger.info("Adding some directories...");
		
		// use cmsDirectoryAdder directly rather than calling service method, this will uses the
		// spring proxy and correct transaction
		try {
			
			FsDirectory sampleDir1 = fsDirectoryAdder.addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 1");
				fsDirectoryAdder.addDirectory(sampleDir1.getDirId(), "Sample directory 1-1");
				fsDirectoryAdder.addDirectory(sampleDir1.getDirId(), "Sample directory 1-2");
			
			FsDirectory sampleDir2 = fsDirectoryAdder.addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 2");
				fsDirectoryAdder.addDirectory(sampleDir2.getDirId(), "Sample directory 2-1");
				fsDirectoryAdder.addDirectory(sampleDir2.getDirId(), "Sample directory 2-2");
			
			FsDirectory sampleDir3 = fsDirectoryAdder.addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 3");
				fsDirectoryAdder.addDirectory(sampleDir3.getDirId(), "Sample directory 3-1");
				fsDirectoryAdder.addDirectory(sampleDir3.getDirId(), "Sample directory 3-2");
				
		} catch (DatabaseException e) {
			throw new FsServiceException("Error adding new directory. " + e.getMessage(), e);
		}
		
		Resource sampleImageResource = resourceLoader.getResource("classpath:image/");
		
		Path sampleImagePath = null;
		try {
			sampleImagePath = Paths.get(sampleImageResource.getFile().getAbsolutePath());
		} catch (IOException e) {
			throw new FsServiceException("Error attempting to get parent path for classpath images at src/main/resource/images", e);
		}
		
		// all images, at depth 1
		List<Path> listSampleImages = null;
		try {
			listSampleImages = FileUtil.listFilesToDepth(sampleImagePath, 1);
		} catch (IOException e) {
			throw new FsServiceException("Error attempting to get list of paths for sample images in src/main/resource/images", e);
		}
		
		if(listSampleImages == null || listSampleImages.size() < 9){
			throw new FsServiceException("No images in classpath images folder at src/main/resources/images, or less than 9 images. Need at least 9.");
		}
		
		Iterator<Path> imagePathItr = listSampleImages.iterator();
			
		Tree<FsDirectory> directoryTree = getTree(fileStore.getRootDir().getDirId());
		
		// walk tree and add sample images to each of the directories
		try {
			
			Trees.walkTree(directoryTree,
					(treeNode) -> {
						
						// skip root node, only add files to child nodes
						if(!treeNode.getData().isRootNode()){
						
							Path nextImagePath = imagePathItr.hasNext() ? imagePathItr.next() : null;
							
							if(nextImagePath != null){
								try {
									
									fsFileAdder.addFile(nextImagePath, treeNode.getData().getDirId(), true);
									
								} catch (DatabaseException e) {
									throw new TreeNodeVisitException("DatabaseException while adding file " + nextImagePath + 
											" to directory " + treeNode.getData().getDirName(), e);
								} catch (IOException e) {
									throw new TreeNodeVisitException("IOException while adding file " + nextImagePath + 
											" to directory " + treeNode.getData().getDirName(), e);						
								}
							}
							
						}
						
					},
					WalkOption.PRE_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new FsServiceException("Error while adding sample images to sample file store.", e);
		}
		
		return fileStore;
		
	}
	
	/**
	 * Creates a sample file store, with some directories and files.
	 * 
	 * Does NOT, directly use CmsFileStoreAdder, CmsDirectoryAdder, and CmsFileAdder. Instead we call the service
	 * methods from this class.
	 * 
	 * @param storePath
	 * @return
	 * @throws FsServiceException
	 */
	public FsFileStore createSampleFileStoreAlt(Path storePath) throws FsServiceException {
		
		FsFileStore fileStore = null;
		
		LocalDateTime timePoint = LocalDateTime.now();
		
		String pathPostfix = String.format("_%d.%s.%d_%d.%d.%d",
				timePoint.getYear(), timePoint.getMonth(), timePoint.getDayOfMonth(),
				timePoint.getHour(), timePoint.getMinute(), timePoint.getSecond());
		
		String dateTime = String.format("%s %s", timePoint.format( DateTimeFormatter.ISO_DATE ), 
				timePoint.format( DateTimeFormatter.ISO_TIME ));
		
		Path fullStorePath = Paths.get(storePath.toString() + pathPostfix);
		
		logger.info("Creating sample file store at => " + fullStorePath);

		fileStore = createFileStore(
				fullStorePath, "Example File Store " + dateTime, 
				"This is an example file store, created at " + dateTime, true);
		
		logger.info("Store, name => " + fileStore.getName() + " was successfully created at, path => " + fileStore.getStorePath());
		logger.info("Store root directory, id => " + fileStore.getRooDirId() + ", name => " + fileStore.getRootDir().getDirName() + 
				", relative path => " + fileStore.getRootDir().getRelativeDirPath());
	
		logger.info("Adding some directories...");
	
		FsDirectory sampleDir1 = addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 1");
			addDirectory(sampleDir1.getDirId(), "Sample directory 1-1");
			addDirectory(sampleDir1.getDirId(), "Sample directory 1-2");
		
		FsDirectory sampleDir2 = addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 2");
			addDirectory(sampleDir2.getDirId(), "Sample directory 2-1");
			addDirectory(sampleDir2.getDirId(), "Sample directory 2-2");
		
		FsDirectory sampleDir3 = addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 3");
			addDirectory(sampleDir3.getDirId(), "Sample directory 3-1");
			addDirectory(sampleDir3.getDirId(), "Sample directory 3-2");
		
		Resource sampleImageResource = resourceLoader.getResource("classpath:image/");
		
		Path sampleImagePath = null;
		try {
			sampleImagePath = Paths.get(sampleImageResource.getFile().getAbsolutePath());
		} catch (IOException e) {
			throw new FsServiceException("Error attempting to get parent path for classpath images at src/main/resource/images", e);
		}
		
		// all images, at depth 1
		List<Path> listSampleImages = null;
		try {
			listSampleImages = FileUtil.listFilesToDepth(sampleImagePath, 1);
		} catch (IOException e) {
			throw new FsServiceException("Error attempting to get list of paths for sample images in src/main/resource/images", e);
		}
		
		if(listSampleImages == null || listSampleImages.size() < 9){
			throw new FsServiceException("No images in classpath images folder at src/main/resources/images, or less than 9 images. Need at least 9.");
		}
		
		Iterator<Path> imagePathItr = listSampleImages.iterator();
			
		Tree<FsDirectory> directoryTree = getTree(fileStore.getRootDir().getDirId());
		
		// walk tree and add sample images to each of the directories
		try {
			
			Trees.walkTree(directoryTree,
					(treeNode) -> {
						
						// skip root node, only add files to child nodes
						if(!treeNode.getData().isRootNode()){
						
							Path nextImagePath = imagePathItr.hasNext() ? imagePathItr.next() : null;
							
							if(nextImagePath != null){
								try {
									
									addFile(nextImagePath, treeNode.getData().getDirId(), true);
									
								} catch (FsServiceException e) {
									throw new TreeNodeVisitException("Error while adding file " + nextImagePath + 
											" to directory " + treeNode.getData().getDirName(), e);
								}
							}
							
						}
						
					},
					WalkOption.PRE_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new FsServiceException("Error while adding sample images to sample file store.", e);
		}
		
		return fileStore;
		
	}

}
