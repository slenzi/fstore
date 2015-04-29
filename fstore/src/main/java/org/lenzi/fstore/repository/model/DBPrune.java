package org.lenzi.fstore.repository.model;

import java.io.Serializable;

/**
 * Prune interface
 * 
 * Used for tracking prune (delete) operations on database trees.
 * 
 * @author slenzi
 */
public interface DBPrune extends Serializable {

	public Long getPruneId();
	
	public void setPruneId(Long pruneId);
	
	public Long getNodeId();
	
	public void setNodeId(Long nodeId);
	
}
