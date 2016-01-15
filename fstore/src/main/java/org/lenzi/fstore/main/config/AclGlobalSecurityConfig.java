/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.lenzi.fstore.core.security.FsResourcePermissionEvaluator;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Spring ACL Configuration.
 * 
 * --old comments
 * Allows us to annotate methods with the following annotation to control access.
 * @PreAuthorize("isAuthenticated() and hasPermission(#thingId, 'path.to.Thing', 'read')")
 * There are four possible annotations one can use, @PreAuthorize, @PreFilter, @PostAuthorize and @PostFilter.
 * 
 * @author slenzi
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AclGlobalSecurityConfig extends GlobalMethodSecurityConfiguration {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private JdbcMutableAclService mutableService;
	
	@Autowired
	private FsResourcePermissionEvaluator fsResourcePermissionEvaluator;
	
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		
		logInfo(AclGlobalSecurityConfig.class.getCanonicalName() + ".createExpressionHandler() called");
		
	    DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
	    expressionHandler.setPermissionEvaluator(new AclPermissionEvaluator(mutableService));
	    return expressionHandler;
	}

	/*
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator( fsResourcePermissionEvaluator );
		return expressionHandler;
		
	}
	*/
	
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
