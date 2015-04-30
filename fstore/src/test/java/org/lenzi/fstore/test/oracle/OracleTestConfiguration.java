/**
 * 
 */
package org.lenzi.fstore.test.oracle;

import java.io.IOException;

import org.lenzi.fstore.repository.TreeRepository;
import org.lenzi.fstore.repository.OracleTestTreeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * @author sal
 *
 * Configuration setup for our Oracle unit test cases.
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@ComponentScan(
		basePackages={
			"org.lenzi.fstore.model.util",
			"org.lenzi.fstore.repository",
			"org.lenzi.fstore.repository.model",
			"org.lenzi.fstore.service",
			"org.lenzi.fstore.logging"
		}
)
// "org.lenzi.fstore.service"
@EnableAspectJAutoProxy(proxyTargetClass=true)
@TransactionConfiguration(transactionManager="oracleTxManager", defaultRollback=true)
public class OracleTestConfiguration implements TransactionManagementConfigurer {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	// defined in /src/test/resources/META-INF/test-oracle-persistence.xml
	private String persistenceUnitName = "FStoreOracleSQLPersistenceUnit";
	
	/**
	 * 
	 */
	public OracleTestConfiguration() {
	
	}
	
	/**
	 * Oracle closure repository
	 * 
	 * @return
	 */
	@Bean
	@Profile("oracle")
	public TreeRepository getOracleTreeRepository(){
		
		logger.info("Getting OracleTestTreeRepository");
		
		return new OracleTestTreeRepository();
	}

	/**
	 * Transaction manager
	 */
    @Bean(name="oracleTxManager")
    @Qualifier("oracle")
	public PlatformTransactionManager annotationDrivenTransactionManager() {
    	JpaTransactionManager txManager = new JpaTransactionManager();
    	txManager.setPersistenceUnitName(persistenceUnitName);
		return txManager;
	}

    /**
     * Entity manager factory
     * 
     * @return
     * @throws IOException
     */
    @Bean
    @Qualifier("oracle")
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory() throws IOException {	
    	LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    	emf.setPersistenceXmlLocation("classpath:META-INF/test-oracle-persistence.xml");
    	emf.setPersistenceUnitName(persistenceUnitName);
    	return emf; 
    }
    
}
