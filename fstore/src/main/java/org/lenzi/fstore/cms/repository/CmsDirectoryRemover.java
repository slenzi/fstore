package org.lenzi.fstore.cms.repository;

import java.io.IOException;
import java.nio.file.Paths;

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.FileStoreHelper;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.tree.Tree;
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
 * For removing cms directories.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class CmsDirectoryRemover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7808176216105514149L;

	public CmsDirectoryRemover() {
		
	}
	
	@InjectLogger
	private Logger logger;	
	
	@Autowired
	private FileStoreHelper fileStoreHelper;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;	
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;
	
	@Autowired
	private CmsFileRemover cmsFileRemover;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;	
	
	/**
	 * Remove a directory
	 * 
	 * @param dirId
	 * @throws DatabaseException
	 */
	public void removeDirectory(Long dirId) throws DatabaseException {
		
		Tree<CmsDirectory> dirTree = cmsDirectoryRepository.getTree(dirId);
		
		logger.info(dirTree.printTree());
		
		CmsDirectory cmsDirectory = cmsDirectoryRepository.getCmsDirectoryById(dirId, CmsDirectoryFetch.FILE_META);
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
		if(dirToDelete.hasFileEntries()){
			for(CmsFileEntry fileEntryToDelete : dirToDelete.getFileEntries()){
				try {
					
					cmsFileRemover.remove(cmsStore, dirToDelete, fileEntryToDelete);
					
				} catch (DatabaseException e) {
					throw new DatabaseException("Error deleting CmsFileEntry, file id => " + 
							fileEntryToDelete.getFileId() + ", name => " + fileEntryToDelete.getFileName() +
							", in CmsDirectory, dir id => " + dirToDelete.getDirId() + ", name => " +
							dirToDelete.getDirName(), e);
				}
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

}
