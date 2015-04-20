package org.lenzi.fstore.config;

import java.util.Properties;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.lenzi.fstore.properties.ManagedProperties;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA + Hibernate Setup
 * 
 * @author slenzi
 */
@Configuration 
@EnableTransactionManagement
public class PersistenceConfig {
	
    @Autowired
    private ManagedProperties appProps;
    
	@InjectLogger
	private Logger logger;
	
	/**
	 * Setup entity manager factory
	 * 
	 * @return
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		
		//em.setDataSource(getDriverManagerDataSource());
		em.setDataSource(getJndiDataSource());
		
		em.setPackagesToScan(new String[] { "org.lenzi.fstore.repository.model" });

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());

		return em;
	}
	
	/**
	 * Fetch data source from JNDI.
	 * 
	 * @return
	 */
	@Bean
	public DataSource getJndiDataSource() {
		
		DataSource dataSource = null;
        JndiTemplate jndi = new JndiTemplate();
        
        String jndiDataSourceName = appProps.getProperty("database.jndi.pool");
        
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
	public DataSource getDriverManagerDataSource(){
		
		logger.info("Initializing Postgres data source:");
		
		logger.info("Database driver = " + appProps.getDatabaseDriver());
		logger.info("Database url = " + appProps.getDatabaseUrl());
		logger.info("Database user = " + appProps.getDatabaseUser());
		logger.info("Database password = *******");
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(appProps.getDatabaseDriver());
		dataSource.setUrl(appProps.getDatabaseUrl());
        dataSource.setUsername(appProps.getDatabaseUser());
        dataSource.setPassword(appProps.getDatabasePassword());
        
        return dataSource;
	}

	/**
	 * Transaction setup
	 * 
	 * @param emf
	 * @return
	 */
	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
		
	}
	
	/**
	 * Additional database connection properties
	 * 
	 * @return
	 */
	public Properties additionalProperties() {
		
		Properties properties = new Properties();
		
		properties.setProperty("hibernate.show_sql", appProps.getProperty("hibernate.show_sql"));
		properties.setProperty("hibernate.jdbc.batch_size", appProps.getProperty("hibernate.jdbc.batch_size"));
		properties.setProperty("hibernate.dialect",appProps.getProperty("hibernate.dialect"));
		properties.setProperty("hibernate.generate_statistics",appProps.getProperty("hibernate.generate_statistics"));
		
		logger.info("Additional JPA Hibernate properties:");
		
		for(String propName : properties.stringPropertyNames()){
			logger.info(propName + " = " + properties.getProperty(propName));
		}
		
		return properties;
		
	}	

}
