/**
 * 
 */
package org.lenzi.fstore.file2.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
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
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore_;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For fetching file resource data
 * 
 * @author sal
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
		
		CriteriaQuery<FsFileMetaResource> query = criteriaBuilder.createQuery(FsFileMetaResource.class);
		Root<FsFileMetaResource> root = query.from(FsFileMetaResource.class);
		
		switch(fetch){
		
			// just FsFileMetaResource
			case FILE_META:
				break;
			
			// also fetch FsFileResource
			case FILE_META_WITH_DATA:
				root.fetch(FsFileMetaResource_.fileResource, JoinType.LEFT);
				break;
			
			// default to just meta data, no join
			default:
				break;
	
		}		
		
		query.select(root);
		query.where(
				criteriaBuilder.equal(root.get(FsFileMetaResource_.nodeId), fileId)
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
	
	}
	
	/**
	 * Fetch a file by it's full path.
	 * 
	 * @param path - Combination of the resource store's root directory name + the file resource relative path value.
	 * 
	 * e.g.
	 * Resource store path = /test/stores/testStore
	 * File resource full path = /test/stores/testStore/sampleDir1/lolcat.jpg
	 * 
	 * Using above example,
	 * Resource store root directory name = testStore
	 * File resource relative path = /sampleDir1/lolcat.jpg
	 * 
	 * So, the path value to pass to this method would be, testStore/sampleDir1/lolcat.jpg
	 * 
	 * @throws DatabaseException
	 */
	public FsFileMetaResource getFileEntryByPath(String path, FsFileResourceFetch fetch) throws DatabaseException {

		/* hql
		 
		select f
		from
			FsFileMetaResource as f
		left join fetch f.fileResource r
		where
			LOWER (
					CONCAT (f.resourceStore.storePath, f.relativePath)
				) LIKE LOWER (
					CONCAT ('%', 'sample_resource_store_add_bulk_file\Sample directory 2\let me eat you kitten.jpg')
				)
		*/
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<FsFileMetaResource> query = cb.createQuery(FsFileMetaResource.class);
		Root<FsFileMetaResource> root = query.from(FsFileMetaResource.class);
		
		switch(fetch){
		
			// just FsFileMetaResource
			case FILE_META:
				break;
			
			// also fetch FsFileResource
			case FILE_META_WITH_DATA:
				root.fetch(FsFileMetaResource_.fileResource, JoinType.LEFT);
				break;
			
			// default to just meta data, no join
			default:
				break;

		}		
		
		Path<String> relativePath = root.<String>get(FsFileMetaResource_.relativePath);
		Path<FsResourceStore> store = root.<FsResourceStore>get(FsFileMetaResource_.resourceStore);
		Path<String> storePath = store.<String>get(FsResourceStore_.storePath);
		
		String pathMatch = ("%" + path).toLowerCase();
		
		query.select(root);
		query.where(
				cb.like( cb.lower( cb.concat(storePath, relativePath) ), pathMatch )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
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
	 * @param fileName
	 * @param dirResource
	 * @param caseSensitive
	 * @param maxDepth
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsFileMetaResource> haveExistingFile(String fileName, FsDirectoryResource dirResource, boolean caseSensitive, int maxDepth) throws DatabaseException {
		
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
						if(resource.getName().equals(fileName)){
							matchingChildFiles.add((FsFileMetaResource) resource);
						}
					}else{
						if(resource.getName().equalsIgnoreCase(fileName)){
							matchingChildFiles.add((FsFileMetaResource) resource);
						}
					}
				}				
				
			}
			
		}
		
		return matchingChildFiles.size() > 0 ? matchingChildFiles : null;
		
	}
	
	/**
	 * Check at depth 1
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
