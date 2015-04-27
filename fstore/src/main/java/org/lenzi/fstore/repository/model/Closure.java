package org.lenzi.fstore.repository.model;

import java.io.Serializable;

/**
 * Closure interface
 * 
 * Models parent and child node relationships for database trees using a closure table.
 * 
 * @author slenzi
 */
public interface Closure extends Serializable {

	public Long getLinkId();
	
	public void setLinkId(Long linkId);
	
	public Long getParentNodeId();
	
	public void setParentNodeId(Long parentNodeId);
	
	public Long getChildNodeId();
	
	public void setChildNodeId(Long childNodeId);
	
	public Integer getDepth();
	
	public void setDepth(Integer depth);
	
	public Node getParentNode();
	
	public void setParentNode(Node parentNode);
	
	public Node getChildNode();
	
	public void setChildNode(Node child);
	
}
