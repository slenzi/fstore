package org.lenzi.fstore.file2.web.messaging;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.web.messaging.model.ReplyMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Sample messaging service. Currently disabled (uncomment @PostConstruct and @PreDestroy to re-enable)
 * 
 * @author sal
 */
@Service
public class TestMessageService {

	@InjectLogger
	private Logger logger;	
	
    @Autowired
    private SimpMessagingTemplate template;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();	
	
	public TestMessageService() {

	}

	//@PostConstruct
	private void init(){
		
		executor.submit(() -> {
			
			while(true){
				
				//logger.info("Sending message...");
				
				LocalDate nowDate = LocalDate.now();
				LocalTime nowTime = LocalTime.now();
				
				template.convertAndSend("/topic/tests", 
						new ReplyMessage("This is a message! " + nowDate + " " +nowTime));
				
				//logger.info("Message sent. Sleeping....");
				
				TimeUnit.SECONDS.sleep(10);
				
			}
			
		});		
		
	}
	
	//@PreDestroy
	private void cleanup(){
		
		executor.shutdownNow();
		
	}
	
}
