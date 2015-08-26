/**
 * 
 */
package org.lenzi.fstore.main.config;

import org.lenzi.fstore.chat.web.handler.ChatWebSocketHandler;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

/**
 * Websocket chat configuration
 * @author sal
 */
@Configuration
@EnableWebSocket
public class WebSocketChatConfig implements WebSocketConfigurer {

	@InjectLogger
	private Logger logger;

	/**
	 * For each client connection, register a ChatWebSocketHandler.
	 */
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		logger.info(WebSocketConfig.class.getName() + ".registerWebSocketHandlers called");
		registry.addHandler(getChatWebSocketHandler(), "/chat").withSockJS();
	}
	
	/**
	 * Each web socket gets a ChatWebSocketHandler
	 * 
	 * @return
	 */
	@Bean
	public WebSocketHandler getChatWebSocketHandler() {
		logger.info(WebSocketConfig.class.getName() + ".getChatWebSocketHandler called");
		return new PerConnectionWebSocketHandler(ChatWebSocketHandler.class);
	}
	
}
