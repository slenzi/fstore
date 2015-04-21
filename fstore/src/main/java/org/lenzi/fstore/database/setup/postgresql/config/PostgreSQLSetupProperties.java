package org.lenzi.fstore.database.setup.postgresql.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Properties used during the PostgreSQL database setup process.
 *
 * @see org.lenzi.fstore.database.setup.postgresql.config.PostgreSQLSetupPropertyConfig
 * 
 * @author slenzi
 */
@Component(value="SetupProperties")
@Scope("singleton")
public class PostgreSQLSetupProperties {

	@Autowired
	Environment env;
	
	/**
	 * Get a specific property
	 */
	public String getProperty(String name) {
		return env.getProperty(name);
	}

}
