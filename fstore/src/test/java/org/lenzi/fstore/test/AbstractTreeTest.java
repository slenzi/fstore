package org.lenzi.fstore.test;

import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.service.TreeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	protected void logTestTitle(String title){
		logger.info("\n-------------------------------------------------------------------------------");
		logger.info(title);
		logger.info("-------------------------------------------------------------------------------\n");
	}
	
	public abstract TreeService getTreeSerive();
	
	public abstract ClosureLogger getClosureLogger();

}
