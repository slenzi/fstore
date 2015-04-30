package org.lenzi.fstore.repository;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.impl.FSTestNode;
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
	public DBNode addCustomNode(FSTestNode node) throws DatabaseException {
		
		logger.info("Add custom node => " + node.getClass().getCanonicalName());
		logger.info("Is root node => " + node.isRootNode());
		
		return null;
		
	}

	@Override
	public DBNode moveCustomNode(FSTestNode node) throws DatabaseException {
		
		logger.info("Move custom node => " + node.getClass().getCanonicalName());
		logger.info("Is root node => " + node.isRootNode());
		
		return null;
		
	}

	@Override
	public void removeCustomNode(FSTestNode node) throws DatabaseException {
		
		// remove the node and anything under it
		
		logger.info("Remove custom node => " + node.getClass().getCanonicalName());
		logger.info("Is root node => " + node.isRootNode());
		
		remove(node);
		
	}

	@Override
	public DBNode copyCustomNode(FSTestNode originalNode, FSTestNode newCopyNode) throws DatabaseException {
		
		logger.info("Copy node.");
		
		return null;
		
	}
	
}
