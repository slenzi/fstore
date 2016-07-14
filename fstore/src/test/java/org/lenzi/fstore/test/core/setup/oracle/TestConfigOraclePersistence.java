/**
 * 
 */
package org.lenzi.fstore.test.core.setup.oracle;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Oracle persistence config used for oracle unit tests.
 * 
 * @author slenzi
 */
@Configuration 
@EnableTransactionManagement
public class TestConfigOraclePersistence {

    @Autowired
    private ManagedProperties appProps;
    
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("primaryDataSource")
	private DataSource primaryDataSource;
	
	private final String[] entityPackages = new String[]{
			
			"org.lenzi.fstore.core.repository.security.model.impl",
			"org.lenzi.fstore.core.repository.tree.model.impl",
			
			"org.lenzi.fstore.file2.repository.model.impl",
			"org.lenzi.fstore.cms.repository.model.impl",
			
			"org.lenzi.fstore.example.repository.model.impl"
				
		};	
	
	/**
	 * 
	 */
	public TestConfigOraclePersistence() {
		
	}
	
	/**
	 * Setup entity manager factory
	 * 
	 * @return
	 */
	@Bean
	@Qualifier("oracle")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		
		em.setDataSource(primaryDataSource);
		
		em.setPackagesToScan(entityPackages);

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());

		return em;
	}

	/**
	 * Transaction setup
	 * 
	 * @param emf
	 * @return
	 */
    @Bean(name="oracleTxManager")
    @Qualifier("oracle")	
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
		
		properties.setProperty("hibernate.default_schema", appProps.getProperty("hibernate.default_schema"));
		properties.setProperty("hibernate.show_sql", appProps.getProperty("hibernate.show_sql"));
		properties.setProperty("hibernate.format_sql", appProps.getProperty("hibernate.format_sql"));
		properties.setProperty("hibernate.jdbc.batch_size", appProps.getProperty("hibernate.jdbc.batch_size"));
		properties.setProperty("hibernate.dialect", appProps.getProperty("hibernate.dialect"));
		properties.setProperty("hibernate.driver", appProps.getProperty("hibernate.driver"));
		properties.setProperty("hibernate.generate_statistics", appProps.getProperty("hibernate.generate_statistics"));
		properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", appProps.getProperty("hibernate.temp.use_jdbc_metadata_defaults"));
		
		logInfo("Additional JPA Hibernate properties:");
		
		for(String propName : properties.stringPropertyNames()){
			logInfo(propName + " = " + properties.getProperty(propName));
		}
		
		return properties;
		
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
