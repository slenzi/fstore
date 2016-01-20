/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Upload log entry meta data. See FsUploadLogResource for file specific data that's logged.
 * 
 * @author sal
 */
@Entity
@Table(name="FS_UPLOAD_LOG")
@SequenceGenerator(name="FS_UPLD_LOG_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_UPLD_LOG_ID_SEQUENCE", allocationSize = 1)
public class FsUploadLog implements Comparable<FsUploadLog>, Serializable {

	private static final long serialVersionUID = -7048154243887063383L;

	// unique upload id
	@Id
	@GeneratedValue(generator="FS_UPLD_LOG_ID_SEQUENCE_GENERATOR")
	@Column(name = "UPLD_ID", unique=true, nullable = false)	
	private Long uploadId;
	
	// upload date
	@Column(name = "UPLD_DATE", nullable = false)
	private Timestamp dateUploaded;
	
	// path to temporary upload directory
	@Column(name = "UPLD_TEMP_PATH", nullable = false)
	private String tempUploadPath;
	
	// id of user who uploaded
	@Column(name = "UPLD_USER_ID", nullable = false)
	private Long userId;
	
	// id of directory path resource where uploaded files will go
	@Column(name = "UPLD_NODE_ID", nullable = false)
	private Long nodeId;
	
	// all resurces for this upload
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "uploadLog", cascade=CascadeType.ALL)  
    private Set<FsUploadLogResource> resources = new HashSet<FsUploadLogResource>(0);
	
	/**
	 * 
	 */
	public FsUploadLog() {

	}

	/**
	 * @return the uploadId
	 */
	public Long getUploadId() {
		return uploadId;
	}

	/**
	 * @param uploadId the uploadId to set
	 */
	public void setUploadId(Long uploadId) {
		this.uploadId = uploadId;
	}

	/**
	 * @return the dateUploaded
	 */
	public Timestamp getDateUploaded() {
		return dateUploaded;
	}

	/**
	 * @param dateUploaded the dateUploaded to set
	 */
	public void setDateUploaded(Timestamp dateUploaded) {
		this.dateUploaded = dateUploaded;
	}

	/**
	 * @return the tempUploadPath
	 */
	public String getTempUploadPath() {
		return tempUploadPath;
	}

	/**
	 * @param tempUploadPath the tempUploadPath to set
	 */
	public void setTempUploadPath(String tempUploadPath) {
		this.tempUploadPath = tempUploadPath;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the nodeId
	 */
	public Long getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the resources
	 */
	public Set<FsUploadLogResource> getResources() {
		return resources;
	}

	/**
	 * @param resources the resources to set
	 */
	public void setResources(Set<FsUploadLogResource> resources) {
		this.resources = resources;
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
		FsUploadLog other = (FsUploadLog) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

	/**
	 * Compare on date uploaded
	 * 
	 * @param log
	 * @return
	 */
	@Override
	public int compareTo(FsUploadLog log) {
		if(log == null){
			return 1;
		}
		return this.dateUploaded.compareTo(log.getDateUploaded());
	}

}
