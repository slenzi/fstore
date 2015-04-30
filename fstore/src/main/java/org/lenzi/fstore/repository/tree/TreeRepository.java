package org.lenzi.fstore.repository.tree;

import java.util.List;

import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.DBTree;

public interface TreeRepository {
	
	/**
	 * Get name of repository.
	 * 
	 * @return
	 */
	public String getRepositoryName();

	/**
	 * Get a node, not closure data, or children. Just the node data.
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public DBNode getNode(Long nodeId) throws DatabaseException;	
	
	/**
	 * Add a new root node. Parent node ID will be set to 0.
	 * 
	 * @param newNode
	 * @return
	 * @throws DatabaseException
	 */
	public DBNode addRootNode(DBNode newNode) throws DatabaseException;
	
	/**
	 * Add a new child node under the parent node.
	 * 
	 * @param parentNodeId - The ID of the parent node.
	 * @param nodeName - The name of the new node.
	 * @return
	 * @throws DatabaseException
	 */
	public DBNode addChildNode(DBNode parentNode, DBNode newNode) throws DatabaseException;
	
	/**
	 * Copy a node.
	 * 
	 * @param nodeToCopy - The node to copy
	 * @param parentNode - The new copy will be placed under this parent node.
	 * @param copyChildren - True to copy all children of the node, false to copy just the node itself.
	 * @param copier - The copier which knows how to copy your node object.
	 * @return Reference to the copied node
	 * @throws DatabaseException
	 */
	public DBNode copyNode(DBNode nodeToCopy, DBNode parentNode, boolean copyChildren, NodeCopier copier) throws DatabaseException;
	
	
	/**
	 * Add a tree.
	 * 
	 * @param newTree
	 * @param newRootNode
	 * @return
	 * @throws DatabaseException
	 */
	public DBTree addTree(DBTree newTree, DBNode newRootNode) throws DatabaseException;
	
	/**
	 * Remove a node, plus all its children.
	 * 
	 * @param node
	 * @throws DatabaseException
	 */
	public void removeNode(DBNode node) throws DatabaseException;
	
	/**
	 * Remove all children of a node, but not the node itself.
	 * 
	 * @param node
	 * @throws DatabaseException
	 */
	public void removeChildren(DBNode node) throws DatabaseException;	
	
	/**
	 * Get closure data for a node. This will give you all the necessary information to build a tree model.
	 * Usually you would do this for a root node of a tree.
	 * 
	 * @param node - The node to fetch closure data for. Most likely you want this to be a root node of a tree.
	 * @return
	 * @throws DatabaseException
	 */
	public List<DBClosure> getClosure(DBNode node) throws DatabaseException;
	
	/**
	 * Move a node
	 * 
	 * @param nodeToMode
	 * @param newParentNode
	 * @throws DatabaseException
	 */
	public void moveNode(DBNode nodeToMode, DBNode newParentNode)  throws DatabaseException;
	
	/**
	 * Get a tree with it's root node.
	 * 
	 * @param treeId - The ID of the tree.
	 * @return
	 * @throws DatabaseException
	 */
	//public Tree getTree(Long treeId) throws DatabaseException;
	
	/**
	 * Add a new tree.
	 * 
	 * @param treeName - Name of the tree.
	 * @param treeDesc - Description for the tree.
	 * @param rootNodeName - Name of the root node.
	 * @return
	 * @throws DatabaseException
	 */
	//public Tree addTree(String treeName, String treeDesc, String rootNodeName) throws DatabaseException;
	
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
	//public Tree addTree(String treeName, String treeDesc, Node existingNode) throws DatabaseException;
	
	/**
	 * Remove a tree
	 * 
	 * @param treeId
	 * @throws DatabaseException
	 */
	//public void removeTree(Long treeId) throws DatabaseException;
	
	/**
	 * Take the root node of an tree you want to delete, and move it plus all children to under any node
	 * of some other tree. Then delete the old tree.
	 * 
	 * @param tree - The tree you want to delete (but keep all nodes under it.)
	 * @param newParentNode - The root node (plus all children) of the tree being deleted will be moved to
	 * 	under this node. This node must be a node in a different tree.
	 * @throws DatabaseException
	 */
	//public void removeTree(Tree tree, Node newParentNode)  throws DatabaseException;
	
	/**
	 * Move a node. The node, plus all its chilren, will be moved to under the new parent node.
	 * 
	 * @param nodeId - the id of the node to move
	 * @param newParentNodeId - the id of the new parent node.
	 * @throws DatabaseException
	 */
	//public void moveNode(Long nodeId, Long newParentNodeId) throws DatabaseException;
	
	/**
	 * Check if two nodes are in the same tree. Returns true if they are, false if they are not.
	 * 
	 * @param node1 - the first node
	 * @param node2 - the second node
	 * @return true if node1 and node2 are in the same tree, false if not.
	 * @throws DatabaseException
	 */
	public boolean isSameTree(DBNode node1, DBNode node2) throws DatabaseException;
	
	/**
	 * Check if node1 is a parent of node2.
	 * 
	 * If 'fullSearch' is true, a full search will be completed all the way to the root node. If
	 * node1 is found anywhere up the tree then true is returned, otherwise false.
	 * 
	 * If 'fullSearch' is false, then a simple check is performed to see if node1 is an immediate
	 * parent of node2. An immediate parent would mean node2.getParentNodeId() == node1.getNodeId().
	 * 
	 * @param node1 - The first node
	 * @param node2 - The second node
	 * @param fullSearch - Pass true to search up the tree till the root node. Pass False to simply
	 * 	check if node2.getParentNodeId() == node1.getNodeId()
	 * @return
	 * @throws DatabaseException
	 */
	public boolean isParent(DBNode node1, DBNode node2, boolean fullSearch) throws DatabaseException;
	
	/**
	 * Check if node1 is a child of node2.
	 * 
	 * If 'fullSearch' is true, a full search will be completed of all nodes under node2, until all
	 * leaf nodes have been reached. If node1 is found anywhere under node2 then true is returned,
	 * otherwise false.
	 * 
	 * If 'fullSearch' is false, then a simple check is performed to see if node1 is an immediate
	 * child of node2. An immediate child would mean node2.getNodeId() == node1.getParentNodeId().
	 * 
	 * @param node1 - The first node
	 * @param node2 - The second node
	 * @param fullSearch - Pass true to search all nodes under node2, till all leaf nodes are reached.
	 * 	Pass false to simple check if node2.getNodeId() == node1.getParentNodeId().
	 * @return
	 * @throws DatabaseException
	 */
	public boolean isChild(DBNode node1, DBNode node2, boolean fullSearch) throws DatabaseException;
	
}
