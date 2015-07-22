/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	/**
	 * Enable a simple broker with destination prefix /simplebroker
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		
		logger.info("configureMessageBroker called");
		
		registry.enableSimpleBroker("/simplebroker");
		
		registry.setApplicationDestinationPrefixes("/app");
		
		logger.info(registry.toString());
	}

	/**
	 * Register a STOMP endpoint at /hello. Our Spring dispatcher is mapped to /spring, and our HelloController
	 * is mapped to receive messages at /hello. We do not need to prefix the endpoint with the Spring MVC dispatcher
	 * mapping.
	 */
	/* (non-Javadoc)
	 * @see org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer#registerStompEndpoints(org.springframework.web.socket.config.annotation.StompEndpointRegistry)
	 */
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		
		logger.info("registerStompEndpoints called");
		
		registry.addEndpoint("/hello").withSockJS();
		
		logger.info(registry.toString());
	}


}
