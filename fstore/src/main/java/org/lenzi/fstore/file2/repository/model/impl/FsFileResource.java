package org.lenzi.fstore.file2.repository.model.impl;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity for storing binary data.
 * 
 * @author sal
 */
@Entity
@Table(name="FS_FILE_RESOURCE")
public class FsFileResource extends FsAbstractBinaryResource implements Serializable  {

	@Transient
	private static final long serialVersionUID = -6133759070827743119L;
	
	@Lob
	@Column(name = "FILE_DATA", nullable = false)
	private byte[] fileData;

	public FsFileResource() {

	}
	
	public FsFileResource(Long fileId){
		setNodeId(fileId);
	}

	public void setFileId(Long fileId) {
		setNodeId(fileId);
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

}
