package org.lenzi.fstore.file2.web.rs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Define an jax-rs application to be at /resource
 * 
 * e.g. - if the CXF Servlet is mapped to /cxf and a file service is mapped to /file
 * then the full jax-rs service would be [app_context]/cxf/resource/file
 * 
 * @author slenzi
 */
@ApplicationPath( "resource" )
public class JaxRsFileResourceApplication extends Application {

	public JaxRsFileResourceApplication() {
		
	}

}
