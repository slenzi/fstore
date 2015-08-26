package org.lenzi.fstore.chat.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Manages sockets for all connected users, the broadcasting/relaying of
 * messages, as well as user connection and disconnection events.
 * 
 * @author sal
 *
 */
@Service
public class ChatService {

	@InjectLogger
	private Logger logger;

	private Set<WebSocketSession> conns = java.util.Collections.synchronizedSet(new HashSet<WebSocketSession>());
	private Map<WebSocketSession, String> nickNames = new ConcurrentHashMap<WebSocketSession, String>();

	public void registerOpenConnection(WebSocketSession session) {
		conns.add(session);
	}

	public void registerCloseConnection(WebSocketSession session) {
		String nick = nickNames.get(session);
		conns.remove(session);
		nickNames.remove(session);
		if (nick != null) {
			String messageToSend = "{\"removeUser\":\"" + nick + "\"}";
			for (WebSocketSession sock : conns) {
				try {
					sock.sendMessage(new TextMessage(messageToSend));
				} catch (IOException e) {
					logger.error("IO exception when sending remove user message. " + e.getMessage(), e);
				}
			}
		}
	}

	public void processMessage(WebSocketSession session, String message) {
		if (!nickNames.containsKey(session)) {
			// No nickname has been assigned by now
			// the first message is the nickname
			// escape the " character first
			message = message.replace("\"", "\\\"");

			// broadcast all the nicknames to him
			for (String nick : nickNames.values()) {
				try {
					session.sendMessage(new TextMessage("{\"addUser\":\"" + nick + "\"}"));
				} catch (IOException e) {
					logger.error("Error when sending addUser message. " + e.getMessage(), e);
				}
			}

			// Register the nickname with the
			nickNames.put(session, message);

			// broadcast him to everyone now
			String messageToSend = "{\"addUser\":\"" + message + "\"}";
			for (WebSocketSession sock : conns) {
				try {
					sock.sendMessage(new TextMessage(messageToSend));
				} catch (IOException e) {
					logger.error("Error when sending broadcast addUser message. " + e.getMessage(), e);
				}
			}
		} else {
			// Broadcast the message
			String messageToSend = "{\"nickname\":\"" + nickNames.get(session) + "\", \"message\":\"" + message.replace("\"", "\\\"") + "\"}";
			for (WebSocketSession sock : conns) {
				try {
					sock.sendMessage(new TextMessage(messageToSend));
				} catch (IOException e) {
					logger.error("Error when sending message. " + e.getMessage(), e);
				}
			}
		}
	}

}
