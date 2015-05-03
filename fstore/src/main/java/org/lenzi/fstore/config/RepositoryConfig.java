package org.lenzi.fstore.config;

import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.repository.tree.OracleTestTreeRepository;
import org.lenzi.fstore.example.repository.tree.PostgreSQLTestTreeRepository;
import org.lenzi.fstore.repository.tree.TreeRepository;
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
	 * Create instance of OracleTestTreeRepository. Will only be created when
	 * the "oracle" Spring Profile is active.
	 * 
	 * @return Instance of an OracleTestTreeRepository.
	 */
	@Bean
	@Profile("oracle")
	public TreeRepository<FSTestNode> getOracleTreeRepository(){
		
		logger.info("Creating Oracle Tree Repository");
		
		return new OracleTestTreeRepository();
	}
	
	/**
	 * Create instance of PostgreSQLTestTreeRepository. Will only be created when
	 * the "postgresql" Spring Profile is active.
	 * 
	 * @return Instance of an PostgreSQLTestTreeRepository.
	 */
	@Bean
	@Profile("postgresql")
	public TreeRepository<FSTestNode> getPostgresTreeRepository(){
		
		logger.info("Creating PostgreSQL Tree Repository");
		
		return new PostgreSQLTestTreeRepository();
		
	}

}
