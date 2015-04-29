package org.lenzi.fstore.repository.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Tree interface.
 * 
 * A tree simply contains a reference to its root node, plus some other meta data (name, description, dates, etc.)
 * 
 * @author slenzi
 */
public interface DBTree extends Serializable {

	public Long getTreeId();
	
	public void setTreeId(Long treeId);
	
	public Long getRootNodeId();
	
	public void setRootNodeId(Long rootNodeId);
	
	public String getName();
	
	public void setName(String name);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public Timestamp getDateCreated();
	
	public void setDateCreated(Timestamp dateCreated);
	
	public Timestamp getDateUpdated();
	
	public void setDateUpdated(Timestamp dateUpdated);
	
	public DBNode getRootNode();
	
	public void setRootNode(DBNode rootNode);
	
}
