/**
 * 
 */
package org.lenzi.fstore.test.file2.setup;

import java.io.IOException;

import org.lenzi.fstore.core.repository.tree.query.TreeQueryPostgresqlRepository;
import org.lenzi.fstore.core.repository.tree.query.TreeQueryRepository;
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
 * Configuration setup for our PostgreSQL unit test cases.
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@ComponentScan(
		basePackages={
				"org.lenzi.fstore.core.model.util",
				"org.lenzi.fstore.core.repository",
				"org.lenzi.fstore.core.repository.model",
				"org.lenzi.fstore.core.repository.model.impl",
				"org.lenzi.fstore.core.service",
				"org.lenzi.fstore.core.logging",
				"org.lenzi.fstore.example.repository",
				"org.lenzi.fstore.example.repository.model.impl",
				"org.lenzi.fstore.file2.service",
				"org.lenzi.fstore.file2.repository",
				"org.lenzi.fstore.file2.repository.model.impl"
		}
)
@EnableAspectJAutoProxy(proxyTargetClass=true)
@TransactionConfiguration(transactionManager="postgresqlTxManager", defaultRollback=true)
public class PostgresqlCmsTestConfiguration implements TransactionManagementConfigurer {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	// defined in /src/test/resources/META-INF/test-postgresql-persistence.xml
	private String persistenceUnitName = "FStoreTestPostgreSQLPersistenceUnit";
	
	/**
	 * 
	 */
	public PostgresqlCmsTestConfiguration() {
	
	}
	
	/**
	 * Repository tree related query functions specific to PostgreSQL.
	 * 
	 * @return
	 */
	@Bean
	@Profile("postgresql")
	public TreeQueryRepository getPostgresqlTreeQueryRepository(){
		
		logger.info("Creating PostgreSQL Tree Query Repository");
		
		return new TreeQueryPostgresqlRepository();
		
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
    
}
