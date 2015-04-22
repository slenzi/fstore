package org.lenzi.fstore.config;

import java.util.Arrays;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.lenzi.fstore.rest.JaxRsApiApplication;
import org.lenzi.fstore.rest.PersonResource;
import org.lenzi.fstore.rest.TreeResource;
import org.lenzi.fstore.rest.exception.WebServiceExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configure our Apache CXF "api" service
 * 
 * @author slenzi
 */
@Configuration
public class CxfConfig {

	/**
	 * will call shutdown on SpringBus when bean is destroyed.
	 * 
	 * @return
	 */
	@Bean( destroyMethod = "shutdown" )
	public SpringBus cxf() {
		return new SpringBus();
	}
	
	@Bean @DependsOn ( "cxf" )
	public Server jaxRsServer() {
		
		JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance().createEndpoint( jaxRsApiApplication(), JAXRSServerFactoryBean.class );
		
		// add all our service beans
		factory.setServiceBeans(
			Arrays.<Object>asList(
					getTreeResourceBean(), getPersonResourceBean(), getExceptionMapper()
			)
		);
		
		factory.setAddress( factory.getAddress() );
		factory.setProviders( Arrays.<Object>asList( getJsonProvider() ) );
		return factory.create();
	}
	
	/**
	 * Create our "/api" jax-rs application 
	 * 
	 * @return
	 */
	@Bean 
	public JaxRsApiApplication jaxRsApiApplication() {
		return new JaxRsApiApplication();
	}
	
	/**
	 * Jax-rs person service bean
	 * 
	 * @return
	 */
	@Bean
	public PersonResource getPersonResourceBean(){
		return new PersonResource();
	}
	
	/**
	 * Jax-rs tree service bean
	 * 
	 * @return
	 */
	@Bean
	public TreeResource getTreeResourceBean(){
		return new TreeResource();
	}
	
	/**
	 * jax-rs JSON marshalling
	 * 
	 * @return
	 */
	@Bean
    public JacksonJsonProvider getJsonProvider() {
        return new JacksonJsonProvider();
    }
	
	/**
	 * Maps our WebServiceException to http response codes.
	 * 
	 * @return
	 */
	@Bean
	public WebServiceExceptionMapper getExceptionMapper(){
		return new WebServiceExceptionMapper();
	}

}
