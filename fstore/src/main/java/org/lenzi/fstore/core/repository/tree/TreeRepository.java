package org.lenzi.fstore.core.repository.tree;

import java.util.List;

import org.lenzi.fstore.core.model.util.NodeCopier;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.model.DBClosure;
import org.lenzi.fstore.core.repository.model.impl.FSNode;
import org.lenzi.fstore.core.repository.model.impl.FSTree;

public interface TreeRepository<N extends FSNode<N>> {
	
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
	public N getNode(N node) throws DatabaseException;
	// include parent closure data, with nodes
	public N getNodeWithParent(N node) throws DatabaseException;
	public N getNodeWithParent(Long resourceId, Class<N> clazz) throws DatabaseException;
	// include child closure data, with nodes
	public N getNodeWithChild(N node) throws DatabaseException;
	public N getNodeWithChild(Long nodeId, Class<N> clazz) throws DatabaseException;
	// include child closure data, with nodes, up to a certain depth
	public N getNodeWithChild(N node, int maxDepth) throws DatabaseException;
	// include both parent and child closure data, with nodes
	public N getNodewithParentChild(N node) throws DatabaseException;
	
	/**
	 * Fetch the node's parent node. If this node is a root node then null is returned.
	 * 
	 * @param node - a node in some tree
	 * @return The node's parent, or null if the node is a root node (root nodes have no parent)
	 * @throws DatabaseException
	 */
	public N getParentNode(N node) throws DatabaseException;
	
	/**
	 * Fetch the first level children of the node (does not include the children's children, etc). If the
	 * node is a leaf node (has no children) then null is returned.
	 * 
	 * @param node - a node in some tree
	 * @return The first level children of the node, or null if a leaf node.
	 * @throws DatabaseException
	 */
	public List<N> getChildNodes(N node) throws DatabaseException;
	
	/**
	 * Retrieve the root node of the tree that this node belongs too.
	 * 
	 * @param node - a node in some tree
	 * @return - the root node of the tree that this node belongs to.
	 * @throws DatabaseException
	 */
	public N getRootNode(N node) throws DatabaseException;
	public N getRootNode(Long resourceId, Class<N> clazz) throws DatabaseException;
	
	/**
	 * Add a new root node. Parent node ID will be set to 0.
	 * 
	 * @param newNode
	 * @return
	 * @throws DatabaseException
	 */
	public N addRootNode(N newNode) throws DatabaseException;
	
	/**
	 * Add a new child node under the parent node.
	 * 
	 * @param parentNodeId - The ID of the parent node.
	 * @param nodeName - The name of the new node.
	 * @return
	 * @throws DatabaseException
	 */
	public N addChildNode(N parentNode, N newNode) throws DatabaseException;
	
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
	public N copyNode(N nodeToCopy, N parentNode, boolean copyChildren, NodeCopier<N> copier) throws DatabaseException;
	
	/**
	 * Get a tree with it's root node.
	 * 
	 * @param tree - a tree object with the tree ID set.
	 * @return
	 * @throws DatabaseException
	 */
	public FSTree<N> getTree(FSTree<N> tree) throws DatabaseException;
	// specify the class type of the node
	public FSTree<N> getTree(FSTree<N> tree, Class<N> nodeClass) throws DatabaseException;
	
	/**
	 * Add a tree.
	 * 
	 * @param newTree - a tree object with the tree name set
	 * @param newRootNode - a node object with the node name set.
	 * @return
	 * @throws DatabaseException
	 */
	public FSTree<N> addTree(FSTree<N> newTree, N newRootNode) throws DatabaseException;
	
	/**
	 * Remove a tree
	 * 
	 * @param tree - a tree object with the ID set.
	 * @throws DatabaseException
	 */
	public void removeTree(FSTree<N> tree) throws DatabaseException;
	
	/**
	 * Remove a node, plus all its children.
	 * 
	 * @param node
	 * @throws DatabaseException
	 */
	public void removeNode(N node) throws DatabaseException;
	
	/**
	 * Remove all children of a node, but not the node itself.
	 * 
	 * @param node
	 * @throws DatabaseException
	 */
	public void removeChildren(N node) throws DatabaseException;	
	
	/**
	 * Get closure data for a node. This will give you all the necessary information to build a tree model.
	 * Usually you would do this for a root node of a tree.
	 * 
	 * @param node - The node to fetch closure data for. Most likely you want this to be a root node of a tree.
	 * @return
	 * @throws DatabaseException
	 */
	public List<DBClosure<N>> getClosure(N node) throws DatabaseException;
	
	/**
	 * Move a node
	 * 
	 * @param nodeToMode
	 * @param newParentNode
	 * @throws DatabaseException
	 */
	public N moveNode(N nodeToMode, N newParentNode)  throws DatabaseException;
	
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
	 * Check if two nodes are in the same tree. Returns true if they are, false if they are not.
	 * 
	 * @param node1 - the first node
	 * @param node2 - the second node
	 * @return true if node1 and node2 are in the same tree, false if not.
	 * @throws DatabaseException
	 */
	public boolean isSameTree(N node1, N node2) throws DatabaseException;
	
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
	public boolean isParent(N node1, N node2, boolean fullSearch) throws DatabaseException;
	
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
	public boolean isChild(N node1, N node2, boolean fullSearch) throws DatabaseException;
	
}
