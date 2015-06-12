/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.IOException;
import java.nio.file.Paths;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNodeVisitException;
import org.lenzi.fstore.core.tree.Trees;
import org.lenzi.fstore.core.tree.Trees.WalkOption;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
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
 * @author slenzi
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsDirectoryResourceRemover extends AbstractRepository {

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
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsFileResourceRemover fsFileResourceRemover;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;

	/**
	 * 
	 */
	public FsDirectoryResourceRemover() {
		
	}
	
	/**
	 * Remove a directory
	 * 
	 * @param dirId
	 * @throws DatabaseException
	 */
	public void removeDirectory(Long dirId) throws DatabaseException {
		
		Tree<FsPathResource> resourceTree = fsDirectoryResourceRepository.getTree(dirId);
		
		logger.info(resourceTree.printTree());
		
		FsDirectoryResource fsDirectory = fsDirectoryResourceRepository.getDirectoryResourceById(dirId);
		
		FsResourceStore fsStore = fsResourceStoreRepository.getStoreByDirectoryId(fsDirectory.getDirId());
		
		//
		// walk tree in post-order traversal, deleting one directory at a time
		//
		try {
			
			Trees.walkTree(resourceTree,
					(treeNode) -> {
						
						FsPathResource resourceToDelete = treeNode.getData();
						
						// TODO - must delete files before deleting dir!
						
						if(resourceToDelete.getPathType().equals(FsPathType.DIRECTORY)){
							
							// remove dir
							
						}else if(resourceToDelete.getPathType().equals(FsPathType.FILE)){
							
							// remove file
							
						}else{
							throw new DatabaseException("Unknown path resource type for node, id => " + 
									resourceToDelete.getNodeId() + ", type => " + resourceToDelete.getPathType().getType());
						}
						
						try {
							
							remove(fsStore, dirToDelete);
							
						} catch (DatabaseException e) {
							throw new TreeNodeVisitException(e.getMessage(), e);
						}
						
					},
					WalkOption.POST_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			throw new DatabaseException("Error deleting directory, id => " + dirTree.getRootNode().getData().getDirId() + 
					", name => " + dirTree.getRootNode().getData().getName(), e);
		}
		
	}
	
	/**
	 * Remove directory. All child directories should be removed first!
	 * 
	 * @param cmsStore
	 * @param dirToDelete
	 * @throws DatabaseException
	 */
	private void remove(FsResourceStore cmsStore, FsDirectoryResource dirToDelete) throws DatabaseException {
		
		String dirPath = fsResourceHelper.getAbsoluteDirectoryString(cmsStore, dirToDelete);
		
		logger.info("Removing directory, id => " + dirToDelete.getDirId() + ", name => " + dirToDelete.getName() +
				", path => " + dirPath);
		
		// remove all files in the directory
		if(dirToDelete.hasFileEntries()){
			for(FsFileEntry fileEntryToDelete : dirToDelete.getFileEntries()){
				try {
					
					fsFileResourceRemover.remove(cmsStore, dirToDelete, fileEntryToDelete);
					
				} catch (DatabaseException e) {
					throw new DatabaseException("Error deleting FsFileEntry, file id => " + 
							fileEntryToDelete.getFileId() + ", name => " + fileEntryToDelete.getFileName() +
							", in FsDirectoryResource, dir id => " + dirToDelete.getDirId() + ", name => " +
							dirToDelete.getName(), e);
				}
			}
		}
		
		// remove the directory
		try {
			treeRepository.removeNode(dirToDelete);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error deleting FsDirectoryResource, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getName(), e);
		}
		
		// remove dir on file system
		try {
			FileUtil.deletePath(Paths.get(dirPath));
		} catch (IOException e) {
			throw new DatabaseException("Error deleting physical directory on file system for FsDirectoryResource, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getName() + ", path => " + dirPath, e);
		}
		
	}

}
