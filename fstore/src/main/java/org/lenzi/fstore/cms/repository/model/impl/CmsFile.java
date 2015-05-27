package org.lenzi.fstore.cms.repository.model.impl;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name="FS_CMS_FILE")
public class CmsFile implements Serializable  {

	@Transient
	private static final long serialVersionUID = -6133759070827743119L;
	
	@Id
	@Column(name = "FILE_ID", updatable = false, nullable = false)
	private Long fileId = 0L;
	
	@Lob
	@Column(name = "FILE_DATA", nullable = false)
	private byte[] fileData;
	
	@OneToOne(mappedBy = "file")
	private CmsFileEntry fileEntry;

	public CmsFile() {

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
	 * @return the fileEntry
	 */
	public CmsFileEntry getFileEntry() {
		return fileEntry;
	}

	/**
	 * @param fileEntry the fileEntry to set
	 */
	public void setFileEntry(CmsFileEntry fileEntry) {
		this.fileEntry = fileEntry;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
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
		CmsFile other = (CmsFile) obj;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		return true;
	}

}
