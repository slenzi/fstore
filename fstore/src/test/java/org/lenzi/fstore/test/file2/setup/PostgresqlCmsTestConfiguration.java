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
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

//deprecated as of spring 4.2, instead use @Rollback at the class level and the transactionManager qualifier in @Transactional
//import org.springframework.test.context.transaction.TransactionConfiguration;
// @TransactionConfiguration(transactionManager="postgresqlTxManager", defaultRollback=true)

/**
 * @author sal
 *
 * Configuration setup for our PostgreSQL unit test cases.
 * 
 * @deprecated - Used an XML file for persistence setup. We don't like XML! Replaced with
 * new configuration that is purely java based.
 */
@Configuration
@EnableWebSocketMessageBroker
@PropertySource("classpath:my.application.test.properties")
@EnableTransactionManagement(proxyTargetClass=true)
@ComponentScan(
	basePackages={
		"org.lenzi.fstore.core.model.util",
		"org.lenzi.fstore.core.repository",
		"org.lenzi.fstore.core.repository.tree.model",
		"org.lenzi.fstore.core.repository.tree.model.impl",
		"org.lenzi.fstore.core.repository.security.model",
		"org.lenzi.fstore.core.repository.security.model.impl",					
		"org.lenzi.fstore.core.service",
		"org.lenzi.fstore.core.logging",
		"org.lenzi.fstore.core.security",
		"org.lenzi.fstore.main.properties",
		"org.lenzi.fstore.example.repository",
		"org.lenzi.fstore.example.repository.model.impl",
		"org.lenzi.fstore.file2.service",
		"org.lenzi.fstore.file2.repository",
		"org.lenzi.fstore.file2.repository.model.impl",
		"org.lenzi.fstore.file2.concurrent",
		"org.lenzi.fstore.file2.web.messaging",
		"org.lenzi.fstore.cms.repository.model.impl"
	}
)
@EnableAspectJAutoProxy(proxyTargetClass=true)
@Rollback
@Transactional(transactionManager = "postgresqlTxManager")
public class PostgresqlCmsTestConfiguration extends AbstractWebSocketMessageBrokerConfigurer implements TransactionManagementConfigurer {

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
    
    /**
     * For spring managed properties
     * 
     * @return
     */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	/**
	 * Needed for autowiring org.lenzi.fstore.file2.web.messaging dependencies
	 * 
	 * @param registry
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");
		registry.setApplicationDestinationPrefixes("/app");
	}

	/**
	 * Needed for autowiring org.lenzi.fstore.file2.web.messaging dependencies
	 * 
	 * @param arg0
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/hello").withSockJS();
	} 	
    
}
