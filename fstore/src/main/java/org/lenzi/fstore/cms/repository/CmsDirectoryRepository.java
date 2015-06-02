package org.lenzi.fstore.cms.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.lenzi.fstore.cms.repository.CmsFileEntryRepository.CmsFileEntryFetch;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry_;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.FileStoreHelper;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.tree.Tree;
import org.lenzi.fstore.tree.TreeNode;
import org.lenzi.fstore.tree.TreeNodeVisitException;
import org.lenzi.fstore.tree.Trees;
import org.lenzi.fstore.tree.Trees.WalkOption;
import org.lenzi.fstore.util.FileUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with cms directory operations.
 * 
 * All operations are wrapped in a new transaction.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class CmsDirectoryRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4125176200354740667L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;	
	
	@Autowired
	private CmsFileEntryRepository cmsFileEntryRepository;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;
	
	@Autowired
	private FileStoreHelper fileStoreHelper;
	
	public enum CmsDirectoryFetch {
		
		// just directory meta, no file entries
		FILE_NONE,		
		
		// just meta data for each file
		FILE_META,
		
		// meta data and byte data
		FILE_META_WITH_DATA
		
	}
	
	public CmsDirectoryRepository() {
	
	}
	
	/**
	 * Get the full absolute path for a cms directory.
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 * @throws DatabaseException
	 */
	public Path getAbsoluteDirectoryPath(CmsFileStore cmsStore, CmsDirectory cmsDirectory) {
		
		return fileStoreHelper.getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
	}
	
	/**
	 * Get the full absolute path for a cms directory.
	 * 
	 * @param cmsDirId - id of the cms directory
	 * @return
	 * @throws DatabaseException
	 */
	public Path getAbsoluteDirectoryPath(Long cmsDirId) throws DatabaseException {
		
		// get directory
		CmsDirectory cmsDirectory = null;
		try {
			cmsDirectory = getCmsDirectoryById(cmsDirId, CmsDirectoryFetch.FILE_META);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to retrieve CmsDirectory", e);
		}
		
		// get file store for directory
		CmsFileStore store = null;
		try {
			store = cmsFileStoreRepository.getCmsFileStoreByDirId(cmsDirectory.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + cmsDirectory.getDirId(), e);
		}
		
		return fileStoreHelper.getAbsoluteDirectoryPath(store, cmsDirectory);
		
	}	
	
	/**
	 * Fetch a cms directory by a file id.
	 * 
	 * @param fileId - id of the file in the directory
	 * @param fetch - specify what to fetch for the cms directory
	 * @return the cms directory entry that the file is in.
	 * @throws DatabaseException
	 */
	public CmsDirectory getCmsDirectoryByFileId(Long fileId, CmsDirectoryFetch fetch) throws DatabaseException {
		
		// TODO - check if this returns all file entries, or just the one with the specified file id
		
		/*
	 	select d from CmsDirectory as d
		join fetch d.fileEntries e
		where e.fileId = 1
		*/
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<CmsDirectory> query = criteriaBuilder.createQuery(CmsDirectory.class);
		Root<CmsDirectory> root = query.from(CmsDirectory.class);
		
		SetJoin<CmsDirectory,CmsFileEntry> fileEntries = root.join(CmsDirectory_.fileEntries, JoinType.LEFT);
		
		switch(fetch){
		
			// just directory meta, no join
			case FILE_NONE:
				break;
		
			// just file meta data
			case FILE_META:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
			
			// file meta data, and file byte data
			case FILE_META_WITH_DATA:
				Fetch<CmsDirectory,CmsFileEntry> metaFetch = root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				metaFetch.fetch(CmsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just file meta data
			default:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
				
		}		
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(fileEntries.get(CmsFileEntry_.fileId), fileId)
				);
		
		CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		
		return result;
		
	}

	/**
	 * Fetch a CmsDirectory
	 * 
	 * @param dirId - directory (node) id
	 * @param fetch - specify which file data to fetch for the directory
	 * @return
	 * @throws DatabaseException
	 */
	public CmsDirectory getCmsDirectoryById(Long dirId, CmsDirectoryFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<CmsDirectory> query = criteriaBuilder.createQuery(CmsDirectory.class);
		Root<CmsDirectory> root = query.from(CmsDirectory.class);		
		
		switch(fetch){
		
			// just directory meta, no join
			case FILE_NONE:
				break;
		
			// just file meta data
			case FILE_META:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
			
			// file meta data, and file byte data
			case FILE_META_WITH_DATA:
				Fetch<CmsDirectory,CmsFileEntry> metaFetch = root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				metaFetch.fetch(CmsFileEntry_.file, JoinType.LEFT);
				break;
			
			// default to just file meta data
			default:
				root.fetch(CmsDirectory_.fileEntries, JoinType.LEFT);
				break;
				
		}
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(CmsDirectory_.nodeId), dirId)
				);
		
		CmsDirectory result = getEntityManager().createQuery(query).getSingleResult();
		
		return result;		
		
	}
	
	/**
	 * Add a new directory.
	 * 
	 * @param parentDirId - The parent directory under which the new child directory will be created.
	 * @param dirName - The name of the new directory
	 * @return - reference to the newly created directory object
	 * @throws DatabaseException - if something goes wrong...
	 */
	public CmsDirectory addDirectory(Long parentDirId, String dirName) throws DatabaseException {
		
		// TODO - check if parent dir already contains dir with same name
		
		if(parentDirId == null){
			throw new DatabaseException("Parent dir id param is null.");
		}
		if(dirName == null){
			throw new DatabaseException("Dir name param is null.");
		}
		
		// get parent dir
		CmsDirectory parentDir = null;
		try {
			parentDir = getCmsDirectoryById(parentDirId, CmsDirectoryFetch.FILE_NONE);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch parent cms directory.", e);
		}
		
		// get file store
		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsFileStoreByDirId(parentDir.getDirId());
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch file store for cms dir id => " + parentDir.getDirId(), e);
		}
		
		return add(parentDir, cmsStore, dirName);
		
	}
	
	/**
	 * Remove a directory
	 * 
	 * @param dirId
	 * @throws DatabaseException
	 */
	public void removeDirectory(Long dirId) throws DatabaseException {
		
		Tree<CmsDirectory> dirTree = fileStoreHelper.getTree(dirId);
		
		logger.info(dirTree.printTree());
		
		CmsDirectory cmsDirectory = getCmsDirectoryById(dirId, CmsDirectoryFetch.FILE_META);
		CmsFileStore cmsStore = cmsFileStoreRepository.getCmsFileStoreByDirId(cmsDirectory.getDirId());
		
		//
		// walk tree in post-order traversal, deleting one directory at a time
		//
		try {
			
			Trees.walkTree(dirTree,
					(treeNode) -> {
						
						CmsDirectory dirToDelete = treeNode.getData();
						
						try {
							
							remove(cmsStore, dirToDelete);
							
						} catch (DatabaseException e) {
							throw new TreeNodeVisitException(e.getMessage(), e);
						}
						
					},
					WalkOption.POST_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new DatabaseException("Error deleting directory, id => " + dirTree.getRootNode().getData().getDirId() + 
					", name => " + dirTree.getRootNode().getData().getDirName(), e);
		}
		
	}
	
	// TODO - implement
	public void moveDirectory(Long dirId, Long newParentDirId, boolean replaceExisting) throws DatabaseException {
		
		// make sure not a root directory
		
		// if directory already exists, merge files
		
	}
	
	/**
	 * Copy a directory
	 * 
	 * @param sourceDirId - id of the directory to copy
	 * @param targetDirId - id of the directory where the copy will be made
	 * @param replaceExisting - true
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	public void copyDirectory(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		if(sourceDirId == null){
			throw new DatabaseException("Cannot copy directory, source dir id param is null");
		}
		if(targetDirId == null){
			throw new DatabaseException("Cannot copy directory, target dir id param is null");
		}		
		
		// build tree for source directory, this is the tree we want to copy, in pre-order traversal (top down)
		Tree<CmsDirectory> sourceTree = fileStoreHelper.getTree(sourceDirId);
		logger.info("Source tree:\n" + sourceTree.printTree());
		
		CmsFileStore sourceStore = cmsFileStoreRepository.getCmsFileStoreByDirId(sourceDirId);
		CmsFileStore targetStore = cmsFileStoreRepository.getCmsFileStoreByDirId(targetDirId);
		
		// start copy at root node (dir) of source tree, and walk tree in pre-order traversal
		copyDirectoryTraversal(sourceTree.getRootNode(), targetDirId, sourceStore, targetStore, replaceExisting);
		
	}
	
	/**
	 * Helper method for copy directory operation
	 * 
	 * @param dirToCopyNode
	 * @param targetParentDirId
	 * @param sourceStore
	 * @param targetStore
	 * @param replaceExisting
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	private void copyDirectoryTraversal(TreeNode<CmsDirectory> dirToCopyNode, Long targetParentDirId, 
			CmsFileStore sourceStore, CmsFileStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		Long nextTargetParentDirId = null;
		
		// flush any pending operations
		getEntityManager().flush();
		
		// copy the dir to the target dir, returning the id of the target dir (which might be a new directory)
		nextTargetParentDirId = copy(dirToCopyNode.getData().getDirId(), targetParentDirId, sourceStore, targetStore, replaceExisting);
		
		// walk tree and copy child directories and files
		if(dirToCopyNode.hasChildren()){
			for(TreeNode<CmsDirectory> child : dirToCopyNode.getChildren()){
				copyDirectoryTraversal(child, nextTargetParentDirId, sourceStore, targetStore, replaceExisting);
			}
		}
	
	}
	
	/**
	 * Copy directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param sourceStore
	 * @param targetStore
	 * @param replaceExisting
	 * @return
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	private Long copy(
			Long sourceDirId, Long targetDirId, 
			CmsFileStore sourceStore, CmsFileStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		CmsDirectory sourceDir = null, targetDir = null, existingDir = null;
		
		// fetch dir with all its file meta data
		sourceDir = getCmsDirectoryById(sourceDirId, CmsDirectoryFetch.FILE_META);
		
		// check if target dir already contains a child dir with the same name as the dir we are copying
		targetDir = treeRepository.getNodeWithChild(new CmsDirectory(targetDirId), 1);
		existingDir = targetDir.getChildDirectoryByName(sourceDir.getDirName(), false);
		
		boolean needMergeDirectory = existingDir != null ? true : false;
		
		// merge contents of dirToCopy into existing directory
		if(needMergeDirectory){
			
			String sourceDirPath = fileStoreHelper.getAbsoluteDirectoryString(sourceStore, sourceDir);
			String targetDirPath = fileStoreHelper.getAbsoluteDirectoryString(targetStore, existingDir);
			
			logger.info("Merging source dir, id => " + sourceDir.getDirId() + ", name => " + sourceDir.getDirName() + ", path => " + sourceDirPath
					+ ", into target dir, id => " + existingDir.getDirId() + ", name => " + existingDir.getDirName() + ", path => " + targetDirPath);
			
			Path sourceFilePath = null, targetFilePath = null, conflictTargetFilePath = null;
			CmsFileEntry sourceEntryWithData = null, conflictingTargetEntry = null;
			
			CmsDirectory targetDirWithFiles = getCmsDirectoryById(existingDir.getDirId(), CmsDirectoryFetch.FILE_META);
			
			for(CmsFileEntry entryToCopy : sourceDir.getFileEntries()){
				
				sourceEntryWithData = cmsFileEntryRepository.getCmsFileEntryById(entryToCopy.getFileId(), CmsFileEntryFetch.FILE_META_WITH_DATA);
				
				conflictingTargetEntry = targetDirWithFiles.getEntryByFileName(sourceEntryWithData.getFileName(), false);
				
				boolean needReplace = conflictingTargetEntry != null ? true : false;
				
				sourceFilePath = fileStoreHelper.getAbsoluteFilePath(sourceStore, sourceDir, sourceEntryWithData);
				targetFilePath = fileStoreHelper.getAbsoluteFilePath(targetStore, existingDir, sourceEntryWithData); // use same file name
				conflictTargetFilePath = fileStoreHelper.getAbsoluteFilePath(targetStore, existingDir, conflictingTargetEntry);
				
				// replace existing entry
				if(needReplace && replaceExisting){
				
					cmsFileEntryRepository.copyReplace(sourceDir, targetDirWithFiles, sourceEntryWithData, 
							conflictingTargetEntry, sourceFilePath, targetFilePath, conflictTargetFilePath);
					
				}else if(needReplace && !replaceExisting){
					
					throw new FileAlreadyExistsException("Target directory contains a file with the same name, but 'replaceExisting' param "
							+ "was false. Cannot move file to target directory.");
					
				// regular copy
				}else{
					
					cmsFileEntryRepository.copy(sourceDir, targetDirWithFiles, sourceEntryWithData, sourceFilePath, targetFilePath);
					
				}

			}
			
			return existingDir.getDirId();
		
		// create new directory under target parent dir, then copy over contents of dirToCopy
		}else{
			
			CmsDirectory newTargetDir = add(targetDir, targetStore, sourceDir.getDirName());
			
			Path sourceFilePath = null, targetFilePath = null;
			CmsFileEntry entryWithData = null;
			
			for(CmsFileEntry entryToCopy : sourceDir.getFileEntries()){
				
				entryWithData = cmsFileEntryRepository.getCmsFileEntryById(entryToCopy.getFileId(), CmsFileEntryFetch.FILE_META_WITH_DATA);
				
				sourceFilePath = fileStoreHelper.getAbsoluteFilePath(sourceStore, sourceDir, entryWithData);
				targetFilePath = fileStoreHelper.getAbsoluteFilePath(targetStore, newTargetDir, entryWithData); // use same file name
				
				cmsFileEntryRepository.copy(sourceDir, newTargetDir, entryWithData, sourceFilePath, targetFilePath);
				
			}
			
			return newTargetDir.getDirId();
			
		}
	
	}	
	
	/**
	 * Add directory
	 * 
	 * @param parentDir
	 * @param cmsStore
	 * @param dirName
	 * @return
	 * @throws DatabaseException
	 */
	private CmsDirectory add(CmsDirectory parentDir, CmsFileStore cmsStore, String dirName) throws DatabaseException {
		
		logger.info("Adding child dir " + dirName + " to parent dir " + parentDir.getName() + " for store " + cmsStore.getName());
		
		// CmsDirectory.getRelativeDirPath() returns a path relative to the store path
		Path storePath = Paths.get(cmsStore.getStorePath());
		Path childPath =  Paths.get(cmsStore.getStorePath() + parentDir.getRelativeDirPath() + File.separator + dirName);
		Path childRelativePath = storePath.relativize(childPath);
		String sChildRelativePath = childRelativePath.toString();
		if(!sChildRelativePath.startsWith(File.separator)){
			sChildRelativePath = File.separator + sChildRelativePath;
		}
		
		// add new child dir
		logger.info("Child dir path => " + childPath.toString());
		CmsDirectory childDir = null;
		try {
			
			childDir = treeRepository.addChildNode(parentDir, new CmsDirectory(dirName, sChildRelativePath) );
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Error adding new directory to parent dir => " + parentDir.getDirId(), e);
		}
		
		try {
			createDirOnFileSystem(childPath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		return childDir;
		
	}
	
	/**
	 * Remove directory. All child directories should be removed first!
	 * 
	 * @param cmsStore
	 * @param dirToDelete
	 * @throws DatabaseException
	 */
	private void remove(CmsFileStore cmsStore, CmsDirectory dirToDelete) throws DatabaseException {
		
		String dirPath = fileStoreHelper.getAbsoluteDirectoryString(cmsStore, dirToDelete);
		
		logger.info("Removing directory, id => " + dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName() +
				", path => " + dirPath);
		
		// remove all files in the directory
		for(CmsFileEntry fileEntryToDelete : dirToDelete.getFileEntries()){
			try {
				
				cmsFileEntryRepository.remove(cmsStore, dirToDelete, fileEntryToDelete);
				
			} catch (DatabaseException e) {
				throw new DatabaseException("Error deleting CmsFileEntry, file id => " + 
						fileEntryToDelete.getFileId() + ", name => " + fileEntryToDelete.getFileName() +
						", in CmsDirectory, dir id => " + dirToDelete.getDirId() + ", name => " +
						dirToDelete.getDirName(), e);
			}
		}
		
		// remove the directory
		try {
			treeRepository.removeNode(dirToDelete);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error deleting CmsDirectory, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName(), e);
		}
		
		// remove dir on file system
		try {
			FileUtil.deletePath(Paths.get(dirPath));
		} catch (IOException e) {
			throw new DatabaseException("Error deleting physical directory on file system for CmsDirectory, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName() + ", path => " + dirPath, e);
		}
		
	}	
	
	/**
	 * Create directory on file system (plus all parent directories if they don't exist.)
	 * 
	 * @param path - the directory to create
	 * @param clearIfExists - clear the directory if not empty
	 * @throws IOException
	 * @throws SecurityException
	 */
	private void createDirOnFileSystem(Path path, boolean clearIfExists) throws IOException, SecurityException {
		
		FileUtil.createDirectory(path, clearIfExists);

		boolean canReadWrite = Files.isReadable(path) && Files.isWritable(path);
		if(!canReadWrite){
			throw new SecurityException("Cannot read and write to directory " + path.toString());
		}		
		
	}	

}
