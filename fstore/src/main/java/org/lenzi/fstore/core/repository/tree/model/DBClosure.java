package org.lenzi.fstore.core.repository.tree.model;

import java.io.Serializable;

/**
 * Closure interface
 * 
 * Models parent and child node relationships for database trees using a closure table.
 * 
 * @author slenzi
 */
public interface DBClosure<N extends DBNode<N>> extends Serializable {

	public Long getLinkId();
	
	public void setLinkId(Long linkId);
	
	public Long getParentNodeId();
	
	public void setParentNodeId(Long parentNodeId);
	
	public Long getChildNodeId();
	
	public void setChildNodeId(Long childNodeId);
	
	public Integer getDepth();
	
	public void setDepth(Integer depth);
	
	public N getParentNode();
	
	public void setParentNode(N parentNode);
	
	public N getChildNode();
	
	public void setChildNode(N child);
	
	public boolean hasChild();
	
	public boolean hasParent();
	
}
