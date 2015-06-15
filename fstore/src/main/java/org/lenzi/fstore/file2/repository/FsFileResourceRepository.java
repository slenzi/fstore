/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.util.ArrayList;
import java.util.List;
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
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
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
	 * check at specified depth
	 * 
	 * @param fileName
	 * @param dirId
	 * @param caseSensitive
	 * @param maxDepth
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsFileMetaResource> haveExistingFile(String fileName, Long dirId, boolean caseSensitive, int maxDepth) throws DatabaseException {
		
		FsDirectoryResource dirResource = fsDirectoryResourceRepository.getDirectoryResourceWithChildren(dirId, maxDepth);
		
		List<FsFileMetaResource> childFiles = haveExistingFile(fileName, dirResource, caseSensitive, maxDepth);
			
		return childFiles;
	}
	
	/**
	 * check at specified depth
	 * 
	 * @param dirName
	 * @param dirResource
	 * @param caseSensitive
	 * @param maxDepth
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsFileMetaResource> haveExistingFile(String dirName, FsDirectoryResource dirResource, boolean caseSensitive, int maxDepth) throws DatabaseException {
		
		if(dirResource == null){
			throw new DatabaseException("Directory resource paramter is null");
		}
		if(!dirResource.hasChildClosure()){
			return null;
		}
		
		List<FsFileMetaResource> matchingChildFiles = new ArrayList<FsFileMetaResource>();
		
		for(DBClosure<FsPathResource> closure : dirResource.getChildClosure()){
			
			// TODO - check this depth code!
			if(closure.getDepth() > 0 && closure.getDepth() <= maxDepth){
				
				// check if there is an existing child directory resource with the same name
				FsPathResource resource = closure.getChildNode();
				if(resource.getPathType().equals(FsPathType.FILE)){
					if(caseSensitive){
						if(resource.getName().equals(dirName)){
							matchingChildFiles.add((FsFileMetaResource) resource);
						}
					}else{
						if(resource.getName().equalsIgnoreCase(dirName)){
							matchingChildFiles.add((FsFileMetaResource) resource);
						}
					}
				}				
				
			}
			
		}
		
		return matchingChildFiles.size() > 0 ? matchingChildFiles : null;
		
	}
	
	/**
	 * check at depth 1
	 * 
	 * @param fileName
	 * @param dirResource
	 * @param caseSensitive
	 * @return
	 * @throws DatabaseException
	 */
	public FsFileMetaResource haveExistingFile(String fileName, FsDirectoryResource dirResource, boolean caseSensitive) throws DatabaseException {
		
		List<FsFileMetaResource> childFiles = haveExistingFile(fileName, dirResource, caseSensitive, 1);
		
		if(childFiles != null && childFiles.size() > 0){
			return childFiles.get(0);
		}
			
		return null;
	}


}
