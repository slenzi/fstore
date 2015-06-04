package org.lenzi.fstore.main.config;

import org.lenzi.fstore.core.repository.tree.query.TreeQueryOracleRepository;
import org.lenzi.fstore.core.repository.tree.query.TreeQueryPostgresqlRepository;
import org.lenzi.fstore.core.repository.tree.query.TreeQueryRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Setup which closure repository to use depending on which spring profile
 * is active (oracle or postgresql)
 * 
 * @author slenzi
 */
@Configuration
public class RepositoryConfig {
	
	@InjectLogger
	private Logger logger;
	
	/**
	 * Repository tree related query functions specific to Oracle.
	 * 
	 * @return
	 */
	@Bean
	@Profile("oracle")
	public TreeQueryRepository getOracleTreeQueryRepository(){
		
		logger.info("Creating Oracle Tree Query Repository");
		
		return new TreeQueryOracleRepository();
		
	}
	
	/**
	 * Repository tree related query functions specific to PostgreSQL.
	 * 
	 * @return
	 */
	@Bean
	@Profile("postgresql")
	public TreeQueryRepository getPostgresqlTreeQueryRepository(){
		
		logger.info("Creating PostgreSQL Tree Query Repository");
		
		return new TreeQueryPostgresqlRepository();
		
	}	

}
