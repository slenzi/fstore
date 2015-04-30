package org.lenzi.fstore.repository;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.impl.FSTestNode;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides PostgresSQL specific code for working with our sample FSTestNode.
 * 
 * @author sal
 */
@Transactional(propagation=Propagation.REQUIRED)
public class PostgreSQLTestTreeRepository extends AbstractPostgreSQLTreeRepository<FSTestNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3961163035798204009L;

	@InjectLogger
	private Logger logger;	
	
	
	@Override
	public DBNode postAdd(FSTestNode node) throws DatabaseException {
		
		logger.info("Post add node => " + node.getClass().getCanonicalName() + ", id => " + node.getNodeId() + ", is root => " + node.isRootNode());
		
		return null;
		
	}

	@Override
	public DBNode postMove(FSTestNode node) throws DatabaseException {
		
		logger.info("Post move node => " + node.getClass().getCanonicalName() + ", id => " + node.getNodeId() + ", is root => " + node.isRootNode());
		
		return null;
		
	}

	@Override
	public void postRemove(FSTestNode node) throws DatabaseException {
		
		logger.info("Post remove node => " + node.getClass().getCanonicalName() + ", id => " + node.getNodeId() + ", is root => " + node.isRootNode());
		
	}

	@Override
	public DBNode postCopy(FSTestNode originalNode, FSTestNode newCopyNode) throws DatabaseException {
		
		logger.info("Post copy node");
		logger.info("Original Node => " + originalNode.getClass().getCanonicalName() + ", id => " + originalNode.getNodeId() + ", is root => " + originalNode.isRootNode());
		logger.info("New Copy Node => " + newCopyNode.getClass().getCanonicalName() + ", id => " + newCopyNode.getNodeId() + ", is root => " + newCopyNode.isRootNode());
		
		return null;
		
	}

}
