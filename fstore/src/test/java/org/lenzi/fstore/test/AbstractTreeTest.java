package org.lenzi.fstore.test;

import org.lenzi.fstore.service.FSTreeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	protected void logTestTitle(String title){
		logger.info("\n-------------------------------------------------------------------------------");
		logger.info(title);
		logger.info("-------------------------------------------------------------------------------\n");
	}
	
	public abstract FSTreeService getTreeSerive();

}
