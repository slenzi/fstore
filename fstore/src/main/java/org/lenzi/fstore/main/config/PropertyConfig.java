package org.lenzi.fstore.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Load properties from our src/main/resources/my.application.properties file.
 * 
 * @author slenzi
 */
@Configuration
@PropertySource("classpath:my.application.properties")
public class PropertyConfig {

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
