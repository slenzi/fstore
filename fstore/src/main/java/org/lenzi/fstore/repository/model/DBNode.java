package org.lenzi.fstore.repository.model;

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
public interface DBNode extends Serializable {

	public Long getNodeId();
	
	public void setNodeId(Long nodeId);
	
	public Long getParentNodeId();
	
	public void setParentNodeId(Long parentNodeId);
	
	public String getNodeType();
	
	public void setNodeType(String nodeType);
	
	public Set<DBClosure> getChildClosure();
	
	public void setChildClosure(Set<DBClosure> childClosure);
	
	public Set<DBClosure> getParentClosure();
	
	public void setParentClosure(Set<DBClosure> parentClosure);
	
	public boolean isRootNode();
	
	
	// extras - remove?
	
	public String getName();
	
	public void setName(String name);
	
	public Timestamp getDateCreated();
	
	public void setDateCreated(Timestamp dateCreated);
	
	public Timestamp getDateUpdated();
	
	public void setDateUpdated(Timestamp dateUpdated);
	
}
