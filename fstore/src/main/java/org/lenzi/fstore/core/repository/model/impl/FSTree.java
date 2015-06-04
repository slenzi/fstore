/**
 * 
 */
package org.lenzi.fstore.core.repository.model.impl;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.core.repository.model.DBTree;

/**
 * @author sal
 *
 */
@Entity
@Table(name = "FS_TREE")
public class FSTree<N extends FSNode<N>> implements DBTree<N> {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = -4796310839015468465L;

	@Id
	@Column(name = "TREE_ID", updatable = false, nullable = false)
	private Long treeId;
	
	@Column(name = "ROOT_NODE_ID", nullable = false)
	private Long rootNodeId;
	
	@Column(name = "NAME", nullable = false)
	private String name;
	
	@Column(name = "DESCRIPTION", nullable = true)
	private String description;
	
	@Column(name = "CREATION_DATE", nullable = false)
	private Timestamp dateCreated;
	
	@Column(name = "UPDATED_DATE", nullable = false)
	private Timestamp dateUpdated;
	
	@OneToOne(targetEntity = FSNode.class)
	@JoinColumn(name = "ROOT_NODE_ID", insertable=false, updatable=false)
	private N rootNode = null;
	
	/**
	 * 
	 */
	public FSTree() {

	}

	/**
	 * @param name
	 * @param description
	 */
	public FSTree(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}



	/**
	 * @return the treeId
	 */
	public Long getTreeId() {
		return treeId;
	}

	/**
	 * @param treeId the treeId to set
	 */
	public void setTreeId(Long treeId) {
		this.treeId = treeId;
	}

	/**
	 * @return the rootNodeId
	 */
	public Long getRootNodeId() {
		return rootNodeId;
	}

	/**
	 * @param rootNodeId the rootNodeId to set
	 */
	public void setRootNodeId(Long rootNodeId) {
		this.rootNodeId = rootNodeId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the dateCreated
	 */
	public Timestamp getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the dateUpdated
	 */
	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	/**
	 * @param dateUpdated the dateUpdated to set
	 */
	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	/**
	 * @return the rootNode
	 */
	public N getRootNode() {
		return rootNode;
	}

	/**
	 * @param rootNode the rootNode to set
	 */
	public void setRootNode(N rootNode) {
		this.rootNode = rootNode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((treeId == null) ? 0 : treeId.hashCode());
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
		FSTree<N> other = (FSTree<N>) obj;
		if (treeId == null) {
			if (other.treeId != null)
				return false;
		} else if (!treeId.equals(other.treeId))
			return false;
		return true;
	}

}
