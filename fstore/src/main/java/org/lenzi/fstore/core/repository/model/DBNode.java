package org.lenzi.fstore.core.repository.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

/**
 * Node interface
 * 
 * A node which can be used to model a tree structure.
 * 
 * @author slenzi
 */
public interface DBNode<N extends DBNode<N>> extends Serializable {

	public Long getNodeId();
	
	public void setNodeId(Long nodeId);
	
	public Long getParentNodeId();
	
	public void setParentNodeId(Long parentNodeId);
	
	public String getNodeType();
	
	public void setNodeType(String nodeType);
	
	public Set<DBClosure<N>> getChildClosure();
	
	public void setChildClosure(Set<DBClosure<N>> childClosure);
	
	public Set<DBClosure<N>> getParentClosure();
	
	public void setParentClosure(Set<DBClosure<N>> parentClosure);
	
	public boolean isRootNode();
	
	
	// extras - remove?
	
	public String getName();
	
	public void setName(String name);
	
	public Timestamp getDateCreated();
	
	public void setDateCreated(Timestamp dateCreated);
	
	public Timestamp getDateUpdated();
	
	public void setDateUpdated(Timestamp dateUpdated);
	
}
