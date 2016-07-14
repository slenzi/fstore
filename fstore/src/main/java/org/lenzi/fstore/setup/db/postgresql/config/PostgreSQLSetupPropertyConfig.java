package org.lenzi.fstore.setup.db.postgresql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Load properties from our src/main/resources/setup.postgresql.properties
 * 
 * These properties are used when setting up the PostgreSQL database.
 * 
 * @author slenzi
 */
@Configuration
@PropertySource("classpath:setup.postgresql.properties")
public class PostgreSQLSetupPropertyConfig {

	/**
	 * To resolve ${} in @Value annotations.
	 * 
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
