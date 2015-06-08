/**
 * 
 */
package org.lenzi.fstore.file.repository.tree;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.AbstractTreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository("FsDirectoryTree")
@Transactional(propagation=Propagation.REQUIRED)
public class FsDirectoryTreeRepository extends AbstractTreeRepository<FsDirectory> {

	@InjectLogger
	private Logger logger;	
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -5727364294891828637L;

	@Override
	public FsDirectory postAdd(FsDirectory node) throws DatabaseException {
		
		logger.info("Post add node => " + node);
		
		return null;
	}

	@Override
	public FsDirectory postMove(FsDirectory node) throws DatabaseException {

		logger.info("Post move node => " + node);
		
		return null;
	}

	@Override
	public void postRemove(FsDirectory node) throws DatabaseException {

		logger.info("Post remove node => " + node);
		
	}

	@Override
	public FsDirectory postCopy(FsDirectory originalNode, FsDirectory newCopyNode) throws DatabaseException {

		logger.info("Post copy node");
		logger.info("Original Node => " + originalNode);
		logger.info("New Copy Node => " + newCopyNode);
		
		return null;		
		
	}

	@Override
	public String getRepositoryName() {
		return getClass().getName();
	}

}
