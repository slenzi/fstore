/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.model.DBClosure;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource_;
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
public class FsFileResourceRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5096177195394047372L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	public enum FsFileResourceFetch {
		
		// just meta data for each file
		FILE_META,
		
		// meta data and byte data
		FILE_META_WITH_DATA,
		
	}

	/**
	 * 
	 */
	public FsFileResourceRepository() {
		
	}
	
	/**
	 * Fetch file entry
	 * 
	 * @param fileId
	 * @param fetch
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileMetaResource getFileEntry(Long fileId, FsFileResourceFetch fetch) throws DatabaseException {
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<FsFileMetaResource> nodeSelect = criteriaBuilder.createQuery(FsFileMetaResource.class);
		Root<FsFileMetaResource> nodeSelectRoot = nodeSelect.from(FsFileMetaResource.class);
		
		switch(fetch){
		
			// just FsFileMetaResource
			case FILE_META:
				break;
			
			// also fetch FsFileResource
			case FILE_META_WITH_DATA:
				nodeSelectRoot.fetch(FsFileMetaResource_.fileResource, JoinType.LEFT);
				break;
			
			// default to just meta data, no join
			default:
				break;
	
		}		
		
		nodeSelect.select(nodeSelectRoot);
		nodeSelect.where(
				criteriaBuilder.equal(nodeSelectRoot.get(FsFileMetaResource_.nodeId), fileId)
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(nodeSelect));
	
	}
	
	/**
	 * Check if the directory already contains a file resource with the same name
	 * 
	 * @param fileName - the name to check for
	 * @param dirId - the directory to check
	 * @param caseSensitive - true for case sensitive match, false otherwise.
	 * @return The FsFileMetaResource for the matching file if one exists.
	 * @throws DatabaseException
	 */
	public FsFileMetaResource haveExistingFile(String fileName, Long dirId, boolean caseSensitive) throws DatabaseException {
		
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
				// check if there is an existing child file resource with the same name
				FsPathResource resource = closure.getChildNode();
				if(resource.getPathType().equals(FsPathType.FILE)){
					if(caseSensitive){
						return resource.getName().equals(fileName);
					}else{
						return resource.getName().equalsIgnoreCase(fileName);
					}
				}
				return false;	
			})
			.findFirst();
		
		FsFileMetaResource existingFileResource = null;
		
		if(matchingClosure.isPresent()){
			
			existingFileResource = (FsFileMetaResource) matchingClosure.get().getChildNode();
			
			logger.info("Need to replace existing file resource, id => " + existingFileResource.getNodeId() + 
					", name => " + existingFileResource.getName());
			
		}else{
			
			logger.info("No existing resource with same name. No need to worry about replacing!");
			
		}		
		
		return existingFileResource;
	}

}
