/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.nio.file.FileAlreadyExistsException;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author slenzi
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsDirectoryResourceMover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7783725686593002491L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
	@Autowired
	private FsDirectoryResourceAdder fsDirectoryResourceAdder;
	
	@Autowired
	private FsDirectoryResourceRemover fsDirectoryResourceRemover;
	
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsFileResourceMover fsFileResourceMover;	

	/**
	 * 
	 */
	public FsDirectoryResourceMover() {
		
	}
	
	/**
	 * Move directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	public void moveDirectory(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {

		if(sourceDirId == null){
			throw new DatabaseException("Cannot move directory, source dir id param is null");
		}
		if(targetDirId == null){
			throw new DatabaseException("Cannot move directory, target dir id param is null");
		}		
		
		// build tree for source directory, this is the tree we want to move, in pre-order traversal (top down)
		Tree<FsPathResource> sourceTree = fsDirectoryResourceRepository.getTree(sourceDirId);
		
		//logger.info("Copying the following tree:\n" + sourceTree.printTree());
		
		FsResourceStore sourceStore = fsResourceStoreRepository.getStoreByDirectoryId(targetDirId);
		FsResourceStore targetStore = fsResourceStoreRepository.getStoreByDirectoryId(targetDirId);
		
		// start move at root node (dir) of source tree, and walk tree in pre-order traversal
		moveDirectoryTraversal(sourceTree.getRootNode(), targetDirId, sourceStore, targetStore, replaceExisting);
		
		// remove source dir and all child dirs.
		fsDirectoryResourceRemover.removeDirectory(sourceTree.getRootNode().getData().getNodeId());
		
	}
	
	/**
	 * 
	 * @param pathResourceNode
	 * @param targetParentDirId
	 * @param sourceStore
	 * @param targetStore
	 * @param replaceExisting
	 * @throws DatabaseException
	 * @throws FileAlreadyExistsException
	 */
	private void moveDirectoryTraversal(TreeNode<FsPathResource> pathResourceNode, Long targetParentDirId, 
			FsResourceStore sourceStore, FsResourceStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		Long nextTargetParentDirId = null;
		
		// flush any pending operations
		getEntityManager().flush();
		
		FsPathResource resourceToMove = pathResourceNode.getData();

		if(resourceToMove.getPathType().equals(FsPathType.DIRECTORY)){
			
			// move directory
			
			// move the dir to the target dir, returning the id of the child dir under the target dir (which might be a new dir)
			nextTargetParentDirId = move(pathResourceNode.getData().getNodeId(), targetParentDirId, sourceStore, targetStore, replaceExisting);
			
			// walk tree and move child directories and files
			if(pathResourceNode.hasChildren()){
				for(TreeNode<FsPathResource> child : pathResourceNode.getChildren()){
					moveDirectoryTraversal(child, nextTargetParentDirId, sourceStore, targetStore, replaceExisting);
				}
			}			
			
		}else if(resourceToMove.getPathType().equals(FsPathType.FILE)){
			
			// move/merge file
			
			//fsFileResourceCopier.copyReplaceTraversal(resourceToCopy.getNodeId(), targetParentDirId, sourceStore, targetStore, replaceExisting);
			
			fsFileResourceMover.moveReplaceTraversal(resourceToMove.getNodeId(), targetParentDirId, sourceStore, targetStore, replaceExisting);
			
		}else{
			
			throw new DatabaseException("Cannot move path resource, id => " + resourceToMove.getNodeId() + 
					", name => " + resourceToMove.getName() + ", type => " + resourceToMove.getPathType().getType() + 
					". Don't know how to move path type.");
			
		}
	
	}
	
	/**
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
			Long sourceDirId, Long targetParentDirId, 
			FsResourceStore sourceStore, FsResourceStore targetStore, boolean replaceExisting) throws DatabaseException, FileAlreadyExistsException {
		
		FsDirectoryResource sourceDir = null;
		FsDirectoryResource targetParentDir = null;
		FsDirectoryResource existingDir = null;
		
		sourceDir = fsDirectoryResourceRepository.getDirectoryResourceById(sourceDirId);
		targetParentDir = fsDirectoryResourceRepository.getDirectoryResourceById(targetParentDirId);
		existingDir = fsDirectoryResourceRepository.haveExistingChildDirectory(sourceDir.getName(), targetParentDirId, false);
		
		boolean needMergeDirectory = existingDir != null ? true : false;
		
		if(needMergeDirectory){
			
			// target dir already exists. don't need to do anything.
			
			return existingDir.getDirId();
			
		}else{
			
			// create target dir
			
			FsDirectoryResource newDir = fsDirectoryResourceAdder.add(targetParentDir, targetStore, sourceDir.getName());
			
			return newDir.getDirId();
			
		}
	
	}

}
