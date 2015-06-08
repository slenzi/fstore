package org.lenzi.fstore.file.repository;

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
import org.lenzi.fstore.file.repository.FsDirectoryRepository.FsDirectoryFetch;
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
 * For removing directories.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsDirectoryRemover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7808176216105514149L;

	public FsDirectoryRemover() {
		
	}
	
	@InjectLogger
	private Logger logger;	
	
	@Autowired
	private FsHelper fsHelper;
	
	@Autowired
	private FsFileStoreRepository fsFileStoreRepository;	
	
	@Autowired
	private FsDirectoryRepository fsDirectoryRepository;
	
	@Autowired
	private FsFileRemover fsFileRemover;
	
	@Autowired
	@Qualifier("FsDirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;	
	
	/**
	 * Remove a directory
	 * 
	 * @param dirId
	 * @throws DatabaseException
	 */
	public void removeDirectory(Long dirId) throws DatabaseException {
		
		Tree<FsDirectory> dirTree = fsDirectoryRepository.getTree(dirId);
		
		logger.info(dirTree.printTree());
		
		FsDirectory cmsDirectory = fsDirectoryRepository.getFsDirectoryById(dirId, FsDirectoryFetch.FILE_META);
		FsFileStore cmsStore = fsFileStoreRepository.getFsFileStoreByDirId(cmsDirectory.getDirId());
		
		//
		// walk tree in post-order traversal, deleting one directory at a time
		//
		try {
			
			Trees.walkTree(dirTree,
					(treeNode) -> {
						
						FsDirectory dirToDelete = treeNode.getData();
						
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
	
	/**
	 * Remove directory. All child directories should be removed first!
	 * 
	 * @param cmsStore
	 * @param dirToDelete
	 * @throws DatabaseException
	 */
	private void remove(FsFileStore cmsStore, FsDirectory dirToDelete) throws DatabaseException {
		
		String dirPath = fsHelper.getAbsoluteDirectoryString(cmsStore, dirToDelete);
		
		logger.info("Removing directory, id => " + dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName() +
				", path => " + dirPath);
		
		// remove all files in the directory
		if(dirToDelete.hasFileEntries()){
			for(FsFileEntry fileEntryToDelete : dirToDelete.getFileEntries()){
				try {
					
					fsFileRemover.remove(cmsStore, dirToDelete, fileEntryToDelete);
					
				} catch (DatabaseException e) {
					throw new DatabaseException("Error deleting FsFileEntry, file id => " + 
							fileEntryToDelete.getFileId() + ", name => " + fileEntryToDelete.getFileName() +
							", in FsDirectory, dir id => " + dirToDelete.getDirId() + ", name => " +
							dirToDelete.getDirName(), e);
				}
			}
		}
		
		// remove the directory
		try {
			treeRepository.removeNode(dirToDelete);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error deleting FsDirectory, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName(), e);
		}
		
		// remove dir on file system
		try {
			FileUtil.deletePath(Paths.get(dirPath));
		} catch (IOException e) {
			throw new DatabaseException("Error deleting physical directory on file system for FsDirectory, dir id => " + 
					dirToDelete.getDirId() + ", name => " + dirToDelete.getDirName() + ", path => " + dirPath, e);
		}
		
	}	

}
