/**
 * 
 */
package org.lenzi.fstore.test.core.setup.postgres;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiTemplate;

/**
 * Configures Postgres datasource to be used with our Postgres unit tests.
 * 
 * @See src/test/resources/my.application.test.properties for database connection details.
 * 
 * @author slenzi
 */
@Configuration
public class TestConfigPostgresDataSource {

    @Autowired
    private ManagedProperties appProps;
    
	@InjectLogger
	private Logger logger;    
	
	/**
	 * 
	 */
	public TestConfigPostgresDataSource() {
	
	}
	
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
		
		String jndiPropName = "database.jndi.pool.main";
		
		String jndiDataSourceName = appProps.getProperty(jndiPropName);
		
		if(!StringUtil.isNullEmpty(jndiDataSourceName)){
			
			logInfo("Getting data source from JNDI, name => " + jndiDataSourceName);
			
			JndiTemplate jndi = new JndiTemplate();
	        try {
	            dataSource = (DataSource) jndi.lookup(jndiDataSourceName);
	        } catch (NamingException e) {
	        	logError("NamingException for " + jndiDataSourceName, e);
	        }
	        
		}else{
			
			logInfo("No JNDI data source name found for property => " + jndiPropName);
			
			dataSource = getDriverManagerDataSource();
			
		}
		
		return dataSource;
		
	}
	
	/**
	 * Get DataSource for database.
	 * 
	 * @See src/test/resources/my.application.test.properties for database connection details.
	 * 
	 * @return
	 */
	private DataSource getDriverManagerDataSource(){
		
		logInfo("Initializing driver manager data source:");
		
		// uses test version of connection properties. all test properties are prefixed with 'test.'
		String dbDriver = StringUtil.changeNull(appProps.getProperty("test.database.driver"));
		String dbUrl = StringUtil.changeNull(appProps.getProperty("test.database.url"));
		String dbUser = StringUtil.changeNull(appProps.getProperty("test.database.user"));
		String dbPwd = StringUtil.changeNull(appProps.getProperty("test.database.password"));
		
		logInfo("Database driver = " + dbDriver);
		logInfo("Database url = " + dbUrl);
		logInfo("Database user = " + dbUser);
		logInfo("Database password = *******");
		
		if(StringUtil.isNullEmpty(appProps.getDatabaseDriver()) || StringUtil.isNullEmpty(appProps.getDatabaseUrl()) ||
				StringUtil.isNullEmpty(appProps.getDatabaseUser())){
			
			logError("Missing required values for data source. Check driver name, connection url, username, and/or password", null);
			
			return null;
		}
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(dbDriver);
		dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPwd);

        return dataSource;
	}	
	
	private void logInfo(String message){
		if(logger != null){
			logger.info(message);
		}else{
			System.out.println("> " + message);
		}
	}
	
	private void logError(String message, Throwable t){
		if(logger != null){
			logger.error(message, t);
		}else{
			System.err.println("> " + message + ((t != null) ? " " + t.getMessage() : ""));
		}
	}	

}
