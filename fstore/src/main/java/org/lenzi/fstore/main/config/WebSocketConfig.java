/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Websocket configuration
 * 
 * @author sal
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@InjectLogger
	private Logger logger;	
	
	/**
	 * Enable a simple broker with destination prefix /simplebroker
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		
		logger.info("configureMessageBroker called");
		
		registry.enableSimpleBroker("/simplebroker");
		
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
		
		registry.addEndpoint("/hello").withSockJS();
		
		logger.info("Stomp endpoint registery = " + registry.toString());
	}


}
