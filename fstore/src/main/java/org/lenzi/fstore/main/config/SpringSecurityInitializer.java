/**
 * 
 */
package org.lenzi.fstore.main.config;

import javax.servlet.ServletContext;

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
@Order(1) // should always be registered in first place (= before WebAppInitializer)
public class SpringSecurityInitializer extends AbstractSecurityWebApplicationInitializer {

	/* (non-Javadoc)
	 * @see org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer#beforeSpringSecurityFilterChain(javax.servlet.ServletContext)
	 */
	@Override
	protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		
		// prevent spring security from intercepting and block multipart uploads
		// http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#csrf-multipart
		
		System.out.println(SpringSecurityInitializer.class.getName() + ".beforeSpringSecurityFilterChain(ServletContext) called.");
		System.out.println("Inserting MultipartFilter");
		
		insertFilters(servletContext, new MultipartFilter());
		
	}

	//
	// do nothing
	//
	
	// http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#abstractsecuritywebapplicationinitializer-with-spring-mvc


	
}
