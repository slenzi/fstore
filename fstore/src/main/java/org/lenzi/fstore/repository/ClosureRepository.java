package org.lenzi.fstore.repository;

import java.util.List;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.FSClosure;
import org.lenzi.fstore.repository.model.FSNode;
import org.lenzi.fstore.repository.model.FSTree;

public interface ClosureRepository {
	
	/**
	 * Get name of respository.
	 * 
	 * @return
	 */
	public String getRepositoryName();

	/**
	 * Add a new child node.
	 * 
	 * @param parentNodeId - The ID of the parent node.
	 * @param nodeName - The name of the new node.
	 * @return
	 * @throws DatabaseException
	 */
	public FSNode addNode(Long parentNodeId, String nodeName) throws DatabaseException;
	
	/**
	 * Get a node, not closure data, or children. Just the node data.
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public FSNode getNode(Long nodeId) throws DatabaseException;
	
	/**
	 * Get a tree with it's root node.
	 * 
	 * @param treeId - The ID of the tree.
	 * @return
	 * @throws DatabaseException
	 */
	public FSTree getTree(Long treeId) throws DatabaseException;
	
	/**
	 * Add a new tree.
	 * 
	 * @param treeName - Name of the tree.
	 * @param treeDesc - Description for the tree.
	 * @param rootNodeName - Name of the root node.
	 * @return
	 * @throws DatabaseException
	 */
	public FSTree addTree(String treeName, String treeDesc, String rootNodeName) throws DatabaseException;
	
	/**
	 * Create a new tree by taking a non-root node of an existing tree and making it the root node of the new tree.
	 * All children of the existing node are also moved over.
	 * 
	 * @param treeName - name of new tree
	 * @param treeDesc - description of new tree
	 * @param existingNode - a non-root node of an existing tree which will become the root node of 
	 * 	the new tree. All child nodes will be moved over as well.
	 * @return
	 * @throws DatabaseException
	 */
	public FSTree addTree(String treeName, String treeDesc, FSNode existingNode) throws DatabaseException;
	
	/**
	 * Add a new tree, and copy over all the nodes from another tree.
	 * 
	 * @param treeName - name for the new tree
	 * @param treeDesc - description for the new tree
	 * @param rootNodeName - optional name of the root node for the new tree. If null, the root node
	 * of the new tree will have the same name as the root node of the tree being copied. If your tree
	 * is modeling a directory structure you might want the root node to have a different name (i.e path).
	 * @param treeToCopy
	 * @return
	 * @throws DatabaseException
	 */
	//public FSTree addTree(String treeName, String treeDesc, String rootNodeName, FSTree treeToCopy) throws DatabaseException;
	
	/**
	 * Copy a the node, and optionally all it's children.
	 * 
	 * @param nodeId - the id of the node to copy.
	 * @param parentNodeId - the parent node where all the new copy node will be placed under.
	 * @param copyChildren - true to copy over all the nodes children nodes as well, false to just copy the node.
	 */
	public void copyNode(Long nodeId, Long parentNodeId, boolean copyChildren) throws DatabaseException;
	
	/**
	 * Copy a the node and all it's children.
	 * 
	 * @param nodeId - the id of the node to copy. this node and all it's children will be copied to the parent node.
	 * @param parentNodeId - the parent node where all the copied data will go.
	 * @param newRootNodeName - new name for the root node of the sub-tree being moved. this is optional. if you don't
	 * 	provide a name then the node will have the same name as the original node node.
	 * @throws DatabaseException
	 */
	//public void copyNode(Long nodeId, Long parentNodeId, String newRootNodeName) throws DatabaseException;
	
	/**
	 * Remove a tree
	 * 
	 * @param treeId
	 * @throws DatabaseException
	 */
	public void removeTree(Long treeId) throws DatabaseException;
	
	/**
	 * Take the root node of an tree you want to delete, and move it plus all children to under any node
	 * of some other tree. Then delete the old tree.
	 * 
	 * @param tree - The tree you want to delete (but keep all nodes under it.)
	 * @param newParentNode - The root node (plus all children) of the tree being deleted will be moved to
	 * 	under this node. This node must be a node in a different tree.
	 * @throws DatabaseException
	 */
	public void removeTree(FSTree tree, FSNode newParentNode)  throws DatabaseException;
	
	/**
	 * Get closure data for a node. This will give you all the necessary information to build a tree model.
	 * Usually you would do this for a root node of a tree.
	 * 
	 * @param nodeId - the node id, most likely the ID of a root node of a tree
	 * @return
	 * @throws DatabaseException
	 */
	public List<FSClosure> getClosureByNodeId(Long nodeId) throws DatabaseException;
	
	/**
	 * Move a node. The node, plus all its chilren, will be moved to under the new parent node.
	 * 
	 * @param nodeId - the id of the node to move
	 * @param newParentNodeId - the id of the new parent node.
	 * @throws DatabaseException
	 */
	public void moveNode(Long nodeId, Long newParentNodeId) throws DatabaseException;
	
	/**
	 * Remove a node and all its children.
	 * 
	 * @param nodeId - the id of the node to remove
	 * @throws DatabaseException
	 */
	public void removeNode(Long nodeId) throws DatabaseException;
	
	/**
	 * Remove all children of a node. The node itself is not removed
	 * 
	 * @param nodeId - the id of the node, all its children will be removed.
	 * @throws DatabaseException
	 */
	public void removeChildren(Long nodeId) throws DatabaseException;
	
	/**
	 * Check if two nodes are in the same tree. Returns true if they are, false if they are not.
	 * 
	 * @param node1 - the first node
	 * @param node2 - the second node
	 * @return true if node1 and node2 are in the same tree, false if not.
	 * @throws DatabaseException
	 */
	public boolean isSameTree(FSNode node1, FSNode node2) throws DatabaseException;
	
}
