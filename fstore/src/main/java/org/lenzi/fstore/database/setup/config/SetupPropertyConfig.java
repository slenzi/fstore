package org.lenzi.fstore.database.setup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Load properties from our src/main/resources/setup.oracle.properties
 * 
 * These properties are used when setting up the Oracle database.
 * 
 * @author slenzi
 */
@Configuration
@PropertySource("classpath:setup.oracle.properties")
public class SetupPropertyConfig {

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
