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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Enables method level security using Spring Security.
 * 
 * Allows us to annotate methods with the following annotation to control access.
 * 
 * @PreAuthorize("isAuthenticated() and hasPermission(#thingId, 'path.to.Thing', 'read')")
 * 
 * There are four possible annotations one can use, @PreAuthorize, @PreFilter, @PostAuthorize and @PostFilter.
 * 
 * @author slenzi
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourcePermissionEvaluator fsResourcePermissionEvaluator;
	
	/**
	 * 
	 */
	public MethodSecurityConfig() {
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration#createExpressionHandler()
	 */
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		
		expressionHandler.setPermissionEvaluator( fsResourcePermissionEvaluator );
		
		return expressionHandler;
		
	}

}
