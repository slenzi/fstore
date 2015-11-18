/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.logging.ClosureLogger;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.repository.tree.model.DBClosure;
import org.lenzi.fstore.core.service.TreeBuilder;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource_;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsDirectoryResourceRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7010548771117300824L;
	
	@InjectLogger
	private Logger logger;	
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private ClosureLogger<FsPathResource> closureLogger;
	
	@Autowired
	private TreeBuilder<FsPathResource> treeBuilder;
	
	
	/**
	 * 
	 */
	public FsDirectoryResourceRepository() {

	}
	
	/**
	 * Get tree for directory. Tree contains both directories and files so resulting tree type is of FsPathResource.
	 * 
	 * @param dirId
	 * @return
	 * @throws DatabaseException
	 */
	public Tree<FsPathResource> getTree(Long dirId) throws DatabaseException {
		
		logger.info("Fetching tree for resource, id => " + dirId);
		
		FsPathResource pathResource = treeRepository.getNodeWithChild(dirId, FsPathResource.class);
		Tree<FsPathResource> tree = null;
		try {
			tree = treeBuilder.buildTree(pathResource);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from FsDirectory node, id => " + dirId, e);
		}
		return tree;
		
	}
	
	/**
	 * Get tree for directory. Tree contains both directories and files so resulting tree type is of FsPathResource.
	 * 
	 * Only fetched child elements up to the max depth
	 * 
	 * @param dirId
	 * @param maxDepth
	 * @return
	 * @throws DatabaseException
	 */
	public Tree<FsPathResource> getTree(Long dirId, int maxDepth) throws DatabaseException {
		
		logger.info("Fetching tree for resource, id => " + dirId);
		
		FsPathResource pathResource = treeRepository.getNodeWithChild(dirId, FsPathResource.class, maxDepth);
		Tree<FsPathResource> tree = null;
		try {
			tree = treeBuilder.buildTree(pathResource);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from FsDirectory node, id => " + dirId, e);
		}
		return tree;
		
	}
	
	/**
	 * Fetches the directory node, plus all parents, all the way to the tree root node. Returns a tree of all the data.
	 * 
	 * e.d.  [tree root node] -> [parent of parent of parent...] -> [parent of parent] -> [parent of dir] - [dir (dirId)]
	 * 
	 * @param dirId
	 * @param clazz - e.g. FsPathResource, or FsDirectoryResource.
	 * @return
	 * @throws DatabaseException
	 */
	public Tree<FsPathResource> getParentTree(Long dirId) throws DatabaseException {
		
		logger.info("Fetching parent tree for resource, id => " + dirId);
		
		FsPathResource pathResource = treeRepository.getNodeWithParent(dirId, FsPathResource.class);
		Tree<FsPathResource> tree = null;
		try {
			tree = treeBuilder.buildParentTree(pathResource);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from FsDirectory node, id => " + dirId, e);
		}
		return tree;
		
	}
	
	/**
	 * Fetch by id
	 * 
	 * @param dirId
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectoryResource getDirectoryResourceById(Long dirId) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<FsDirectoryResource> query = criteriaBuilder.createQuery(FsDirectoryResource.class);
		Root<FsDirectoryResource> root = query.from(FsDirectoryResource.class);
		
		query.distinct(true);
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(FsDirectoryResource_.nodeId), dirId)
				);
		
		FsDirectoryResource result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
		return result;			
		
	}

	/**
	 * Fetch by file id
	 * 
	 * @param fileId 
	 * @return The parent directory for the file resource
	 * @throws DatabaseException
	 */
	public FsDirectoryResource getDirectoryResourceByFileId(Long fileId) throws DatabaseException {
		
		FsDirectoryResource parentResource = null;
		try {
			parentResource = (FsDirectoryResource) treeRepository.getParentNode(new FsFileMetaResource(fileId));
		} catch (DatabaseException e) {
			throw new DatabaseException("Error fetching parent directory resource for file id, => " + fileId, e);
		} catch (ClassCastException e){
			throw new DatabaseException("Parent path resource for node id, => " + fileId + 
					" does not appear to be a " + FsDirectoryResource.class.getName(), e);			
		}
		return parentResource;
		
	}
	
	/**
	 * Get a directory resource with child resources up to the max specified depth.
	 * 
	 * @param dirId - id of the directory resource
	 * @param maxDepth - also fetches children path resources up to this max depth. Use Integer.MAX_VALUE for no restriction.
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectoryResource getDirectoryResourceWithChildren(Long dirId, int maxDepth) throws DatabaseException {

		FsDirectoryResource dirResource = null;
		try {
			dirResource = (FsDirectoryResource) treeRepository.getNodeWithChild(new FsDirectoryResource(dirId), maxDepth);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch resources to depth " + maxDepth + " for path resource, id => " + dirId, e);
		} catch (ClassCastException e){
			throw new DatabaseException("Path resource for node id, => " + dirId + 
					" does not appear to be a " + FsDirectoryResource.class.getName(), e);
		}
		if(dirResource == null){
			throw new DatabaseException("Failed to fetch directory for id => " + dirId + ". Returned object was null.");
		}
		
		return dirResource;
		
	}
	
	/**
	 * Check if the directory contains child directory with matching name, up to the specified depth.
	 * 
	 * @param dirName - dir name to check for
	 * @param dirId - directory to check
	 * @param caseSensitive - true for case sensitive match, false otherwise.
	 *  @param maxDepth - fetches children path resources up to this max depth. Use Integer.MAX_VALUE for no restriction.
	 * @return A list of all matching child dirs, or null if none.
	 * @throws DatabaseException
	 */
	public List<FsDirectoryResource> haveExistingChildDirectory(String dirName, Long dirId, boolean caseSensitive, int maxDepth) throws DatabaseException {
		
		FsDirectoryResource dirResource = getDirectoryResourceWithChildren(dirId, maxDepth);
		
		List<FsDirectoryResource> childDirs = haveExistingChildDirectory(dirName, dirResource, caseSensitive, maxDepth);
			
		return childDirs;
	}
	
	/**
	 * Given a directory resource with child closure data, this method will search the child closure data for a child
	 * directory resource with the specified name, up to the max depth specified.
	 * 
	 * @param dirName - the name of the child directory resource to search for.
	 * @param dirResource - the parent directory to search
	 * @param caseSensitive - true for case sensitive match, false otherwise.
	 * @param maxDepth - the max depth to search for child resources
	 * @return - all matching child directories.
	 * @throws DatabaseException
	 */
	public List<FsDirectoryResource> haveExistingChildDirectory(String dirName, FsDirectoryResource dirResource, boolean caseSensitive, int maxDepth) throws DatabaseException {
		
		if(dirResource == null){
			throw new DatabaseException("Directory resource paramter is null");
		}
		if(!dirResource.hasChildClosure()){
			return null;
		}
		
		List<FsDirectoryResource> matchingChildDirs = new ArrayList<FsDirectoryResource>();
		
		for(DBClosure<FsPathResource> closure : dirResource.getChildClosure()){
			
			// TODO - check this depth code!
			if(closure.getDepth() > 0 && closure.getDepth() <= maxDepth){
				
				// check if there is an existing child directory resource with the same name
				FsPathResource resource = closure.getChildNode();
				if(resource.getPathType().equals(FsPathType.DIRECTORY)){
					if(caseSensitive){
						if(resource.getName().equals(dirName)){
							matchingChildDirs.add((FsDirectoryResource) resource);
						}
					}else{
						if(resource.getName().equalsIgnoreCase(dirName)){
							matchingChildDirs.add((FsDirectoryResource) resource);
						}
					}
				}				
				
			}
			
		}
		
		return matchingChildDirs.size() > 0 ? matchingChildDirs : null;
		
	}
	
	/**
	 * check at depth-1
	 * 
	 * @param dirName
	 * @param dirResource
	 * @param caseSensitive
	 * @return
	 * @throws DatabaseException
	 */
	public FsDirectoryResource haveExistingChildDirectory(String dirName, FsDirectoryResource dirResource, boolean caseSensitive) throws DatabaseException {
		
		List<FsDirectoryResource> childList = haveExistingChildDirectory(dirName, dirResource, caseSensitive, 1);
		
		if(childList != null && childList.size() > 1){
			return childList.get(0);
		}
		
		return null;
		
	}

}
