/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.io.IOException;
import java.nio.file.Path;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

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
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource_;
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
		
		logger.info("Resource tree to delete:");
		logger.info(resourceTree.printTree());
		
		FsPathResource rootResource = resourceTree.getRootNode().getData();
		FsDirectoryResource rootDir = null;
		try {
			rootDir = (FsDirectoryResource) rootResource;
		} catch (ClassCastException e) {
			throw new DatabaseException("Error deleting directory resource. " + FsPathResource.class.getName() + 
					" with node id " + rootResource.getNodeId() + " does not appear to be a " + FsDirectoryResource.class.getName());
		}
		
		logger.info("Root Node: id => " + rootDir.getDirId() + ", name => " + rootDir.getName());
		
		FsResourceStore fsStore = fsResourceStoreRepository.getStoreByPathResourceId(rootDir.getDirId());
		
		//
		// Walk tree in post-order traversal, deleting resources from the bottom up.
		//
		try {
			
			Trees.walkTree(resourceTree,
					(treeNode) -> {
						
						FsPathResource resourceToDelete = treeNode.getData();
						
						logger.info("Deleting path resource " + ", id => " + resourceToDelete.getNodeId() + ", name => " + resourceToDelete.getName() + ", type => " + resourceToDelete.getPathType().getType());
						
						try {
							
							if(resourceToDelete.getPathType().equals(FsPathType.DIRECTORY)){
								
								// remove dir
								removeDirectoryResource(resourceToDelete, fsStore);
								
							}else if(resourceToDelete.getPathType().equals(FsPathType.FILE)){
								
								// remove file
								removeFileResource(resourceToDelete, fsStore);
								
							}else{
								
								throw new DatabaseException("Unknown path resource type for node, id => " + 
										resourceToDelete.getNodeId() + ", type => " + resourceToDelete.getPathType().getType());
								
							}							
							
						} catch (DatabaseException e) {
							
							throw new TreeNodeVisitException("Error deleting path resource, id => " + 
									resourceToDelete.getNodeId() + ", type => " + resourceToDelete.getPathType().getType() + ". " + e.getMessage(), e);
							
						}
						
					},
					WalkOption.POST_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			
			throw new DatabaseException("Error deleting directory, id => " + rootDir.getDirId() + 
					", name => " + rootDir.getName(), e);
			
		}
		
	}
	
	/**
	 * Delete directory resource
	 * 
	 * @param resource
	 * @param fsStore
	 * @throws DatabaseException
	 */
	private void removeDirectoryResource(FsPathResource resource, FsResourceStore fsStore) throws DatabaseException {
		
		logger.info("Remove directory resource, id => " + resource.getNodeId() + 
				", name => " + resource.getName() + ", type => " + resource.getPathType().getType());
		
		// cast to directory resource
		FsDirectoryResource dirResource = null;
		try {
			dirResource = (FsDirectoryResource) resource;
		} catch (ClassCastException e) {
			throw new DatabaseException("Error deleting directory resource. " + FsPathResource.class.getName() + 
					" with node id " + resource.getNodeId() + " does not appear to be a " + FsDirectoryResource.class.getName());
		}
		
		// remove the directory
		try {
			treeRepository.removeNode(dirResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error deleting " + FsDirectoryResource.class.getName() + ", dir id => " + 
					dirResource.getDirId() + ", name => " + dirResource.getName(), e);
		}
		
		Path dirPath = fsResourceHelper.getAbsoluteDirectoryPath(fsStore, dirResource);
		
		// remove dir on file system
		try {
			FileUtil.deletePath(dirPath);
		} catch (IOException e) {
			throw new DatabaseException("Error deleting physical directory on file system for " + 
					FsDirectoryResource.class.getName() + ", dir id => " + 
					dirResource.getDirId() + ", name => " + dirResource.getName() + ", path => " + dirPath, e);
		}
		
	}
	
	/**
	 * Delete file resource
	 * 
	 * @param resource
	 * @param fsStore
	 * @throws DatabaseException
	 */
	private void removeFileResource(FsPathResource resource, FsResourceStore fsStore) throws DatabaseException {
		
		logger.info("Remove file resource, id => " + resource.getNodeId() + 
				", name => " + resource.getName() + ", type => " + resource.getPathType().getType());
		
		// cast to file meta resource
		FsFileMetaResource fileMetaResource = null;
		try {
			fileMetaResource = (FsFileMetaResource) resource;
		} catch (ClassCastException e) {
			throw new DatabaseException("Error deleting file resource. " + FsPathResource.class.getName() + 
					" with node id " + resource.getNodeId() + " does not appear to be a " + FsFileMetaResource.class.getName());
		}
		
		// remove FsFileResource data
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<FsFileResource> cmsFileDelete = cb.createCriteriaDelete(FsFileResource.class);
		Root<FsFileResource> cmsFileRoot = cmsFileDelete.from(FsFileResource.class);
		cmsFileDelete.where(cb.equal(cmsFileRoot.get(FsFileResource_.nodeId), fileMetaResource.getFileId()));
		executeUpdate(cmsFileDelete);
		
		// remove FsFileMetaResource tree node
		try {
			treeRepository.removeNode(fileMetaResource);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error deleting " + FsFileMetaResource.class.getName() + ", file id => " + 
					fileMetaResource.getFileId() + ", name => " + fileMetaResource.getName(), e);
		}
		
		Path filePath = fsResourceHelper.getAbsoluteFilePath(fsStore, fileMetaResource);
		
		// remove file on file system
		try {
			FileUtil.deletePath(filePath);
		} catch (IOException e) {
			throw new DatabaseException("Error deleting physical directory on file system for " + 
					FsDirectoryResource.class.getName() + ", dir id => " + 
					fileMetaResource.getFileId() + ", name => " + filePath.getFileName().toString() + ", path => " + filePath, e);
		}
		
	}

}
