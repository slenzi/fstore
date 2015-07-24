package org.lenzi.fstore.file2.web.messaging.controller;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.web.messaging.model.HelloMessage;
import org.lenzi.fstore.file2.web.messaging.model.ReplyMessage;
import org.slf4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Messaging controller which echos all incoming messages. 
 * 
 * @author sal
 */
@Controller
@RequestMapping("/")
public class EchoController {

    @InjectLogger
    private Logger logger;	
	
	public EchoController() {
	
	}
	
	/**
	 * Process incoming STOMP endpoint messages mapped to /hello and output
	 * replies to our broker mapped at /simplebroker
	 * 
	 * @param message
	 * @return
	 */
    @MessageMapping("/hello")
	@SendTo("/simplebroker/replies")
	public ReplyMessage processMessage(HelloMessage message) throws Exception {
		
    	logger.debug(EchoController.class.getName() + ".processMessage(...) called.");
    	logger.debug("message = " + message);
    	
    	return new ReplyMessage("Reply From Server. Original message = " + message.getMessage());
		
	}

}
