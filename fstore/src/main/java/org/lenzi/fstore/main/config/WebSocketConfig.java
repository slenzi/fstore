/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Websocket STOMP configuration
 * 
 * Need @ComponentScan annotation even though we have it on our base AppConfig class!  Arg!
 * 
 * @author sal
 */
@Configuration
@EnableWebSocketMessageBroker
@ComponentScan(basePackages = { "org.lenzi.fstore.file2.web.messaging.controller" })
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private ManagedProperties appProps;	
	
	//@Autowired
	//private ObjectMapper objectMapper;
	
	/**
	 * Enable a simple broker with destination prefix /simplebroker
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		
		logger.info("configureMessageBroker called");
		
		//registry.enableSimpleBroker("/simplebroker");
		registry.enableSimpleBroker("/topic");
		
		registry.setApplicationDestinationPrefixes("/app");
		
		logger.info("Message broker registery = " + registry.toString());
	}

	/**
	 * Register a STOMP endpoint at /hello. Our Spring dispatcher is mapped to /spring, and our HelloController
	 * is mapped to receive messages at /hello. We do not need to prefix the endpoint with the Spring MVC dispatcher
	 * mapping.
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		
		logger.info("registerStompEndpoints called");
		
		String sockjsClientUrl = appProps.getProperty("js.sockjs");
		
		logger.info("Using sockjs client at " + sockjsClientUrl);
		
		//registry.addEndpoint("/hello").withSockJS();
		
		// iframe support
		// http://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-fallback-xhr-vs-iframe
		
		// Use the following link (view page source) to see which sockjs client is used for iframe support
		// http://localhost:8080/fstore/spring/hello/iframe.html
		
		//registry.addEndpoint("/hello").withSockJS().setClientLibraryUrl(sockjsClientUrl);
		
		// this version adds a handshake handler
		TomcatRequestUpgradeStrategy tomcatStrategy = new TomcatRequestUpgradeStrategy();
		DefaultHandshakeHandler handshakeHandler = new DefaultHandshakeHandler(tomcatStrategy);
		//registry.addEndpoint("/hello").setHandshakeHandler(handshakeHandler).withSockJS().setClientLibraryUrl(sockjsClientUrl);
		registry.addEndpoint("/hello").setAllowedOrigins("*").setHandshakeHandler(handshakeHandler)
			.withSockJS().setClientLibraryUrl(sockjsClientUrl);
		
		// http://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-server-allowed-origins
		//registry.addEndpoint("/hello").setAllowedOrigins("*").withSockJS();
		
		logger.info("Stomp endpoint registery = " + registry.toString());
	}

	/*
	// TODO - not sure if this is needed...
	@Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
		
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);
        return false;
    }
    */	

}
