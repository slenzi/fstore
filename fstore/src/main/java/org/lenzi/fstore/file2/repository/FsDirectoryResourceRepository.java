/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.model.DBClosure;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
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
			dirResource = (FsDirectoryResource) treeRepository.getNodeWithChild(new FsDirectoryResource(dirId), 1);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch depth-1 resources for path resource, id => " + dirId, e);
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
		
		List<FsDirectoryResource> matchingChildDirs = new ArrayList<FsDirectoryResource>();
		
		for(DBClosure<FsPathResource> closure : dirResource.getChildClosure()){
			
			// TODO - check this depth code!
			if(closure.getDepth() > 0 && closure.getDepth() <= maxDepth){
				
				// check if there is an existing child directory resource with the same name
				FsPathResource resource = closure.getChildNode();
				if(resource.getPathType().equals(FsPathType.DIRECTORY)){
					if(caseSensitive){
						matchingChildDirs.add((FsDirectoryResource) resource);
					}else{
						matchingChildDirs.add((FsDirectoryResource) resource);
					}
				}				
				
			}
			
		}
		
		return matchingChildDirs.size() > 0 ? matchingChildDirs : null;
		
	}
	
	/**
	 * Check if the directory contains child directory at depth-1 with the specified name (does not check entire sub tree)
	 * 
	 * @param dirName - dir name to check for
	 * @param dirId - directory to check
	 * @param caseSensitive - true for case sensitive match, false otherwise.
	 * @return The FsDirectoryResource for the matching child directory, if one exists
	 * @throws DatabaseException
	 */
	public FsDirectoryResource haveExistingChildDirectory(String dirName, Long dirId, boolean caseSensitive) throws DatabaseException {
		
		FsDirectoryResource dirResource = null;
		try {
			dirResource = (FsDirectoryResource) treeRepository.getNodeWithChild(new FsDirectoryResource(dirId), 1);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to fetch depth-1 resources for path resource, id => " + dirId, e);
		} catch (ClassCastException e){
			throw new DatabaseException("Path resource for node id, => " + dirId + 
					" does not appear to be a " + FsDirectoryResource.class.getName(), e);
		}
		if(dirResource == null){
			throw new DatabaseException("Failed to fetch directory for id => " + dirId + ". Returned object was null.");
		}
		// check each child node on each child closure entry
		Optional<DBClosure<FsPathResource>> matchingClosure = dirResource.getChildClosure().stream()
			.filter(closure -> {
				
				// TODO - check this code!
				
				// ignore depth-0 closure entries (a resource is a child of itself at depth-0)
				if(closure.getDepth() > 0){
					
					// check if there is an existing child directory resource with the same name
					FsPathResource resource = closure.getChildNode();
					if(resource.getPathType().equals(FsPathType.DIRECTORY)){
						if(caseSensitive){
							return resource.getName().equals(dirName);
						}else{
							return resource.getName().equalsIgnoreCase(dirName);
						}
					}		
					
				}
				return false;
				
			})
			.findFirst();
		
		FsDirectoryResource existingChildDirectoryResource = null;
		
		if(matchingClosure.isPresent()){
			
			existingChildDirectoryResource = (FsDirectoryResource) matchingClosure.get().getChildNode();
			
			logger.info("Need to replace/merge existing child directory resource, id => " + existingChildDirectoryResource.getNodeId() + 
					", name => " + existingChildDirectoryResource.getName());
			
		}else{
			
			logger.info("No existing child directory resource with same name. No need to worry about replacing/merging!");
			
		}		
		
		return existingChildDirectoryResource;
	}

}
