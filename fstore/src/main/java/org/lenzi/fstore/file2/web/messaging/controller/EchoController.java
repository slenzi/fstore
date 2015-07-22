package org.lenzi.fstore.file2.web.messaging.controller;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Messaging controller which echos all incoming messages. 
 * 
 * @author sal
 */
@Controller
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
	public String processMessage(String message) {
		
    	logger.debug(EchoController.class.getName() + ".processMessage(...) called.");
    	logger.debug("message = " + message);
    	
		return "Hello from server. Your message = " + message;
		
	}

}
