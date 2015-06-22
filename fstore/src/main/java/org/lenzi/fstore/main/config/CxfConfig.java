package org.lenzi.fstore.main.config;

import java.util.Arrays;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.lenzi.fstore.example.web.rs.JaxRsExampleApplication;
import org.lenzi.fstore.example.web.rs.TreeResource;
import org.lenzi.fstore.file2.web.rs.FileResource;
import org.lenzi.fstore.file2.web.rs.JaxRsResourceStoreApplication;
import org.lenzi.fstore.web.rs.exception.WebServiceExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configure our Apache CXF services
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
	
	/**
	 * Load "example" server which runs example service
	 * 
	 * @return
	 */
	@Bean(name="jaxRsExampleServer")
	@DependsOn ( "cxf" )
	public Server getJaxRsExampleServer() {
		
		RuntimeDelegate delegate = RuntimeDelegate.getInstance();
		
		JAXRSServerFactoryBean factory = delegate.createEndpoint( 
				getJaxRsExampleApplication(), JAXRSServerFactoryBean.class );
		
		// add all our service beans
		factory.setServiceBeans(
			Arrays.<Object>asList(
					getTreeResourceBean(), getExceptionMapper()
			)
		);
		
		factory.setAddress( factory.getAddress() );
		factory.setProviders( Arrays.<Object>asList( getJsonProvider() ) );
		
		return factory.create();
	}
	
	/**
	 * Load "resource" server which runs file resource service
	 * 
	 * @return
	 */
	@Bean(name="jaxRsResourceServer")
	@DependsOn ( "cxf" )
	public Server getJaxRsResourceServer() {
		
		RuntimeDelegate delegate = RuntimeDelegate.getInstance();
		
		JAXRSServerFactoryBean factory = delegate.createEndpoint( 
				getJaxRsResourceStoreApplication(), JAXRSServerFactoryBean.class );
		
		// add all our service beans
		factory.setServiceBeans(
			Arrays.<Object>asList(
					getFileResourceBean(), getExceptionMapper()
			)
		);
		
		factory.setAddress( factory.getAddress() );
		
		// TODO - no need for JSON provider, but might need a provider for binary data?
		factory.setProviders( Arrays.<Object>asList( getJsonProvider() ) );
		
		return factory.create();
	}
	
	/**
	 * Create our "/example" jax-rs application 
	 * 
	 * @return
	 */
	@Bean 
	public JaxRsExampleApplication getJaxRsExampleApplication() {
		return new JaxRsExampleApplication();
	}
	
	/**
	 * Create our "/resource" jax-rs application
	 * 
	 * @return
	 */
	@Bean
	public JaxRsResourceStoreApplication getJaxRsResourceStoreApplication(){
		return new JaxRsResourceStoreApplication();
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
	 * Jax-rs file resource bean
	 * 
	 * @return
	 */
	@Bean
	public FileResource getFileResourceBean(){
		return new FileResource();
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
