package org.lenzi.fstore.repository.model.impl;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.repository.model.Prune;

/**
 * Database entity for FS_PRUNE.
 * 
 * FS_PRUNE table is used during delete operations.
 * 
 * @author sal
 */
@Entity
@Table(name = "FS_PRUNE")
public class FSPrune implements Prune {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 1105023514399757780L;

	// @GeneratedValue(generator="FS_PRUNE_ID_SEQUENCE")
	@Id
	@Column(name = "PRUNE_ID", updatable = false, nullable = false)
	private Long pruneId;
	
	@Column(name = "NODE_ID", nullable = false)
	private Long nodeId;
	
	public FSPrune() {

	}

	/**
	 * @param pruneId
	 * @param nodeId
	 */
	public FSPrune(Long pruneId, Long nodeId) {
		super();
		this.pruneId = pruneId;
		this.nodeId = nodeId;
	}

	/**
	 * @return the pruneId
	 */
	public Long getPruneId() {
		return pruneId;
	}

	/**
	 * @param pruneId the pruneId to set
	 */
	public void setPruneId(Long pruneId) {
		this.pruneId = pruneId;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pruneId == null) ? 0 : pruneId.hashCode());
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
		FSPrune other = (FSPrune) obj;
		if (pruneId == null) {
			if (other.pruneId != null)
				return false;
		} else if (!pruneId.equals(other.pruneId))
			return false;
		return true;
	}

}
