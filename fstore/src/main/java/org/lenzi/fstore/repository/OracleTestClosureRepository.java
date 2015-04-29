package org.lenzi.fstore.repository;

import org.lenzi.fstore.repository.exception.DatabaseException;
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
public class OracleTestClosureRepository extends AbstractOracleClosureRepository<FSTestNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@InjectLogger
	private Logger logger;	
	
	@Override
	public void removeCustomNode(FSTestNode node) throws DatabaseException {
		
		logger.info("Removing user custom node => " + node.getClass().getCanonicalName());
		
	}

}
