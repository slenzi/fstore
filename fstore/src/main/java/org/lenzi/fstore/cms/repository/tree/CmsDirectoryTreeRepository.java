/**
 * 
 */
package org.lenzi.fstore.cms.repository.tree;

import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.AbstractTreeRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository("cmsDirectoryTree")
@Transactional(propagation=Propagation.REQUIRED)
public class CmsDirectoryTreeRepository extends AbstractTreeRepository<CmsDirectory> {

	@InjectLogger
	private Logger logger;	
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -5727364294891828637L;

	@Override
	public CmsDirectory postAdd(CmsDirectory node) throws DatabaseException {
		
		logger.info("Post add node => " + node);
		
		return null;
	}

	@Override
	public CmsDirectory postMove(CmsDirectory node) throws DatabaseException {

		logger.info("Post move node => " + node);
		
		return null;
	}

	@Override
	public void postRemove(CmsDirectory node) throws DatabaseException {

		logger.info("Post remove node => " + node);
		
	}

	@Override
	public CmsDirectory postCopy(CmsDirectory originalNode, CmsDirectory newCopyNode) throws DatabaseException {

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
