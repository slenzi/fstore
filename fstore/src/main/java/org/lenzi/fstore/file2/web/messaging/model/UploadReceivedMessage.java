package org.lenzi.fstore.file2.web.messaging.model;

import java.io.Serializable;

import org.springframework.stereotype.Component;

@Component
public class UploadReceivedMessage extends AbstractMessage implements Serializable {

	private static final long serialVersionUID = 5106792655395102404L;

	private String fileName = null;
	
	public UploadReceivedMessage() {
		setMessage("Upload has been received on the server.");
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}	
	
}
