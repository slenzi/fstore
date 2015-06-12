/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.service.TreeBuilder;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource_;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
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

}
