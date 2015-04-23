package org.lenzi.fstore.database.setup.oracle.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.lenzi.fstore.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Persistence config used for setting up the Oracle database.
 * 
 * @author sal
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(
	basePackages = "org.lenzi.fstore.database.setup.oracle",
	excludeFilters = {
		@ComponentScan.Filter(value = Controller.class, type = FilterType.ANNOTATION),
		@ComponentScan.Filter(value = Configuration.class, type = FilterType.ANNOTATION)
	}
)
@Import({
	OracleSetupPropertyConfig.class
})
public class OracleSetupConfig {

	private Logger logger = LoggerFactory.getLogger(OracleSetupConfig.class);
	
	@Autowired
	private OracleSetupProperties setupProps;
	
	/**
	 * Setup entity manager factory
	 * 
	 * @return
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		
		em.setDataSource(getDriverManagerDataSource());
		
		em.setPackagesToScan(new String[] { "org.lenzi.fstore.repository.model" });

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
	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
		
	}
	
	@Bean
	public DataSource getDriverManagerDataSource(){
		
		logger.info("Initializing driver manager data source:");
		
		String driver = setupProps.getProperty("database.driver");
		String url = setupProps.getProperty("database.url");
		String user = setupProps.getProperty("database.user");
		String pwd = setupProps.getProperty("database.password");
		
		logger.info("Database driver = " + driver);
		logger.info("Database url = " + url);
		logger.info("Database user = " + user);
		logger.info("Database password = *****");
		
		if(StringUtil.isNullEmpty(driver) || StringUtil.isNullEmpty(url) || StringUtil.isNullEmpty(user)){
			
			logger.error("Missing required values for data source. Check driver name, connection url, username, and/or password");
			
			return null;
		}
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(pwd);
        
        return dataSource;
        
	}
	
	public Properties additionalProperties() {
		
		Properties properties = new Properties();
		
		properties.setProperty("hibernate.default_schema", setupProps.getProperty("hibernate.default_schema"));
		properties.setProperty("hibernate.show_sql", setupProps.getProperty("hibernate.show_sql"));
		properties.setProperty("hibernate.format_sql", setupProps.getProperty("hibernate.format_sql"));
		properties.setProperty("hibernate.jdbc.batch_size", setupProps.getProperty("hibernate.jdbc.batch_size"));
		properties.setProperty("hibernate.dialect", setupProps.getProperty("hibernate.dialect"));
		properties.setProperty("hibernate.driver", setupProps.getProperty("hibernate.driver"));
		properties.setProperty("hibernate.generate_statistics", setupProps.getProperty("hibernate.generate_statistics"));
		properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", setupProps.getProperty("hibernate.temp.use_jdbc_metadata_defaults"));
		
		logger.info("Additional JPA Hibernate properties:");
		
		for(String propName : properties.stringPropertyNames()){
			logger.info(propName + " = " + properties.getProperty(propName));
		}
		
		return properties;
		
	}	

}
