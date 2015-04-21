package org.lenzi.fstore.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

/**
 * Spring project configuration setup
 * 
 * Scan all packages from base package org.lenzi.fstore, but skip the following.
 * - skip other classes marked with @Configuration. Our AppConfig class specifically includes all the ones we need.
 * - skip classes marked with @Controller. The WebMvcConfig class sets up Spring MVC and all our controllers.
 * - skip all classes under org.lenzi.fstore.database.* These classes are only used when setting up the database,
 *   and they have their own @Configurtion classes.
 * 
 * @see org.lenzi.fstore.config.WebMvcConfig for Spring MVC configuration.
 * 
 * @author slenzi
 */
@Configuration
@ComponentScan(
	basePackages = "org.lenzi.fstore",
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class),
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.lenzi.fstore.database.")
	}
)
@Import({
	PropertyConfig.class, DataSourceConfig.class, PersistenceConfig.class, UploadConfig.class, CxfConfig.class, RepositoryConfig.class
})
public class AppConfig {

	// PropertyConfig - Setup property access for our properties file under src/main/resource
	
	// DataSourceConfig - Setup DataSource objects
	
	// PersistenceConfig - Setup entity manager with transaction management for our database.
	
	// CxfConfig - Setup our JAX-RS service using Apache CXF
	
	// UploadConfig - Initialize Spring's Multipart Resolver bean for processing file uploads.
	
	// RepositoryConfig - Setup repositories depending on which spring profiles are active
	
}
