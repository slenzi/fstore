package org.lenzi.fstore.cms.web.rs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Define an jax-rs application to be at /cms
 * 
 * e.g. - if the CXF Servlet is mapped to /cxf and a cms service is mapped to /site
 * then the full jax-rs service would be [app_context]/cxf/cms/site
 * 
 * @author slenzi
 */
@ApplicationPath( "cms" )
public class JaxRsCmsResourceApplication extends Application {

	public JaxRsCmsResourceApplication() {
		
	}

}
