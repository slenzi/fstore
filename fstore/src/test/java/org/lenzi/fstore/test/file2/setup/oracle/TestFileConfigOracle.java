/**
 * 
 */
package org.lenzi.fstore.test.file2.setup.oracle;

import org.lenzi.fstore.core.repository.tree.query.TreeQueryOracleRepository;
import org.lenzi.fstore.core.repository.tree.query.TreeQueryRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.test.core.setup.common.TestCoreConfigDataSource;
import org.lenzi.fstore.test.core.setup.common.TestCoreConfigProperty;
import org.lenzi.fstore.test.core.setup.common.TestCoreConfigSpringACL;
import org.lenzi.fstore.test.core.setup.common.TestCoreConfigSpringSecurity;
import org.lenzi.fstore.test.core.setup.oracle.TestCoreConfigOraclePersistence;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * New java-based config for Oracle unit tests. The older config setup made use of
 * a persistence.xml file and we want to get rid of any XML!
 * 
 * @author slenzi
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableTransactionManagement(proxyTargetClass=true)
@EnableAspectJAutoProxy(proxyTargetClass=true)
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
// use core configuration because it's the same
@Import({
	TestCoreConfigProperty.class,
	TestCoreConfigDataSource.class,
	TestCoreConfigOraclePersistence.class,
	TestCoreConfigSpringSecurity.class,
	TestCoreConfigSpringACL.class
})
@Transactional(transactionManager = "oracleTxManager")
public class TestFileConfigOracle extends AbstractWebSocketMessageBrokerConfigurer {

	@InjectLogger
	private Logger logger;	
	
	public TestFileConfigOracle() {

	}	
	
	@Bean
	@Profile("oracle")
	public TreeQueryRepository getPostgresqlTreeQueryRepository(){
		
		logInfo("Creating Oracle Tree Query Repository");
		
		return new TreeQueryOracleRepository();
		
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
	
	private void logInfo(String message){
		if(logger != null){
			logger.info(message);
		}else{
			System.out.println("> " + message);
		}
	}	

}
