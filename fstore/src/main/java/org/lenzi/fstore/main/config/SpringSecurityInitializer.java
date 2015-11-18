/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * @author slenzi
 *
 * Needed to initialize spring security...
 * 
 * Section 5.2 of example
 * http://www.mkyong.com/spring-security/spring-security-hello-world-annotation-example/
 */
public class SpringSecurityInitializer extends AbstractSecurityWebApplicationInitializer {

	//
	// do nothing
	//
	
	/**
	 * 
	 */
	public SpringSecurityInitializer() {
		
	}

	/**
	 * @param configurationClasses
	 */
	public SpringSecurityInitializer(Class<?>... configurationClasses) {
		super(configurationClasses);
	}

}
