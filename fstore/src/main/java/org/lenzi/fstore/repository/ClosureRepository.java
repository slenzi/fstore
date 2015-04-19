package org.lenzi.fstore.repository;

import java.util.List;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.FSClosure;
import org.lenzi.fstore.repository.model.FSNode;
import org.lenzi.fstore.repository.model.FSTree;

public interface ClosureRepository {
	
	public String getRepositoryName();

	public FSNode addNode(Long parentNodeId, String nodeName) throws DatabaseException;
	
	public FSTree addTree(String treeName, String treeDesc, String rootNodeName) throws DatabaseException;
	
	public List<FSClosure> getClosureByNodeId(Long nodeId) throws DatabaseException;
	
	public void moveNode(Long nodeId, Long newParentNodeId) throws DatabaseException;
	
	public void removeNode(Long nodeId) throws DatabaseException;
	
	public void removeChildren(Long nodeId) throws DatabaseException;
	
}
