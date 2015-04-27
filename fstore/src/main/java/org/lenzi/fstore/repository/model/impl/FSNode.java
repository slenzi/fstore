package org.lenzi.fstore.repository.model.impl;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.InheritanceType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.repository.model.Closure;
import org.lenzi.fstore.repository.model.Node;

/**
 * Database entity for FS_NODE.
 * 
 * FS_NODE contains master list of all nodes.
 * 
 * @author slenzi
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="NODE_TYPE", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("BaseNode")
@Table(name = "FS_NODE")
public abstract class FSNode implements Node {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = -13836178065227788L;

	@Id
	@Column(name = "NODE_ID", updatable = false, nullable = false)
	private Long nodeId;
	
	@Column(name = "PARENT_NODE_ID", nullable = false)
	private Long parentNodeId;
	
	/*
	@Column(name = "NODE_TYPE", nullable = false)
	private String nodeType;
	*/
	
	@Column(name = "NAME", nullable = false)
	private String name;
	
	@Column(name = "CREATION_DATE", nullable = false)
	private Timestamp dateCreated;
	
	@Column(name = "UPDATED_DATE", nullable = false)
	private Timestamp dateUpdated;
	
	// Closure entries that give access to the child nodes for this node.
	@OneToMany(mappedBy="parentNode", targetEntity = FSClosure.class)
	private Set<Closure> childClosure = new HashSet<Closure>(0);
	
	// Closure entries that give access to the parent nodes for this node.
	@OneToMany(mappedBy="childNode", targetEntity = FSClosure.class)
	private Set<Closure> parentClosure = new HashSet<Closure>(0);
	
	public FSNode() {
		
	}

	/**
	 * @param nodeId
	 * @param parentNodeId
	 * @param name
	 * @param dateCreated
	 * @param dateUpdated
	 */
	public FSNode(Long nodeId, Long parentNodeId, String name, Timestamp dateCreated, Timestamp dateUpdated) {
		
		super();
		
		this.nodeId = nodeId;
		this.parentNodeId = parentNodeId;
		this.name = name;
		//this.nodeType = nodeType;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
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

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.model.Node#getNodeType()
	 */
	@Override
	public String getNodeType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.model.Node#setNodeType(java.lang.String)
	 */
	@Override
	public void setNodeType(String nodeType) {
		// TODO Auto-generated method stub
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
	 * @return the childClosure
	 */
	public Set<Closure> getChildClosure() {
		return childClosure;
	}

	/**
	 * @param childClosure the childClosure to set
	 */
	public void setChildClosure(Set<Closure> childClosure) {
		this.childClosure = childClosure;
	}

	/**
	 * @return the parentClosure
	 */
	public Set<Closure> getParentClosure() {
		return parentClosure;
	}

	/**
	 * @param parentClosure the parentClosure to set
	 */
	public void setParentClosure(Set<Closure> parentClosure) {
		this.parentClosure = parentClosure;
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
		FSNode other = (FSNode) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

}
