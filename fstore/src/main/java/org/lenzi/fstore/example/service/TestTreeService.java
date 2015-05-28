package org.lenzi.fstore.example.service;

import java.util.List;

import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.impl.FSTree;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.service.TreeBuilder;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.tree.Tree;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for interacting with database trees.
 * 
 * @author sal
 */
@Service
@Transactional(propagation=Propagation.REQUIRED)
public class TestTreeService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("testNodeTree")
	private TreeRepository<FSTestNode> treeRepository;	
	
	@Autowired
	private ClosureLogger<FSTestNode> closureLogger;
	
	@Autowired
	private TreeBuilder<FSTestNode> treeBuilder;
	
	// debug method for testing factory generation
	public String getClosureRepoType(){
		return treeRepository.getRepositoryName();
	}
	
	public FSTestNode getNode(FSTestNode node) throws ServiceException {
		
		FSTestNode entity = null;
		try {
			entity = treeRepository.getNode(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;
		
	}
	
	/**
	 * Retrieve the immediate parent
	 * 
	 * @param node - a node of some tree.
	 * @return The nodes parent node.
	 * @throws ServiceException
	 */
	public FSTestNode getParentNode(FSTestNode node) throws ServiceException {

		FSTestNode entity = null;
		try {
			entity = treeRepository.getParentNode(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;		
		
	}
	
	/**
	 * Retrieve the immediate (first level) children.
	 * 
	 * @param node - a node of some tree.
	 * @return The first level children of the node (not the children's children, etc.)
	 * 	If this is a leaf node (no children) then null is returned.
	 * @throws ServiceException
	 */
	public List<FSTestNode> getChildNodes(FSTestNode node) throws ServiceException {

		List<FSTestNode> entityList = null;
		try {
			entityList = treeRepository.getChildNodes(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entityList;			
		
	}
	
	/**
	 * Retrieve the root node of the tree that this node belongs too.
	 * 
	 * @param node - a node of some tree.
	 * @return - the root node of the tree that this node belongs to.
	 * @throws ServiceException
	 */
	public FSTestNode getRootNode(FSTestNode node)  throws ServiceException {

		FSTestNode entity = null;
		try {
			entity = treeRepository.getRootNode(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;			
		
	}
	
	/**
	 * Retrieve a node with its child closure, and child nodes for all child closure entries.
	 * 
	 * @param node
	 * @return
	 * @throws ServiceException
	 */
	public FSTestNode getNodeWithChild(FSTestNode node) throws ServiceException {
		
		FSTestNode entity = null;
		try {
			entity = treeRepository.getNodeWithChild(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;
		
	}
	
	/**
	 * Retrieve a node with its parent closure, and parent nodes for all parent closure entries.
	 * 
	 * @param node
	 * @return
	 * @throws ServiceException
	 */
	public FSTestNode getNodeWithParent(FSTestNode node) throws ServiceException {
		
		FSTestNode entity = null;
		try {
			entity = treeRepository.getNodeWithParent(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;
		
	}
	
	/**
	 * Retrieve a node with its parent and child closure data, plus all parent and child nodes
	 * for all closure entries.
	 * 
	 * @param node
	 * @return
	 * @throws ServiceException
	 */
	public FSTestNode getNodeWithParentChild(FSTestNode node) throws ServiceException {
		
		FSTestNode entity = null;
		try {
			entity = treeRepository.getNodewithParentChild(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;
		
	}
	
	/**
	 * Add a new root node. A root node has no parent. The parent ID will be 0.
	 * 
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public FSTestNode createRootNode(FSTestNode newNode) throws ServiceException {
		
		FSTestNode addedNode = null;
		try {
			addedNode = treeRepository.addRootNode(newNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return addedNode;
		
	}	
	
	/**
	 * Add a new child node.
	 * 
	 * @param parentNode - that parent node under which the new child node will be added.
	 * @param nodeName
	 * @return
	 */
	public FSTestNode createChildNode(FSTestNode parentNode, FSTestNode newNode) throws ServiceException {
		
		FSTestNode addedNode = null;
		try {
			addedNode = treeRepository.addChildNode(parentNode, newNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return addedNode;
	}
	
	/**
	 * Copy a node, and optionally all its children
	 * 
	 * @param nodeToCopy - The node to copy
	 * @param parentNode - The new copy will be placed under this parent node
	 * @param copyChildren - True to copy all children, false not to.
	 * @param copier - The copier which knows how to copy your node object.
	 * @return A reference to the copied node.
	 * @throws ServiceException
	 */
	public FSTestNode copyNode(FSTestNode nodeToCopy, FSTestNode parentNode, boolean copyChildren, NodeCopier<FSTestNode> copier) throws ServiceException {
		
		FSTestNode newCopy = null;
		try {
			newCopy = treeRepository.copyNode(nodeToCopy, parentNode, copyChildren, copier);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return newCopy;
	}
	
	/**
	 * Get closure data for a node. This will give you all the necessary information to build a tree model.
	 * Usually you would do this for a root node of a tree.
	 * 
	 * @param node - The node to fetch closure data for.
	 * @return
	 * @throws ServiceException
	 */
	public List<DBClosure<FSTestNode>> getClosure(FSTestNode node) throws ServiceException {
		
		List<DBClosure<FSTestNode>> closure = null;
		try {
			closure = treeRepository.getClosure(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return closure;
	}
	
	/**
	 * Remove a node
	 * 
	 * @param node
	 */
	public void removeNode(FSTestNode node) throws ServiceException{
		
		try {
			treeRepository.removeNode(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Remove children of a node
	 * 
	 * @param node
	 */
	public void removeChildren(FSTestNode node) throws ServiceException {
		
		try {
			treeRepository.removeChildren(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Move a node
	 * 
	 * @param nodeToMode - The node to move. Cannot be a root node.
	 * @param newParentNode - The new parent node. Can be in a different tree.
	 */
	public FSTestNode moveNode(FSTestNode nodeToMode, FSTestNode newParentNode) throws ServiceException {
		
		FSTestNode updatedNode = null;
		try {
			updatedNode = treeRepository.moveNode(nodeToMode, newParentNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return updatedNode;
		
	}
	
	public FSTree<FSTestNode> geTreeById(FSTree<FSTestNode> tree) throws ServiceException {
		
		FSTree<FSTestNode> result = null;
		try {
			result = treeRepository.getTree(tree);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return result;
	}
	
	// not needed, or working...
	public FSTree<FSTestNode> geTreeById(FSTree<FSTestNode> tree, Class<FSTestNode> nodeType) throws ServiceException {
		
		FSTree<FSTestNode> result = null;
		try {
			result = treeRepository.getTree(tree, nodeType);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return result;
	}	
	
	/**
	 * Add a tree.
	 * 
	 * @param newTree - The new tree to add
	 * @param newRootNode - The root node for the new tree.
	 * @return
	 * @throws ServiceException
	 */
	public FSTree<FSTestNode> addTree(FSTree<FSTestNode> newTree, FSTestNode newRootNode) throws ServiceException {
		
		FSTree<FSTestNode> tree = null;
		try {
			tree = treeRepository.addTree(newTree, newRootNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return tree;
	}
	
	/**
	 * Remove a tree
	 * 
	 * @param tree - tree object with the tree ID set.
	 * @throws ServiceException
	 */
	public void removeTree(FSTree<FSTestNode> tree) throws ServiceException {
		
		try {
			treeRepository.removeTree(tree);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Create a new tree by taking a non-root node of an existing tree and making it the root node of the new tree.
	 * All children of the existing node are also moved over.
	 * 
	 * @param treeName - name of the new tree
	 * @param treeDesc - description of the new tree
	 * @param existingNode - a non-root node of an existing tree which will become the root node of 
	 * 	the new tree. All child nodes will be moved over as well.
	 */
	/*
	public DBTree createTree(DBTree tree, DBNode existingNode) throws ServiceException {
		
		if(existingNode.getParentNodeId() == 0L){
			throw new ServiceException("Existing node is a root node. Cannot make this node a root node of a new tree.");
		}
		DBTree newTree = null;
		try {
			newTree = closureRepository.addTree(tree, existingNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return newTree;
		
	}
	*/
	
	/**
	 * Remove a tree
	 * 
	 * @param tree
	 * @throws ServiceException
	 */
	/*
	public void removeTree(FSTree tree) throws ServiceException {
		
		try {
			closureRepository.removeTree(tree.getTreeId());
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	*/
	
	/**
	 * Take the root node of an tree you want to delete, and move it plus all children to under any node
	 * of some other tree. Then delete the old tree. 
	 * 
	 * @param tree - The tree you want to delete (but keep all nodes under it.)
	 * @param newParentNode - The root node (plus all children) of the tree being deleted will be moved to
	 * 	under this node. This node must be a node in a different tree.
	 * @return
	 */
	/*
	public void removeTree(FSTree tree, FSNode newParentNode) throws ServiceException {
		
		try {
			closureRepository.removeTree(tree, newParentNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	*/		
	
	/**
	 * Check if two nodes are in the same tree. Returns true if they are, false if they are not.
	 * 
	 * @param node1 - the first node
	 * @param node2 - the second node
	 * @return true if node1 and node2 are in the same tree, false if not.
	 * @throws ServiceException
	 */
	public boolean isSameTree(FSTestNode node1, FSTestNode node2) throws ServiceException {
		
		try {
			return treeRepository.isSameTree(node1, node2);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
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
	 * @throws ServiceException
	 */
	public boolean isParent(FSTestNode node1, FSTestNode node2, boolean fullSearch) throws ServiceException {
		
		try {
			return treeRepository.isParent(node1, node2, fullSearch);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
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
	 * @throws ServiceException
	 */
	public boolean isChild(FSTestNode node1, FSTestNode node2, boolean fullSearch) throws ServiceException {
		
		try {
			return treeRepository.isChild(node1, node2, fullSearch);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}		
		
	}
	
	/**
	 * Build an unmanaged tree object from a node. ALl children of the node will be added to the tree.
	 * 
	 * @param node
	 * @return
	 * @throws ServiceException
	 */
	public Tree<FSTestNode> buildTree(FSTestNode node) throws ServiceException {
		
		// initial error checking
		if(node == null || node.getNodeId() == null){
			throw new ServiceException("Cannont build tree. Node object is null, or node is is null.");
		}
		
		// fetch node with child closure, and child node data
		FSTestNode n = null;
		try {
			n = treeRepository.getNodeWithChild(node);
		} catch (DatabaseException e) {
			throw new ServiceException("Cannont build tree, error fetching node with child closure and child node data.", e);
		}
		
		// error checking
		if(n == null){
			throw new ServiceException("Cannont build tree, node not found. Fetched node object was null.");
		}
		
		return treeBuilder.buildTree(n);
		
	}

}
