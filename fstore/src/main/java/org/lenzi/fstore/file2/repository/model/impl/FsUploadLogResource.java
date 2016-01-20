/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Entry for single resource for upload log.
 * 
 * @author sal
 */
@Entity
@Table(name="FS_UPLOAD_LOG_RESOURCE")
@SequenceGenerator(name="FS_UPLD_LOG_RES_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_UPLD_LOG_RES_ID_SEQUENCE", allocationSize = 1)
public class FsUploadLogResource implements Serializable {

	private static final long serialVersionUID = 7899687111687000342L;

	// unique upload resource id
	@Id
	@GeneratedValue(generator="FS_UPLD_LOG_RES_ID_SEQUENCE_GENERATOR")
	@Column(name = "UPLD_RESOURCE_ID", unique=true, nullable = false)	
	private Long uploadResourceId;	
	
	// name of resource (file name)
	@Column(name = "UPLD_RESOURCE_NAME", nullable = false)
	private String resourceName;
	
	// the parent upload log entry 
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinTable(name = "FS_UPLOAD_LOG_RESOURCE_LINK",
		joinColumns = { 
			@JoinColumn(name = "UPLD_RESOURCE_ID", nullable = false, updatable = false)
		}, 
		inverseJoinColumns = {
			@JoinColumn(name = "UPLD_ID", nullable = false, updatable = false)
		}
	) 	
	private FsUploadLog uploadLog;
	
	/**
	 * 
	 */
	public FsUploadLogResource() {
	
	}

	/**
	 * @return the uploadResourceId
	 */
	public Long getUploadResourceId() {
		return uploadResourceId;
	}

	/**
	 * @param uploadResourceId the uploadResourceId to set
	 */
	public void setUploadResourceId(Long uploadResourceId) {
		this.uploadResourceId = uploadResourceId;
	}

	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @param resourceName the resourceName to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @return the uploadLog
	 */
	public FsUploadLog getUploadLog() {
		return uploadLog;
	}

	/**
	 * @param uploadLog the uploadLog to set
	 */
	public void setUploadLog(FsUploadLog uploadLog) {
		this.uploadLog = uploadLog;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uploadResourceId == null) ? 0 : uploadResourceId.hashCode());
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
		FsUploadLogResource other = (FsUploadLogResource) obj;
		if (uploadResourceId == null) {
			if (other.uploadResourceId != null)
				return false;
		} else if (!uploadResourceId.equals(other.uploadResourceId))
			return false;
		return true;
	}

}
