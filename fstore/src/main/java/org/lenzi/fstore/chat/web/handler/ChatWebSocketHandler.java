package org.lenzi.fstore.chat.web.handler;

import org.lenzi.fstore.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Processes connections and messages for our web sockets.
 * 
 * @author slenzi
 *
 */
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
	
	@Autowired
	private ChatService chatService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("New connection established");
		chatService.registerOpenConnection(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		logger.info("Connection closed");
		chatService.registerCloseConnection(session);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		logger.info("Transport error: " + exception.getMessage());
		chatService.registerCloseConnection(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		logger.info("New message: " + message.getPayload());
		chatService.processMessage(session, message.getPayload());
	}

}
