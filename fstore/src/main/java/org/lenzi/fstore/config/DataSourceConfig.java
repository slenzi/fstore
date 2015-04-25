package org.lenzi.fstore.config;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.lenzi.fstore.properties.ManagedProperties;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	 * Primary data source for the application. Can add more later if needed....
	 * 
	 * First, look for JNDI name in properties. If that's available then it grabs the data source from JNDI.
	 * 
	 * Second, if no JNDI data source then manually create a driver manager data source using values from property file.
	 * 
	 * @return
	 */
	@Bean(name="primaryDataSource")
	public DataSource getPrimaryDataSource(){
		
		DataSource dataSource = null;
		
		String jndiDataSourceName = appProps.getProperty("database.jndi.pool.main");
		
		if(!StringUtil.isNullEmpty(jndiDataSourceName)){
			
			logger.info("Getting data source from JNDI, name => " + jndiDataSourceName);
			
			JndiTemplate jndi = new JndiTemplate();
	        try {
	            dataSource = (DataSource) jndi.lookup(jndiDataSourceName);
	        } catch (NamingException e) {
	            logger.error("NamingException for " + jndiDataSourceName, e);
	        }
	        
		}else{
			
			dataSource = getDriverManagerDataSource();
			
		}
		
		return dataSource;
		
	}
	
	
	/**
	 * Fetch data source from JNDI.
	 * 
	 * @return
	 */
	private DataSource getJndiDataSource(String jndiName) {
		
		DataSource dataSource = null;
        JndiTemplate jndi = new JndiTemplate();
        
        try {
            dataSource = (DataSource) jndi.lookup(jndiName);
        } catch (NamingException e) {
            logger.error("NamingException for " + jndiName, e);
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
	private DataSource getDriverManagerDataSource(){
		
		logger.info("Initializing driver manager data source:");
		
		logger.info("Database driver = " + appProps.getDatabaseDriver());
		logger.info("Database url = " + appProps.getDatabaseUrl());
		logger.info("Database user = " + appProps.getDatabaseUser());
		logger.info("Database password = *******");
		
		if(StringUtil.isNullEmpty(appProps.getDatabaseDriver()) || StringUtil.isNullEmpty(appProps.getDatabaseUrl()) ||
				StringUtil.isNullEmpty(appProps.getDatabaseUser())){
			
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
