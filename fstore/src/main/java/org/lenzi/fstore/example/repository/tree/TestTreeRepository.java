package org.lenzi.fstore.example.repository.tree;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.AbstractTreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Example tree repository for working with FSTestNode objects.
 * 
 * Rollbacks by default only happen for unchecked exceptions. In the transaction annotation
 * we add rollbackFor=Throwable.class so rollbacks will happen for checked exceptions as
 * well, e.g., our DatabaseException class.
 * 
 * @author sal
 */
@Repository("testNodeTree")
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class TestTreeRepository extends AbstractTreeRepository<FSTestNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4555086719725585870L;

	@InjectLogger
	private Logger logger;	
	
	@Override
	public FSTestNode postAdd(FSTestNode node) throws DatabaseException {

		logger.info("Post add node => " + node);
		
		return null;		
		
	}

	@Override
	public FSTestNode postMove(FSTestNode node) throws DatabaseException {

		logger.info("Post move node => " + node);
		
		return null;		
		
	}

	@Override
	public void postRemove(FSTestNode node) throws DatabaseException {
		
		logger.info("Post remove node => " + node);
		
	}

	@Override
	public FSTestNode postCopy(FSTestNode originalNode, FSTestNode newCopyNode) throws DatabaseException {

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
