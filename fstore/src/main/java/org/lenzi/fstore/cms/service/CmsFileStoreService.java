package org.lenzi.fstore.cms.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import org.lenzi.fstore.cms.repository.CmsDirectoryAdder;
import org.lenzi.fstore.cms.repository.CmsDirectoryCopier;
import org.lenzi.fstore.cms.repository.CmsDirectoryMover;
import org.lenzi.fstore.cms.repository.CmsDirectoryRemover;
import org.lenzi.fstore.cms.repository.CmsDirectoryRepository;
import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.CmsFileAdder;
import org.lenzi.fstore.cms.repository.CmsFileCopier;
import org.lenzi.fstore.cms.repository.CmsFileEntryRepository;
import org.lenzi.fstore.cms.repository.CmsFileEntryRepository.CmsFileEntryFetch;
import org.lenzi.fstore.cms.repository.CmsFileMover;
import org.lenzi.fstore.cms.repository.CmsFileRemover;
import org.lenzi.fstore.cms.repository.CmsFileStoreAdder;
import org.lenzi.fstore.cms.repository.CmsFileStoreRepository;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNodeVisitException;
import org.lenzi.fstore.core.tree.Trees;
import org.lenzi.fstore.core.tree.Trees.WalkOption;
import org.lenzi.fstore.core.util.FileUtil;
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
public class CmsFileStoreService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;
	
	@Autowired
	private CmsFileEntryRepository cmsFileEntryRepository;
	
	@Autowired
	private CmsFileStoreAdder cmsFileStoreAdder;

	@Autowired
	private CmsFileAdder cmsFileAdder;	
	
	@Autowired
	private CmsFileCopier cmsFileCopier;
	
	@Autowired
	private CmsFileMover cmsFileMover;
	
	@Autowired
	private CmsFileRemover cmsFileRemover;
	
	@Autowired
	private CmsDirectoryAdder cmsDirectoryAdder;
	
	@Autowired
	private CmsDirectoryCopier cmsDirectoryCopier;

	@Autowired
	private CmsDirectoryMover cmsDirectoryMover;	
	
	@Autowired
	private CmsDirectoryRemover cmsDirectoryRemover;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	
	public CmsFileStoreService() {
		
	}
	
	/**
	 * Get printable tree in string format
	 */
	public String printTree(Long dirId) throws CmsServiceException {
		
		return getTree(dirId).printTree();
		
	}
	
	/**
	 * Get tree for directory
	 * 
	 * @param dirId
	 * @return
	 * @throws CmsServiceException
	 */
	public Tree<CmsDirectory> getTree(Long dirId) throws CmsServiceException {
		
		try {
			return cmsDirectoryRepository.getTree(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching tree for directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Get tree for directory, and include CmsFileEntry objects for each directory (just meta, no binary data.)
	 * 
	 * @param dirId
	 * @return
	 * @throws CmsServiceException
	 */
	public Tree<CmsDirectory> getTreeWithFileMeta(Long dirId) throws CmsServiceException {
		
		try {
			return cmsDirectoryRepository.getTreeWithFileMeta(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching tree for directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Get full path for directory
	 * 
	 * @param nodeId
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(Long dirId) throws CmsServiceException {
		
		Path path = null;
		try {
			path = cmsDirectoryRepository.getAbsoluteDirectoryPath(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching path for directory, id => " + dirId + ". " + e.getMessage(), e);
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
	public Path getAbsoluteDirectoryPath(CmsFileStore cmsStore, CmsDirectory cmsDirectory) {
		
		return cmsDirectoryRepository.getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
	}
	
	/**
	 * Get full path for file
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @param cmsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(CmsFileStore cmsStore, CmsDirectory cmsDirectory, CmsFileEntry cmsFileEntry){
		
		return cmsFileEntryRepository.getAbsoluteFilePath(cmsStore, cmsDirectory, cmsFileEntry);
		
	}
	
	/**
	 * Create new store.
	 * 
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore createFileStore(Path storePath, String name, String description, boolean clearIfExists) throws CmsServiceException {
		
		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreAdder.createFileStore(storePath, name, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error creating store. " + e.getMessage(), e);
		}
		return cmsStore;
		
	}

	/**
	 * Get store by id
	 * 
	 * @param storeId
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore getCmsStoreByStoreId(long storeId) throws CmsServiceException {
		
		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsStoreByStoreId(storeId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching store, id => " + storeId + ". " + e.getMessage(), e);
		}
		return cmsStore;
		
	}

	/**
	 * Get store by it's root directory
	 * 
	 * @param rootDirId
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore getCmsStoreByRootDirId(long rootDirId) throws CmsServiceException {

		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsStoreByRootDirId(rootDirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching store by root directory, id => " + rootDirId + ". " + e.getMessage(), e);
		}
		return cmsStore;		
		
	}
	
	/**
	 * Get store by any directory. does not have to be a root directory.
	 * 
	 * @param dirId
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore getCmsStoreByDirId(long dirId) throws CmsServiceException {

		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsFileStoreByDirId(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching store by directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return cmsStore;		
		
	}

	/**
	 * Fetch directory by id
	 * 
	 * @param dirId
	 * @param fetch
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsDirectory getCmsDirectoryById(long dirId, CmsDirectoryFetch fetch) throws CmsServiceException {
		
		CmsDirectory dir = null;
		try {
			dir = cmsDirectoryRepository.getCmsDirectoryById(dirId, fetch);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return dir;
		
	}

	/**
	 * Add new directory
	 * 
	 * @param parentDirId
	 * @param dirName
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsDirectory addDirectory(Long parentDirId, String dirName) throws CmsServiceException {

		CmsDirectory dir = null;
		try {
			dir = cmsDirectoryAdder.addDirectory(parentDirId, dirName);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error adding new directory. " + e.getMessage(), e);
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
	 * @throws CmsServiceException
	 */
	public CmsFileEntry addFile(Path sourcePath, Long dirId, boolean replaceExisting) throws CmsServiceException {

		CmsFileEntry fileEntry = null;
		try {
			fileEntry = cmsFileAdder.addFile(sourcePath, dirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when adding new file. " + e.getMessage(), e);
		} catch (IOException e) {
			throw new CmsServiceException("I/O error when adding new file. " + e.getMessage(), e);
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
	 * @throws CmsServiceException
	 */
	public List<CmsFileEntry> addFile(List<Path> filePaths, Long dirId, boolean replaceExisting) throws CmsServiceException {
		
		List<CmsFileEntry> entries = null;
		try {
			entries = cmsFileAdder.addFile(filePaths, dirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when adding new files. " + e.getMessage(), e);
		} catch (IOException e) {
			throw new CmsServiceException("I/O error when adding new files. " + e.getMessage(), e);
		}
		return entries;
		
	}

	/**
	 * Get a file entry
	 * 
	 * @param fileId
	 * @param fetch
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileEntry getCmsFileEntryById(Long fileId, CmsFileEntryFetch fetch) throws CmsServiceException {

		CmsFileEntry fileEntry = null;
		try {
			fileEntry = cmsFileEntryRepository.getCmsFileEntryById(fileId, fetch);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching file, id => " + fileId + ". " + e.getMessage(), e);
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
	 * @throws CmsServiceException
	 */
	public CmsFileEntry copyFile(Long fileId, Long dirId, boolean replaceExisting) throws CmsServiceException {
		
		CmsFileEntry copy = null;
		try {
			copy = cmsFileCopier.copyFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new CmsServiceException("Error, file already exists. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when copying file. " + e.getMessage(), e);
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
	 * @throws CmsServiceException
	 */
	public CmsFileEntry moveFile(Long fileId, Long dirId, boolean replaceExisting) throws CmsServiceException {

		CmsFileEntry move = null;
		try {
			move = cmsFileMover.moveFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new CmsServiceException("Error, file already exists. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when moving file. " + e.getMessage(), e);
		}
		return move;		
		
	}

	/**
	 * Remove file
	 * 
	 * @param fileId
	 * @throws CmsServiceException
	 */
	public void removeFile(Long fileId) throws CmsServiceException {
		
		try {
			cmsFileRemover.removeFile(fileId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when removing file, id => " + fileId + ". " + e.getMessage(), e);
		}
		
	}

	/**
	 * Remove directory
	 * 
	 * @param dirId
	 * @throws CmsServiceException
	 */
	public void removeDirectory(Long dirId) throws CmsServiceException {
		
		try {
			cmsDirectoryRemover.removeDirectory(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when removing directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		
	}

	/**
	 * Copy directory
	 * 
	 * @param dirId
	 * @param dirId2
	 * @param replaceExisting
	 * @throws CmsServiceException
	 */
	public void copyDirectory(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws CmsServiceException {
		
		try {
			cmsDirectoryCopier.copyDirectory(sourceDirId, targetDirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new CmsServiceException("Error copying directory, file already exists in target dir. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error copying directory. " + e.getMessage(), e);
		}
		
	}

	/**
	 * Move directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 */
	public void moveDirectory(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws CmsServiceException {
		
		try {
			cmsDirectoryMover.moveDirectory(sourceDirId, targetDirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new CmsServiceException("Error moving directory, file already exists in target dir. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error moving directory. " + e.getMessage(), e);
		}		
		
	}
	
	/**
	 * Creates a sample file store, with some directories and files.
	 * 
	 * Directly uses CmsFileStoreAdder, CmsDirectoryAdder, and CmsFileAdder
	 * 
	 * @param storePath
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore createSampleFileStore(Path storePath) throws CmsServiceException {
		
		CmsFileStore fileStore = null;
		
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
			
			fileStore = cmsFileStoreAdder.createFileStore(
					fullStorePath, "Example File Store " + dateTime, 
					"This is an example file store, created at " + dateTime, true);
			
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error creating sample file store at => " + fullStorePath, e);
		}
		
		logger.info("Store, name => " + fileStore.getName() + " was successfully created at, path => " + fileStore.getStorePath());
		logger.info("Store root directory, id => " + fileStore.getRooDirId() + ", name => " + fileStore.getRootDir().getDirName() + 
				", relative path => " + fileStore.getRootDir().getRelativeDirPath());
	
		logger.info("Adding some directories...");
		
		// use cmsDirectoryAdder directly rather than calling service method, this will uses the
		// spring proxy and correct transaction
		try {
			
			CmsDirectory sampleDir1 = cmsDirectoryAdder.addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 1");
				cmsDirectoryAdder.addDirectory(sampleDir1.getDirId(), "Sample directory 1-1");
				cmsDirectoryAdder.addDirectory(sampleDir1.getDirId(), "Sample directory 1-2");
			
			CmsDirectory sampleDir2 = cmsDirectoryAdder.addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 2");
				cmsDirectoryAdder.addDirectory(sampleDir2.getDirId(), "Sample directory 2-1");
				cmsDirectoryAdder.addDirectory(sampleDir2.getDirId(), "Sample directory 2-2");
			
			CmsDirectory sampleDir3 = cmsDirectoryAdder.addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 3");
				cmsDirectoryAdder.addDirectory(sampleDir3.getDirId(), "Sample directory 3-1");
				cmsDirectoryAdder.addDirectory(sampleDir3.getDirId(), "Sample directory 3-2");
				
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error adding new directory. " + e.getMessage(), e);
		}
		
		Resource sampleImageResource = resourceLoader.getResource("classpath:image/");
		
		Path sampleImagePath = null;
		try {
			sampleImagePath = Paths.get(sampleImageResource.getFile().getAbsolutePath());
		} catch (IOException e) {
			throw new CmsServiceException("Error attempting to get parent path for classpath images at src/main/resource/images", e);
		}
		
		// all images, at depth 1
		List<Path> listSampleImages = null;
		try {
			listSampleImages = FileUtil.listFilesToDepth(sampleImagePath, 1);
		} catch (IOException e) {
			throw new CmsServiceException("Error attempting to get list of paths for sample images in src/main/resource/images", e);
		}
		
		if(listSampleImages == null || listSampleImages.size() < 9){
			throw new CmsServiceException("No images in classpath images folder at src/main/resources/images, or less than 9 images. Need at least 9.");
		}
		
		Iterator<Path> imagePathItr = listSampleImages.iterator();
			
		Tree<CmsDirectory> directoryTree = getTree(fileStore.getRootDir().getDirId());
		
		// walk tree and add sample images to each of the directories
		try {
			
			Trees.walkTree(directoryTree,
					(treeNode) -> {
						
						// skip root node, only add files to child nodes
						if(!treeNode.getData().isRootNode()){
						
							Path nextImagePath = imagePathItr.hasNext() ? imagePathItr.next() : null;
							
							if(nextImagePath != null){
								try {
									
									cmsFileAdder.addFile(nextImagePath, treeNode.getData().getDirId(), true);
									
								} catch (DatabaseException e) {
									throw new TreeNodeVisitException("DatabaseException while adding file " + nextImagePath + 
											" to cms directory " + treeNode.getData().getDirName(), e);
								} catch (IOException e) {
									throw new TreeNodeVisitException("IOException while adding file " + nextImagePath + 
											" to cms directory " + treeNode.getData().getDirName(), e);						
								}
							}
							
						}
						
					},
					WalkOption.PRE_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new CmsServiceException("Error while adding sample images to sample file store.", e);
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
	 * @throws CmsServiceException
	 */
	public CmsFileStore createSampleFileStoreAlt(Path storePath) throws CmsServiceException {
		
		CmsFileStore fileStore = null;
		
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
	
		CmsDirectory sampleDir1 = addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 1");
			addDirectory(sampleDir1.getDirId(), "Sample directory 1-1");
			addDirectory(sampleDir1.getDirId(), "Sample directory 1-2");
		
		CmsDirectory sampleDir2 = addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 2");
			addDirectory(sampleDir2.getDirId(), "Sample directory 2-1");
			addDirectory(sampleDir2.getDirId(), "Sample directory 2-2");
		
		CmsDirectory sampleDir3 = addDirectory(fileStore.getRootDir().getDirId(), "Sample directory 3");
			addDirectory(sampleDir3.getDirId(), "Sample directory 3-1");
			addDirectory(sampleDir3.getDirId(), "Sample directory 3-2");
		
		Resource sampleImageResource = resourceLoader.getResource("classpath:image/");
		
		Path sampleImagePath = null;
		try {
			sampleImagePath = Paths.get(sampleImageResource.getFile().getAbsolutePath());
		} catch (IOException e) {
			throw new CmsServiceException("Error attempting to get parent path for classpath images at src/main/resource/images", e);
		}
		
		// all images, at depth 1
		List<Path> listSampleImages = null;
		try {
			listSampleImages = FileUtil.listFilesToDepth(sampleImagePath, 1);
		} catch (IOException e) {
			throw new CmsServiceException("Error attempting to get list of paths for sample images in src/main/resource/images", e);
		}
		
		if(listSampleImages == null || listSampleImages.size() < 9){
			throw new CmsServiceException("No images in classpath images folder at src/main/resources/images, or less than 9 images. Need at least 9.");
		}
		
		Iterator<Path> imagePathItr = listSampleImages.iterator();
			
		Tree<CmsDirectory> directoryTree = getTree(fileStore.getRootDir().getDirId());
		
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
									
								} catch (CmsServiceException e) {
									throw new TreeNodeVisitException("Error while adding file " + nextImagePath + 
											" to cms directory " + treeNode.getData().getDirName(), e);
								}
							}
							
						}
						
					},
					WalkOption.PRE_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new CmsServiceException("Error while adding sample images to sample file store.", e);
		}
		
		return fileStore;
		
	}

}
