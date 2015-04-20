package org.lenzi.fstore.test.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	protected void logTestTitle(String title){
		logger.info("\n-------------------------------------------------------------------------------");
		logger.info(title);
		logger.info("-------------------------------------------------------------------------------\n");
	}

}
