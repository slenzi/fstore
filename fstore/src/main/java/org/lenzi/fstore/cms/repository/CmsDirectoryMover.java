package org.lenzi.fstore.cms.repository;

import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For moving cms directories.
 * 
 * @author sal
 */
@Service
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class CmsDirectoryMover extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1132760623198092221L;

	public CmsDirectoryMover() {
		
	}
	
	// TODO - implement
	public void moveDirectory(Long dirId, Long newParentDirId, boolean replaceExisting) throws DatabaseException {
		
		// make sure not a root directory
		
		// if directory already exists, merge files
		
	}	

}
