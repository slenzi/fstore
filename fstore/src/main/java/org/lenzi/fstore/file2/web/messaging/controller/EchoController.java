package org.lenzi.fstore.file2.web.messaging.controller;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.web.messaging.model.HelloMessage;
import org.lenzi.fstore.file2.web.messaging.model.ReplyMessage;
import org.slf4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
	
	@RequestMapping(value = "/file2/echo", method = RequestMethod.GET)
	public void test(){
		
		logger.info("You have successfully triggered the echo controller.");
		
	}
	
	/**
	 * Process incoming STOMP endpoint messages mapped to /hello and output
	 * replies to our broker mapped at /topic
	 * 
	 * @param message
	 * @return
	 */
    @MessageMapping("/hello")
	@SendTo("/topic/echos")
	public ReplyMessage processMessage(HelloMessage message) throws Exception {
		
    	logger.info(EchoController.class.getName() + ".processMessage(...) called.");
    	logger.info("message = " + message);
    	
    	return new ReplyMessage("Reply From Server. Original message = " + message.getMessage());
		
	}

}
