/**
 * 
 */
package org.lenzi.fstore.main.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.multipart.support.MultipartFilter;

/**
 * @author slenzi
 *
 * Needed to initialize spring security...
 * 
 * Section 5.2 of example
 * http://www.mkyong.com/spring-security/spring-security-hello-world-annotation-example/
 */
@Order(1) // should always be registered in first place (before WebAppInitializer)
public class SpringSecurityInitializer extends AbstractSecurityWebApplicationInitializer {

	/* (non-Javadoc)
	 * @see org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer#beforeSpringSecurityFilterChain(javax.servlet.ServletContext)
	 */
	@Override
	protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		
		//
		// NOTE: When we place the MultipartFilter before before the Spring Security Filter we essentially turn
		// off security for Multipart HTTP uploads. In this scenario spring security is not triggered. This poses
		// a problem when someone uploads a file because the authentication principle (logged in user) will be null.
		// We want to know who is uploading so we need to make sure the spring security is triggered for multipart
		// uploads, AND, we need to pass the CSRF token when HTTP uploads are sent to the server, otherwise spring
		// security will reject the request.
		//
		
		// prevent spring security from intercepting and block multipart uploads
		// http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#csrf-multipart
		
		// turn off. see notes above.
		//insertFilters(servletContext, new MultipartFilter());
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer#afterSpringSecurityFilterChain(javax.servlet.ServletContext)
	 */
	@Override
	protected void afterSpringSecurityFilterChain(ServletContext servletContext) {
		
		//
		// See notes above. We place the multipart filter after the spring security filter, and pass the CSRF token during http uploads
		// http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#csrf-include-csrf-token-in-action
		//
		insertFilters(servletContext, new MultipartFilter());
		
	}
	
	
	
}
