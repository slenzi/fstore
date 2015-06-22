package org.lenzi.fstore.example.web.rs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Define an jax-rs application to be at /example
 * 
 * e.g. - if the CXF Servlet is mapped to /cxf and a tree service is mapped to /tree
 * then the full jax-rs service would be [app_context]/cxf/example/tree
 * 
 * @author slenzi
 */
@ApplicationPath( "example" )
public class JaxRsExampleApplication extends Application {


}
