package org.lenzi.fstore.config;

import java.util.Arrays;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.lenzi.fstore.rest.JaxRsApiApplication;
import org.lenzi.fstore.rest.PersonResource;
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
		factory.setServiceBeans(
			Arrays.<Object>asList(
				getPersonServiceBean(), getExceptionMapper()
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
	 * Our persons jax-rs service bean.
	 * 
	 * @return
	 */
	@Bean
	public PersonResource getPersonServiceBean(){
		return new PersonResource();
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
