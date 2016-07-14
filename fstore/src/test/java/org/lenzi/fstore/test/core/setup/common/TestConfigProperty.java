package org.lenzi.fstore.test.core.setup.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Setup properties used for Postgres unit tests.
 * 
 * Uuse src/test/resources/my.application.test.properties
 * 
 * @author slenzi
 */
@Configuration
@PropertySource("classpath:my.application.test.properties")
public class TestConfigProperty {

	public TestConfigProperty() {
	
	}
	
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
