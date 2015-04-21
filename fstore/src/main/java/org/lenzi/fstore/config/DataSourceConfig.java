package org.lenzi.fstore.config;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.lenzi.filestore.util.StringUtil;
import org.lenzi.fstore.properties.ManagedProperties;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiTemplate;

/**
 * Configure DataSource objects
 * 
 * @author slenzi
 */
@Configuration
public class DataSourceConfig {

    @Autowired
    private ManagedProperties appProps;
    
	@InjectLogger
	private Logger logger;
	
	/**
	 * Fetch data source from JNDI.
	 * 
	 * @return
	 */
	@Bean
	@Profile("oracle")
	public DataSource getJndiDataSource() {
		
		DataSource dataSource = null;
        JndiTemplate jndi = new JndiTemplate();
        
        String jndiDataSourceName = appProps.getProperty("database.jndi.pool.main");
        
        try {
            dataSource = (DataSource) jndi.lookup(jndiDataSourceName);
        } catch (NamingException e) {
            logger.error("NamingException for " + jndiDataSourceName, e);
        }
        return dataSource;
	}
	
	/**
	 * Get DataSource for database.
	 * 
	 * See src/main/resources/my.application.properties for database connection details.
	 * 
	 * @return
	 */
	@Bean
	@Profile("postgresql")
	public DataSource getDriverManagerDataSource(){
		
		logger.info("Initializing driver manager data source:");
		
		logger.info("Database driver = " + appProps.getDatabaseDriver());
		logger.info("Database url = " + appProps.getDatabaseUrl());
		logger.info("Database user = " + appProps.getDatabaseUser());
		logger.info("Database password = *******");
		
		if(StringUtil.isNullEmpty(appProps.getDatabaseDriver()) || StringUtil.isNullEmpty(appProps.getDatabaseUrl()) ||
				StringUtil.isNullEmpty(appProps.getDatabaseUser()) || StringUtil.isNullEmpty(appProps.getDatabasePassword())){
			
			logger.error("Missing required values for data source. Check driver name, connection url, username, and/or password");
			
			return null;
		}
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(appProps.getDatabaseDriver());
		dataSource.setUrl(appProps.getDatabaseUrl());
        dataSource.setUsername(appProps.getDatabaseUser());
        dataSource.setPassword(appProps.getDatabasePassword());
        
        return dataSource;
	}

}
