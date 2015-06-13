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
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource_;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
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
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsFileResourceRemover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1756855144789479319L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
	@Autowired
	private FsFileResourceRepository fsFileResourceRepository;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;

	/**
	 * 
	 */
	public FsFileResourceRemover() {
		
	}
	
	/**
	 * Remove a file
	 * 
	 * @param fileId - if of the file to remove
	 * @throws DatabaseException
	 */
	public void removeFile(Long fileId) throws DatabaseException {
		
		FsFileMetaResource fileEntry = fsFileResourceRepository.getFileEntry(fileId, FsFileResourceFetch.FILE_META);
		FsDirectoryResource dir = fsDirectoryResourceRepository.getDirectoryResourceByFileId(fileId);
		FsResourceStore store = fsResourceStoreRepository.getStoreByDirectoryId(dir.getDirId());
		
		remove(store, dir, fileEntry);
		
	}	
	
	/**
	 * Remove file
	 * 
	 * @param store
	 * @param dir
	 * @param fileEntry
	 * @throws DatabaseException
	 */
	public void remove(FsResourceStore store, FsDirectoryResource dir, FsFileMetaResource fileEntry) throws DatabaseException {
		
		Path absoluteFilePath = fsResourceHelper.getAbsoluteFilePath(store, dir, fileEntry);		
		
		logger.info("removing file id => " + fileEntry.getFileId() + ", name => " + fileEntry.getName() + 
				", path => " + absoluteFilePath);
		
		try {
			
			// remove FsFileResource
			CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
			CriteriaDelete<FsFileResource> cmsFileDelete = cb.createCriteriaDelete(FsFileResource.class);
			Root<FsFileResource> cmsFileRoot = cmsFileDelete.from(FsFileResource.class);
			cmsFileDelete.where(cb.equal(cmsFileRoot.get(FsFileResource_.nodeId), fileEntry.getFileId()));
			executeUpdate(cmsFileDelete);			
			
			// remove FsFileMetaResource (tree node)
			treeRepository.removeNode(fileEntry);
			
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to remove file from database for file id => " + fileEntry.getFileId(), e);
		}
		
		try {
			FileUtil.deletePath(absoluteFilePath);
		} catch (IOException e) {
			throw new DatabaseException("Failed to remove file from local file system => " + absoluteFilePath.toString(), e);
		}		
		
	}	

}
