package org.lenzi.fstore.file2.web.messaging.model;

import java.io.Serializable;

import org.springframework.stereotype.Component;

@Component
public class UploadProcessedMessage extends AbstractMessage implements Serializable {

	private static final long serialVersionUID = 5106792655395102404L;

	private Long fileId = 0L;
	private String fileName = null;
	
	public UploadProcessedMessage() {
	
	}
	
	/**
	 * @return the fileId
	 */
	public Long getFileId() {
		return fileId;
	}

	/**
	 * @param fileId the fileId to set
	 */
	public void setFileId(Long fileId) {
		this.fileId = fileId;
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
