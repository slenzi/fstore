package org.lenzi.fstore.file2.repository;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For updating existing text files.
 * 
 * @deprecated - not sure about this class....
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsTextFileResourceUpdater extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -181534624395410686L;
	
	@InjectLogger
	private Logger logger;

	public FsTextFileResourceUpdater() {
		
	}

}
