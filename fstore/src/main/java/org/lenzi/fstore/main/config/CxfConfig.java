package org.lenzi.fstore.main.config;

import java.util.Arrays;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.provider.BinaryDataProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.lenzi.fstore.example.web.rs.JaxRsExampleApplication;
import org.lenzi.fstore.example.web.rs.TreeResource;
import org.lenzi.fstore.file2.web.rs.DirectoryResource;
import org.lenzi.fstore.file2.web.rs.FileResource;
import org.lenzi.fstore.file2.web.rs.JaxRsResourceStoreApplication;
import org.lenzi.fstore.file2.web.rs.StoreResource;
import org.lenzi.fstore.web.rs.exception.WebServiceExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configure our Apache CXF services.
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
		
		//
		// Add tree resource bean
		//
		factory.setServiceBeans(
			Arrays.<Object>asList(
					getTreeResourceBean(), getExceptionMapper()
			)
		);
		
		factory.setAddress( factory.getAddress() );
		
		//
		// Add JSON provider
		//
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
		
		//
		// Add service beans
		//
		factory.setServiceBeans(
			Arrays.<Object>asList(
					getFileResource(), getDirectoryResource(), getStoreResource(), getExceptionMapper()
			)
		);
		
		factory.setAddress( factory.getAddress() );
		
		// 
		// Add providers for binary data and json marshalling
		//
		factory.setProviders( Arrays.<Object>asList( 
				getBinaryDataProvider(),
				getJsonProvider()
				) );
		
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
	 * Jax-rs file 2 resource bean
	 * 
	 * @return
	 */
	@Bean
	public FileResource getFileResource(){
		return new FileResource();
	}
	
	/**
	 * Jax-rs store resource bean
	 * 
	 * @return
	 */	
	@Bean
	public StoreResource getStoreResource(){
		return new StoreResource();
	}
	
	/**
	 * jax-rs directory resource bean
	 * 
	 * @return
	 */
	@Bean
	public DirectoryResource getDirectoryResource(){
		return new DirectoryResource();
	}
	
	/**
	 * jax-rs JSON marshalling / provider
	 * 
	 * @return
	 */
	@Bean
    public JacksonJsonProvider getJsonProvider() {
        return new JacksonJsonProvider();
    }
	
	/**
	 * jax-rs binary data marshalling / provider
	 */
	@Bean
	public BinaryDataProvider<Object> getBinaryDataProvider(){
		return new BinaryDataProvider<Object>();
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
