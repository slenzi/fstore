package org.lenzi.fstore.example.repository.tree;

import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.AbstractOracleTreeRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides Oracle specific code for working with our sample FSTestNode.
 * 
 * @author sal
 */
@Transactional(propagation=Propagation.REQUIRED)
public class OracleTestTreeRepository extends AbstractOracleTreeRepository<FSTestNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	
}
