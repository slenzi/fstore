package org.lenzi.fstore.core.repository.tree.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.core.repository.tree.model.DBClosure;

/**
 * Database entity for FS_CLOSURE. 
 * 
 * FS_CLOSURE table maintains parent-child relationship for all nodes.
 * 
 * @author sal
 */
@Entity
@Table(name = "FS_CLOSURE")
public class FSClosure<N extends FSNode<N>> implements DBClosure<N> {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 6104278113268285089L;

	@Id
	@Column(name = "LINK_ID", updatable = false, nullable = false)
	private Long linkId;
	
	@Column(name = "PARENT_NODE_ID", nullable = false)
	private Long parentNodeId;
	
	@Column(name = "CHILD_NODE_ID", nullable = false)
	private Long childNodeId;
	
	@Column(name = "DEPTH", nullable = false)
	private Integer depth = 0;
	
	@ManyToOne(targetEntity = FSNode.class)
	@JoinColumn(name="PARENT_NODE_ID", referencedColumnName="NODE_ID", insertable=false, updatable=false)	
	private N parentNode;
	
	@ManyToOne(targetEntity = FSNode.class)
	@JoinColumn(name="CHILD_NODE_ID", referencedColumnName="NODE_ID", insertable=false, updatable=false)		
	private N childNode;
	
	
	public FSClosure() {

	}

	/**
	 * @return the linkId
	 */
	public Long getLinkId() {
		return linkId;
	}

	/**
	 * @param linkId the linkId to set
	 */
	public void setLinkId(Long linkId) {
		this.linkId = linkId;
	}

	/**
	 * @return the depth
	 */
	public Integer getDepth() {
		return depth;
	}

	/**
	 * @param depth the depth to set
	 */
	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	/**
	 * @return the parentNode
	 */
	public N getParentNode() {
		return parentNode;
	}

	/**
	 * @param parentNode the parentNode to set
	 */
	public void setParentNode(N parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * @return the childNode
	 */
	public N getChildNode() {
		return childNode;
	}

	/**
	 * @param childNode the childNode to set
	 */
	public void setChildNode(N childNode) {
		this.childNode = childNode;
	}

	/**
	 * @return the parentNodeId
	 */
	public Long getParentNodeId() {
		return parentNodeId;
	}

	/**
	 * @param parentNodeId the parentNodeId to set
	 */
	public void setParentNodeId(Long parentNodeId) {
		this.parentNodeId = parentNodeId;
	}

	/**
	 * @return the childNodeId
	 */
	public Long getChildNodeId() {
		return childNodeId;
	}

	/**
	 * @param childNodeId the childNodeId to set
	 */
	public void setChildNodeId(Long childNodeId) {
		this.childNodeId = childNodeId;
	}

	public boolean hasChild(){
		return ((childNode != null) ? true : false);
	}
	
	public boolean hasParent(){
		return ((parentNode != null) ? true : false);
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linkId == null) ? 0 : linkId.hashCode());
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
		FSClosure<N> other = (FSClosure<N>) obj;
		if (linkId == null) {
			if (other.linkId != null)
				return false;
		} else if (!linkId.equals(other.linkId))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return "FSClosure [linkId=" + linkId + ", parentNodeId=" + parentNodeId
				+ ", childNodeId=" + childNodeId + ", depth=" + depth
				+ ", parentNode=" + ((parentNode != null) ? parentNode.getName() : "")
				+ ", childNode=" + ((childNode != null) ? childNode.getName() : "")
				+ "]";
	}
	
	

}
