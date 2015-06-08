package org.lenzi.fstore.file.repository;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
import org.lenzi.fstore.file.repository.FsFileEntryRepository.FsFileEntryFetch;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For moving directories.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsDirectoryMover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1132760623198092221L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsDirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;		
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;	
	
	@Autowired
	private FsDirectoryRepository fsDirectoryRepository;
	
	@Autowired
	private FsFileEntryRepository fsFileEntryRepository;	
	
	@Autowired
	private FsDirectoryAdder fsDirectoryAdder;
	
	@Autowired
	private FsDirectoryRemover fsDirectoryRemover;
	
	@Autowired
	private FsFileMover fsFileMover;
	
	@Autowired
	private FsHelper fsHelper;	
	
	
	public FsDirectoryMover() {
		
	}
	
	/**
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 * @throws DatabaseException
	 */
	public void moveDirectory(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		if(sourceDirId == null){
			throw new DatabaseException("Cannot copy directory, source dir id param is null");
		}
		if(targetDirId == null){
			throw new DatabaseException("Cannot copy directory, target dir id param is null");
		}		
		
		// build tree for source directory, this is the tree we want to move, in pre-order traversal (top down)
		Tree<FsDirectory> sourceTree = fsDirectoryRepository.getTree(sourceDirId);
		
		// make sure user is not moving a root directory of a file store
		if(sourceTree.getRootNode().getData().isRootNode()){
			throw new DatabaseException("Cannot move directory, id => " + sourceDirId + ". This is a root directory of a file store.");
		}
		
		logger.info("Moving the following tree:\n" + sourceTree.printTree());
		
		FsFileStore sourceStore = fsFileStoreRepository.getFsFileStoreByDirId(sourceDirId);
		FsFileStore targetStore = fsFileStoreRepository.getFsFileStoreByDirId(targetDirId);
		
		// start move at root node (dir) of source tree, and walk tree in pre-order traversal
		moveDirectoryTraversal(sourceTree.getRootNode(), targetDirId, sourceStore, targetStore, replaceExisting);
		
		// delete old source tree.
		fsDirectoryRemover.removeDirectory(sourceTree.getRootNode().getData().getDirId());
		
	}
	
	/**
	 * Helper method for move directory operation.
	 * 
	 * Walk in pre-order traversal (top-down) and copy over one folder at a time. If folder with same name exists in target directory
	 * then then the files will be merged (and possibly replaced if 'replaceExisting' is true.) If there is no folder in the target
	 * directory with the same name then a new directory with the same name is created, then the files are moved over.
	 * 
	 * Once all files have been moved over, the empty source directory tree is removed.
	 * 
	 * @param dirToMoveNode
	 * @param targetParentDirId
	 * @param sourceStore
	 * @param targetStore
	 * @param replaceExisting
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	private void moveDirectoryTraversal(TreeNode<FsDirectory> dirToMoveNode, Long targetParentDirId, 
			FsFileStore sourceStore, FsFileStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		Long nextTargetParentDirId = null;
		
		// flush any pending operations
		getEntityManager().flush();
		
		// move the dir to the target dir, returning the id of the dir in it's new location
		nextTargetParentDirId = move(dirToMoveNode.getData().getDirId(), targetParentDirId, sourceStore, targetStore, replaceExisting);
		
		// walk tree and copy child directories and files
		if(dirToMoveNode.hasChildren()){
			for(TreeNode<FsDirectory> child : dirToMoveNode.getChildren()){
				moveDirectoryTraversal(child, nextTargetParentDirId, sourceStore, targetStore, replaceExisting);
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
	private Long move(
			Long sourceDirId, Long targetDirId, 
			FsFileStore sourceStore, FsFileStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		FsDirectory sourceDir = null, targetDir = null, existingDir = null;
		
		// fetch dir with all its file meta data
		sourceDir = fsDirectoryRepository.getFsDirectoryById(sourceDirId, FsDirectoryFetch.FILE_META);
		
		// check if target dir already contains a child dir with the same name as the dir we are copying
		targetDir = treeRepository.getNodeWithChild(new FsDirectory(targetDirId), 1);
		existingDir = targetDir.getChildDirectoryByName(sourceDir.getDirName(), false);
		
		boolean needMergeDirectory = existingDir != null ? true : false;
		
		// merge contents of dirToCopy into existing directory
		if(needMergeDirectory){
			
			for(FsFileEntry entryToCopy : sourceDir.getFileEntries()){
				
				fsFileMover.moveReplaceTraversal(entryToCopy.getFileId(), sourceDir.getDirId(), targetDir.getDirId(), 
						sourceStore, targetStore, replaceExisting);
			}
			
			return existingDir.getDirId();
		
		// create new directory under target parent dir, then copy over files
		}else{
			
			// create new directory copy
			FsDirectory newDirCopy = fsDirectoryAdder.add(targetDir, targetStore, sourceDir.getDirName());
			
			Path sourceFilePath = null;
			Path targetFilePath = null;
			FsFileEntry entryWithData = null;
			
			Collection<FsFileEntry> fileList = new ArrayList<FsFileEntry>();
			fileList.addAll(sourceDir.getFileEntries());
			
			// loop through source files and copy to new directory
			for(FsFileEntry entryToCopy : fileList){
				
				// fetch file entry with its byte[] file data
				entryWithData = fsFileEntryRepository.getFsFileEntryById(entryToCopy.getFileId(), FsFileEntryFetch.FILE_META_WITH_DATA);
				
				// build full paths to source and target files on file system
				sourceFilePath = fsHelper.getAbsoluteFilePath(sourceStore, sourceDir, entryWithData);
				targetFilePath = fsHelper.getAbsoluteFilePath(targetStore, newDirCopy, entryWithData); // use same file name
				
				// perform actual copy operation
				fsFileMover.move(sourceDir, newDirCopy, entryWithData, sourceFilePath, targetFilePath);
				
			}
			
			// return id of newly created directory
			return newDirCopy.getDirId();
			
		}
	
	}	

}
