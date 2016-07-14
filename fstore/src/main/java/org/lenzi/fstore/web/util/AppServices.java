/**
 * 
 */
package org.lenzi.fstore.web.util;

import javax.servlet.ServletContext;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sal
 *
 */
@Service
public class AppServices {

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private ManagedProperties appProps; 	
	
	@Autowired
	ServletContext servletContext = null;	
	
	/**
	 * 
	 */
	public AppServices() {
		
	}
	
	/**
	 * Get directory from which the application is running (i.e., under the servlet container, e.g. [tomcat_home]/webapps/appName)
	 * 
	 * @return
	 */
	public String getRuntimePath(){
		
		return servletContext.getRealPath("");
		
	}

}
