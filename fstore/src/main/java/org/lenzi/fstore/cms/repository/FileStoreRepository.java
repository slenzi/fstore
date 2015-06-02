/**
 * 
 */
package org.lenzi.fstore.cms.repository;

import org.slf4j.Logger;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.stereotype.InjectLogger;


/**
 * Main CMS repository for dealing with directory and file operations.
 * 
 * Each method will use the parent transaction if available, otherwise a new one will be created.
 * 
 * @deprecated - old
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FileStoreRepository extends AbstractRepository {

	@InjectLogger
	private Logger logger;	
	
	private static final long serialVersionUID = 8439120459143189611L;

	
	public FileStoreRepository() {
	
	}
	


}
