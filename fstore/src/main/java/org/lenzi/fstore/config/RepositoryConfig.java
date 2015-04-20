package org.lenzi.fstore.config;

import org.lenzi.fstore.repository.ClosureRepository;
import org.lenzi.fstore.repository.OracleClosureRepository;
import org.lenzi.fstore.repository.PostgresClosureRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
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
	 * Create instance of OracleClosureRepository. Will only be created when
	 * the "oracle" Spring Profile is active.
	 * 
	 * @return Instance of an OracleClosureRepository.
	 */
	@Bean
	@Profile("oracle")
	public ClosureRepository getOracleClosureRepository(){
		
		logger.info("Creating Oracle Closure Repository");
		
		return new OracleClosureRepository();
		
	}
	
	/**
	 * Create instance of PostgresClosureRepository. Will only be created when
	 * the "postgresql" Spring Profile is active.
	 * 
	 * @return Instance of an PostgresClosureRepository.
	 */
	@Bean
	@Profile("postgresql")
	public ClosureRepository getPostgresClosureRepository(){
		
		logger.info("Creating PostgreSQL Closure Repository");
		
		return new PostgresClosureRepository();
		
	}

}
