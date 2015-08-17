/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity for file meta data, not actual byte data. 
 * 
 * @author sal
 */
@Entity
//@DiscriminatorValue("FsFileMetaResource") - not needed when using JOINED inheritance
@Table(name="FS_FILE_META_RESOURCE")
public class FsFileMetaResource extends FsPathResource {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 2220744795886332761L;	
	
	@Column(name = "FILE_SIZE", nullable = false)
	private Long fileSize = 0L;
	
	@Column(name = "MIME_TYPE", nullable = true)
	private String mimeType = null;
	
	// optional = false to ensure lazy loading of FsFileResource (which contains the binary file data)
	@OneToOne(cascade = {CascadeType.ALL}, optional = false, fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private FsFileResource fileResource = null;


	public FsFileMetaResource() {
		
		super();
		
		init(null);
		
	}

	public FsFileMetaResource(Long fileId) {
		
		super();
		
		init(fileId);		
	
	}
	
	private void init(Long fileId){
		
		setNodeId(fileId);
		setPathType(FsPathType.FILE);
		
	}
	
	public Long getFileId(){
		return getNodeId();
	}

	/**
	 * @return the fileSize
	 */
	public Long getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @return the fileResource
	 */
	public FsFileResource getFileResource() {
		return fileResource;
	}

	/**
	 * @param fileResource the fileResource to set
	 */
	public void setFileResource(FsFileResource fileResource) {
		this.fileResource = fileResource;
	}
	
}
