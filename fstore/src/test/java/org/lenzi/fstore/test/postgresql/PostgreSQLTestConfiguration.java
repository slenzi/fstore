/**
 * 
 */
package org.lenzi.fstore.test.postgresql;

import java.io.IOException;

import org.lenzi.fstore.logging.LoggerBeanPostProccessor;
import org.lenzi.fstore.repository.ClosureRepository;
import org.lenzi.fstore.repository.PostgreSQLClosureRepository;
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
 * Configuration setup for our PostgreSQL unit test cases.
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@ComponentScan(
		basePackages={
			"org.lenzi.fstore.repository",
			"org.lenzi.fstore.repository.model",
			"org.lenzi.fstore.service"
		}
)
@EnableAspectJAutoProxy(proxyTargetClass=true)
@TransactionConfiguration(transactionManager="postgresqlTxManager", defaultRollback=true)
public class PostgreSQLTestConfiguration implements TransactionManagementConfigurer {

	// defined in /src/test/resources/META-INF/test-postgresql-persistence.xml
	private String persistenceUnitName = "FStoreTestPostgreSQLPersistenceUnit";
	
	/**
	 * 
	 */
	public PostgreSQLTestConfiguration() {
	
	}
	
	/**
	 * PostgreSQL closure repository
	 * 
	 * @return
	 */
	@Bean
	@Profile("postgresql")
	public ClosureRepository getPostgresClosureRepository(){
		return new PostgreSQLClosureRepository();
	}

	/**
	 * Transaction manager
	 */
    @Bean(name="postgresqlTxManager")
    @Qualifier("postgresql")
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
    @Qualifier("postgresql")
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory() throws IOException {	
    	LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    	emf.setPersistenceXmlLocation("classpath:META-INF/test-postgresql-persistence.xml");
    	emf.setPersistenceUnitName(persistenceUnitName);
    	return emf; 
    }
    
    /**
     * To resolve our InjectLogger injection points.
     * 
     * @return
     */
    @Bean
    public LoggerBeanPostProccessor getLoggerBeanPostProcessor(){
    	return new LoggerBeanPostProccessor();
    }
}
