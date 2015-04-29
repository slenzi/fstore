package org.lenzi.fstore.repository.model;

import java.io.Serializable;

/**
 * Closure interface
 * 
 * Models parent and child node relationships for database trees using a closure table.
 * 
 * @author slenzi
 */
public interface DBClosure extends Serializable {

	public Long getLinkId();
	
	public void setLinkId(Long linkId);
	
	public Long getParentNodeId();
	
	public void setParentNodeId(Long parentNodeId);
	
	public Long getChildNodeId();
	
	public void setChildNodeId(Long childNodeId);
	
	public Integer getDepth();
	
	public void setDepth(Integer depth);
	
	public DBNode getParentNode();
	
	public void setParentNode(DBNode parentNode);
	
	public DBNode getChildNode();
	
	public void setChildNode(DBNode child);
	
	public boolean hasChild();
	
	public boolean hasParent();
	
}
