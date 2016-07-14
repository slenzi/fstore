package org.lenzi.fstore.file2.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore_;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For removing a resource store.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsResourceStoreRemover extends AbstractRepository {


	private static final long serialVersionUID = -6256373987542346610L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceStoreRepository fsResourceStoreRepository;
	
	@Autowired
	private FsDirectoryResourceRemover dsDirectoryResourceRemover;

	public FsResourceStoreRemover() {
	
	}
	
	/**
	 * Remove store, deleting all directories and files in the store.
	 * 
	 * @param storeId - ID of the resurce store to remove
	 * @throws DatabaseException
	 */
	public void removeStore(Long storeId) throws DatabaseException {
		
		FsResourceStore storeToDelete = fsResourceStoreRepository.getStoreByStoreId(storeId);
		
		// remove store's root directory, plus all child directories and files. delete everything!
		dsDirectoryResourceRemover.removeDirectory(storeToDelete.getRootDirectoryResource().getDirId());
		
		// delete store entry
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<FsResourceStore> storeDelete = cb.createCriteriaDelete(FsResourceStore.class);
		Root<FsResourceStore> storeRoot = storeDelete.from(FsResourceStore.class);
		storeDelete.where(cb.equal(storeRoot.get(FsResourceStore_.storeId), storeId));
		executeUpdate(storeDelete);
		
	}

}
