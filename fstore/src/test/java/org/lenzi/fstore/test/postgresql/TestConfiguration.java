/**
 * 
 */
package org.lenzi.fstore.test.postgresql;

import java.io.IOException;

import org.lenzi.fstore.logging.LoggerBeanPostProccessor;
import org.lenzi.fstore.repository.ClosureRepository;
import org.lenzi.fstore.repository.OracleClosureRepository;
import org.lenzi.fstore.repository.PostgresClosureRepository;
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
 * Configuration setup for our unit test cases.
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
@TransactionConfiguration(transactionManager="postgresqlTxManager", defaultRollback=false)
public class TestConfiguration implements TransactionManagementConfigurer {

	// defined in /src/test/resources/META-INF/test-persistence.xml
	private String persistenceUnitName = "FStoreTestPostgreSQLPersistenceUnit";
	
	/**
	 * 
	 */
	public TestConfiguration() {
	
	}
	
	/**
	 * Oracle closure repository
	 * 
	 * @return
	 */
	@Bean
	@Profile("oracle")
	public ClosureRepository getOracleClosureRepository(){
		return new OracleClosureRepository();
	}
	
	/**
	 * PostgreSQL closure repository
	 * 
	 * @return
	 */
	@Bean
	@Profile("postgresql")
	public ClosureRepository getPostgresClosureRepository(){
		return new PostgresClosureRepository();
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
    	emf.setPersistenceXmlLocation("classpath:META-INF/test-persistence.xml");
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
