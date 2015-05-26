/**
 * 
 */
package org.lenzi.fstore.cms.repository.model.impl;

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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author sal
 *
 */
@Entity
@Table(name="FS_CMS_FILE_ENTRY")
@SequenceGenerator(name="FS_CMS_FILE_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_CMS_FILE_ID_SEQUENCE", allocationSize = 1)
public class CmsFileEntry implements Serializable {

	@Transient
	private static final long serialVersionUID = 2945275457068233067L;
	
	@Id
	@GeneratedValue(generator="FS_CMS_FILE_ID_SEQUENCE_GENERATOR")
	@Column(name = "FILE_ID", updatable = false, nullable = false)
	private Long fileId = 0L;	
	
	@Column(name = "FILE_NAME", nullable = false)
	private String fileName = "unknown";
	
	@Column(name = "FILE_SIZE", nullable = false)
	private Long fileSize = 0L;
	
	// files belong to one directory
	@ManyToOne
	@JoinTable(
	    name="FS_CMS_DIR_FILE_LINK",
	    joinColumns = @JoinColumn( name="FILE_ID"),
	    inverseJoinColumns = @JoinColumn( name="NODE_ID")
    )
	private CmsDirectory directory = null;
	
	@OneToOne(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private CmsFile file = null;

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
	 * @return the directory
	 */
	public CmsDirectory getDirectory() {
		return directory;
	}

	/**
	 * @param directory the directory to set
	 */
	public void setDirectory(CmsDirectory directory) {
		this.directory = directory;
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

	public String toString(){
		
		StringBuffer buf = new StringBuffer();
		buf.append("id => " + getFileId());
		buf.append(", name => " + getFileName());
		buf.append(", size => " + getFileSize());
		return buf.toString();
	}
	
}
