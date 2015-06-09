package org.lenzi.fstore.file2.repository.model.impl;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity for storing binary data
 * 
 * @author sal
 */
@Entity
@Table(name="FS_FILE_RESOURCE")
public class FsFileResource implements Serializable  {

	@Transient
	private static final long serialVersionUID = -6133759070827743119L;
	
	@Id
	@Column(name = "NODE_ID", updatable = false, nullable = false)
	private Long nodeId = 0L;
	
	@Lob
	@Column(name = "FILE_DATA", nullable = false)
	private byte[] fileData;
	
	@OneToOne(mappedBy = "fileResource")
	private FsFileMetaResource fileMetaResource;

	public FsFileResource() {

	}
	
	public FsFileResource(Long nodeId){
		this.nodeId = nodeId;
	}

	/**
	 * @return the fileId
	 */
	public Long getFileId() {
		return nodeId;
	}

	/**
	 * @param fileId the fileId to set
	 */
	public void setFileId(Long nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the fileData
	 */
	public byte[] getFileData() {
		return fileData;
	}

	/**
	 * @param fileData the fileData to set
	 */
	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	/**
	 * @return the fileMetaResource
	 */
	public FsFileMetaResource getFileMetaResource() {
		return fileMetaResource;
	}

	/**
	 * @param fileMetaResource the fileMetaResource to set
	 */
	public void setFileMetaResource(FsFileMetaResource fileMetaResource) {
		this.fileMetaResource = fileMetaResource;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FsFileResource other = (FsFileResource) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

}
