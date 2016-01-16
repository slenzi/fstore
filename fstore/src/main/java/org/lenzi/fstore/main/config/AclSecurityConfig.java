package org.lenzi.fstore.main.config;

import javax.sql.DataSource;

import net.sf.ehcache.CacheManager;

import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole.Role;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Main config for Spring's ACL Security
 * 
 * @author slenzi
 */
@Configuration
public class AclSecurityConfig {

	@InjectLogger
	private Logger logger;
	
	// the datasource we'll use for spring security acl. This is the same datasource we use
	// in our PersistenceConfig.class for JPA & Hibernate.
	@Autowired
	@Qualifier("primaryDataSource")
	private DataSource primaryDataSource;
	
	// for ingres
	@Bean
	@Profile("postgresql")
	public JdbcMutableAclService aclServicePostgresql() {
		
		logInfo(AclSecurityConfig.class.getCanonicalName() + ".aclServicePostgresql() called");
		
	    JdbcMutableAclService service = new JdbcMutableAclService(primaryDataSource, lookupStrategy(), aclCache());
	    service.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
	    service.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
	    return service;
	}
	
	// for oracle
	@Bean
	@Profile("oracle")
	public JdbcMutableAclService aclServiceOracle() {
		
		logInfo(AclSecurityConfig.class.getCanonicalName() + ".aclServiceOracle() called");
		
	    JdbcMutableAclService service = new JdbcMutableAclService(primaryDataSource, lookupStrategy(), aclCache());
	    // no mention of manually setting class identity and sid identity queries for oracle....just postgresql
	    return service;
	}
	
	@Bean
	public AclAuthorizationStrategy aclAuthorizationStrategy() {
		
		logInfo(AclSecurityConfig.class.getCanonicalName() + ".aclAuthorizationStrategy() called");
		
	    return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority( Role.ADMINISTRATOR.getRoleCode() ));
	}
	
	@Bean
	public EhCacheBasedAclCache aclCache() {
		
		logInfo(AclSecurityConfig.class.getCanonicalName() + ".aclCache() called");
		
	    EhCacheFactoryBean factoryBean = new EhCacheFactoryBean();
	    EhCacheManagerFactoryBean cacheManager = new EhCacheManagerFactoryBean();
	    
		cacheManager.setAcceptExisting(true);
		cacheManager.setCacheManagerName(CacheManager.getInstance().getName());
		cacheManager.afterPropertiesSet();
		
		factoryBean.setName("aclCache");
		factoryBean.setCacheManager(cacheManager.getObject());
		factoryBean.setMaxBytesLocalHeap("16M");
		factoryBean.setMaxEntriesLocalHeap(0L);
		factoryBean.afterPropertiesSet();
	    
	    return new EhCacheBasedAclCache( factoryBean.getObject(), permissionGrantingStrategy(), aclAuthorizationStrategy());
	    
	}
	
	@Bean
	public PermissionGrantingStrategy permissionGrantingStrategy(){
		
		logInfo(AclSecurityConfig.class.getCanonicalName() + ".permissionGrantingStrategy() called");
		
		PermissionGrantingStrategy permissionGrantingStrategy = new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
		return permissionGrantingStrategy;
	}
	
	@Bean
	public LookupStrategy lookupStrategy() {
		
		logInfo(AclSecurityConfig.class.getCanonicalName() + ".lookupStrategy() called");
		
	    return new BasicLookupStrategy(primaryDataSource, aclCache(), aclAuthorizationStrategy(), new ConsoleAuditLogger());
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
