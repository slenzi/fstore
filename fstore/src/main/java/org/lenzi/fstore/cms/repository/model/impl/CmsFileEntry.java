/**
 * 
 */
package org.lenzi.fstore.cms.repository.model.impl;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * @author sal
 *
 */
@Entity
@Table(name="FS_CMS_FILE_ENTRY")
public class CmsFileEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2945275457068233067L;
	
	@Id
	@Column(name = "FILE_ID", updatable = false, nullable = false)
	private Long fileId = 0L;	
	
	@Column(name = "FILE_NAME", nullable = false)
	private String fileName;
	
	// files belong to one directory
	@ManyToOne
	@JoinTable(
	    name="FS_CMS_DIR_FILE_LINK",
	    joinColumns = @JoinColumn( name="FILE_ID"),
	    inverseJoinColumns = @JoinColumn( name="NODE_ID")
    )
	private CmsDirectory directory;
	
	// the binary data for the file
	@OneToOne(cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	private CmsFile file;

	/**
	 * 
	 */
	public CmsFileEntry() {
		
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
	 * @return the file
	 */
	public CmsFile getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(CmsFile file) {
		this.file = file;
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
		CmsFileEntry other = (CmsFileEntry) obj;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		return true;
	}

}
