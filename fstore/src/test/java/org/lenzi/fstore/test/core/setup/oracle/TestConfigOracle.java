/**
 * 
 */
package org.lenzi.fstore.test.core.setup.oracle;

import org.lenzi.fstore.core.repository.tree.query.TreeQueryOracleRepository;
import org.lenzi.fstore.core.repository.tree.query.TreeQueryRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.test.core.setup.common.TestConfigDataSource;
import org.lenzi.fstore.test.core.setup.common.TestConfigProperty;
import org.lenzi.fstore.test.core.setup.common.TestConfigSpringACL;
import org.lenzi.fstore.test.core.setup.common.TestConfigSpringSecurity;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * New java-based config for Oracle unit tests. The older config setup made use of
 * a persistence.xml file and we want to get rid of any XML!
 * 
 * @author slenzi
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@EnableAspectJAutoProxy(proxyTargetClass=true)
@ComponentScan(
	basePackages={
		"org.lenzi.fstore.main.properties",
		"org.lenzi.fstore.core.model.util",
		"org.lenzi.fstore.core.repository",
		"org.lenzi.fstore.core.repository.tree.model",
		"org.lenzi.fstore.core.repository.tree.model.impl",
		"org.lenzi.fstore.core.repository.security.model",
		"org.lenzi.fstore.core.repository.security.model.impl",					
		"org.lenzi.fstore.core.service",
		"org.lenzi.fstore.core.logging",
		"org.lenzi.fstore.core.security",
		"org.lenzi.fstore.example.service",
		"org.lenzi.fstore.example.repository",
		"org.lenzi.fstore.example.repository.model.impl",
		"org.lenzi.fstore.cms.repository.model.impl"
	}
)
@Import({
	TestConfigProperty.class,
	TestConfigDataSource.class,
	TestConfigOraclePersistence.class,
	TestConfigSpringSecurity.class,
	TestConfigSpringACL.class
})
@Transactional(transactionManager = "oracleTxManager")
public class TestConfigOracle {

	@InjectLogger
	private Logger logger;	
	
	public TestConfigOracle() {

	}
	
	@Bean
	@Profile("oracle")
	public TreeQueryRepository getPostgresqlTreeQueryRepository(){
		
		logInfo("Creating Oracle Tree Query Repository");
		
		return new TreeQueryOracleRepository();
		
	}
	
	private void logInfo(String message){
		if(logger != null){
			logger.info(message);
		}else{
			System.out.println("> " + message);
		}
	}	

}
