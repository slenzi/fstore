package org.lenzi.fstore.database.setup.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Properties used during the database setup process.
 *
 * @see org.lenzi.fstore.database.setup.config.SetupPropertyConfig
 * 
 * @author slenzi
 */
@Component(value="SetupProperties")
@Scope("singleton")
public class SetupProperties {

	@Autowired
	Environment env;
	
	/**
	 * Get a specific property
	 */
	public String getProperty(String name) {
		return env.getProperty(name);
	}

}
