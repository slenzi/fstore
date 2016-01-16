package org.lenzi.fstore.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public abstract class AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private Environment env;	
	
	protected void logTestTitle(String title){
		logger.info("\n-------------------------------------------------------------------------------");
		logger.info(title);
		logger.info("-------------------------------------------------------------------------------\n");
	}
	
	/**
	 * Fetch property
	 * 
	 * @param name - name of property
	 * @return
	 */
	public String getProperty(String name) {
		return env != null ? env.getProperty(name) : "Failed to Autowire Environment";
	}	

}
