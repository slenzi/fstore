package org.lenzi.fstore.file2.web.messaging;

import java.time.LocalDate;
import java.time.LocalTime;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.web.messaging.model.UploadProcessedMessage;
import org.lenzi.fstore.file2.web.messaging.model.UploadReceivedMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Messaging service to notify clients that uploads have been received and processed.
 * 
 * @author sal
 */
@Service
public class UploadMessageService {

	@InjectLogger
	private Logger logger;	
	
    @Autowired
    private SimpMessagingTemplate template;
    
    private final String destination = "/topic/uploads";
	
	public UploadMessageService() {
		
	}
	
	/**
	 * Send 'upload received' message to clients.
	 * 
	 * @param fileName
	 */
	public void sendUploadReceivedMessage(String fileName){
		
		LocalDate nowDate = LocalDate.now();
		LocalTime nowTime = LocalTime.now();
		
		UploadReceivedMessage message = new UploadReceivedMessage();
		message.setFileName(fileName);
		message.setDate(nowDate);
		message.setTime(nowTime);
		
		template.convertAndSend(destination, message);
		
	}
	
	/**
	 * Send 'upload processed' message to clients.
	 * 
	 * @param fileId
	 * @param fileName
	 */
	public void sendUploadProcessedMessage(Long fileId, String fileName){
		
		LocalDate nowDate = LocalDate.now();
		LocalTime nowTime = LocalTime.now();
		
		UploadProcessedMessage message = new UploadProcessedMessage();
		message.setFileId(fileId);
		message.setFileName(fileName);
		message.setDate(nowDate);
		message.setTime(nowTime);
		
		template.convertAndSend(destination, message);
		
	}	

}
