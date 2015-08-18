/**
 * 
 */
package org.lenzi.fstore.file2.web.messaging.model;

/**
 * @author sal
 *
 */
public abstract class AbstractUploadMessage extends AbstractMessage {

	private static final long serialVersionUID = -2861384559039476249L;

	private UploadMessageType type = null;
	
	private Long fileId = 0L;
	private Long dirId = 0L;
	private String fileName = null;
	private String directoryName = null;
	
	/**
	 * 
	 */
	public AbstractUploadMessage() {

	}
	
	/**
	 * 
	 * @param type
	 */
	public AbstractUploadMessage(UploadMessageType type) {
		this.type = type;
		setCommand(type.getValue());
		setMessage(type.getMessage());
	}	

	/**
	 * @return the type
	 */
	public UploadMessageType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(UploadMessageType type) {
		this.type = type;
		setCommand(type.getValue());
		setMessage(type.getMessage());		
	}

	/**
	 * @return the dirId
	 */
	public Long getDirId() {
		return dirId;
	}

	/**
	 * @param dirId the dirId to set
	 */
	public void setDirId(Long dirId) {
		this.dirId = dirId;
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

	/**
	 * @return the directoryName
	 */
	public String getDirectoryName() {
		return directoryName;
	}

	/**
	 * @param directoryName the directoryName to set
	 */
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}


	
}
