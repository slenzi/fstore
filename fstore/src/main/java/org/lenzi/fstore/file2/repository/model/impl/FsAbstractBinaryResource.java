package org.lenzi.fstore.file2.repository.model.impl;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Base class for entities that store binary data
 * 
 * @author sal
 */
@MappedSuperclass
public abstract class FsAbstractBinaryResource implements Serializable {

	@Transient
	private static final long serialVersionUID = -3426534469354756741L;

	// id for the entity
	@Id
	@Column(name = "NODE_ID", updatable = false, nullable = false)
	private Long nodeId = 0L;
	
	// the file meta resource for this binary entity
	@OneToOne(mappedBy = "fileResource")
	private FsFileMetaResource fileMetaResource;
	
	public FsAbstractBinaryResource() {
		
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
		FsAbstractBinaryResource other = (FsAbstractBinaryResource) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

}
