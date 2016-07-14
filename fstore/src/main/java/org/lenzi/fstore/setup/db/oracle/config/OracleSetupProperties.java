package org.lenzi.fstore.setup.db.oracle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Properties used during the Oracle database setup process.
 *
 * @see org.lenzi.fstore.setup.db.oracle.config.OracleSetupPropertyConfig
 * 
 * @author slenzi
 */
@Component(value="oracleSetupProperties")
@Scope("singleton")
public class OracleSetupProperties {

	@Autowired
	Environment env;
	
	/**
	 * Get a specific property
	 */
	public String getProperty(String name) {
		return env.getProperty(name);
	}

}
