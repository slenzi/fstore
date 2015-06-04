package org.lenzi.fstore.example.web.rs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Define an jax-rs application to be at /api
 * 
 * e.g. - if the CXF Servlet is mapped to /cxf and a tree service is mapped to /tree
 * then the full jax-rs service would be [app_context]/cxf/api/tree
 * 
 * @author slenzi
 */
@ApplicationPath( "api" )
public class JaxRsApiApplication extends Application {


}
