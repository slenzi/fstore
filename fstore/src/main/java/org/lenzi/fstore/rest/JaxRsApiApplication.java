package org.lenzi.fstore.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Define an jax-rs application to be at /api
 * 
 * e.g. - if the CXF Servlet is mapped to /cxf and a person service is mapped to /person
 * then the full jax-rs service would be [app_context]/cxf/api/person
 * 
 * @author slenzi
 */
@ApplicationPath( "api" )
public class JaxRsApiApplication extends Application {


}
