package org.lenzi.fstore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.model.tree.TreeNode;
import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.DBTree;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.CollectionUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for interacting with database trees.
 * 
 * @author sal
 */
@Service
@Transactional
public class TreeService<N extends FSNode<N>> {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	TreeRepository<N> treeRepository;
	
	@Autowired
	ClosureLogger<N> closureLogger;
	
	// debug method for testing factory generation
	public String getClosureRepoType(){
		return treeRepository.getRepositoryName();
	}
	
	public N getNode(N node) throws ServiceException {
		
		N entity = null;
		try {
			entity = treeRepository.getNode(node);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entity;
		
	}
	
	public N getNodeWithChild(N node) throws ServiceException {
		
		N entity = null;
		try {
			entity = treeRepository.getNodeWithChild(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;
		
	}
	
	public N getNodeWithParent(N node) throws ServiceException {
		
		N entity = null;
		try {
			entity = treeRepository.getNodeWithParent(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;
		
	}
	
	public N getNodeWithParentChild(N node) throws ServiceException {
		
		N entity = null;
		try {
			entity = treeRepository.getNodewithParentChild(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return entity;
		
	}
	
	/**
	 * Get tree by id
	 * 
	 * @param treeId
	 * @return
	 * @throws ServiceException
	 */
	/*
	public FSTree getTree(Long treeId) throws ServiceException {
		
		FSTree fsTree = null;
		try {
			fsTree = closureRepository.getTree(treeId);
		} catch (DatabaseException e) {
			e.printStackTrace();
			logger.error("Error creating new tree. " + e.getMessage());
		}
		
		return fsTree;
	}
	*/
	
	/**
	 * Create a new tree
	 * 
	 * @param tree - Tree data
	 * @param rootNode - Root node for the tree
	 * @return
	 * @throws ServiceException
	 */
	public DBTree<N> createTree(DBTree<N> tree, N rootNode) throws ServiceException {
		
		DBTree<N> newTree = null;
		try {
			newTree = treeRepository.addTree(tree, rootNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return newTree;
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
	 * Add a new root node.
	 * 
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public N createRootNode(N newNode) throws ServiceException {
		
		N addedNode = null;
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
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public N createChildNode(N parentNode, N newNode) throws ServiceException {
		
		N addedNode = null;
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
	public N copyNode(N nodeToCopy, N parentNode, boolean copyChildren, NodeCopier<N> copier) throws ServiceException {
		
		N newCopy = null;
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
	public List<DBClosure<N>> getClosure(N node) throws ServiceException {
		
		List<DBClosure<N>> closure = null;
		try {
			closure = treeRepository.getClosure(node);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return closure;
	}
	
	/**
	 * Add a tree.
	 * 
	 * @param newTree - The new tree to add
	 * @param newRootNode - The root node for the new tree.
	 * @return
	 * @throws ServiceException
	 */
	public DBTree<N> addTree(DBTree<N> newTree, N newRootNode) throws ServiceException {
		
		DBTree<N> tree = null;
		try {
			tree = treeRepository.addTree(newTree, newRootNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return tree;
	}
	
	/**
	 * Remove a node
	 * 
	 * @param node
	 */
	public void removeNode(N node) throws ServiceException{
		
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
	public void removeChildren(N node) throws ServiceException {
		
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
	public N moveNode(N nodeToMode, N newParentNode) throws ServiceException {
		
		N updatedNode = null;
		try {
			updatedNode = treeRepository.moveNode(nodeToMode, newParentNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		return updatedNode;
		
	}
	
	/**
	 * Check if two nodes are in the same tree. Returns true if they are, false if they are not.
	 * 
	 * @param node1 - the first node
	 * @param node2 - the second node
	 * @return true if node1 and node2 are in the same tree, false if not.
	 * @throws ServiceException
	 */
	public boolean isSameTree(N node1, N node2) throws ServiceException {
		
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
	public boolean isParent(N node1, N node2, boolean fullSearch) throws ServiceException {
		
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
	public boolean isChild(N node1, N node2, boolean fullSearch) throws ServiceException {
		
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
	public Tree<N> buildTree(N node) throws ServiceException {
		
		// initial error checking
		if(node == null || node.getNodeId() == null){
			throw new ServiceException("Cannont build tree. Node object is null, or node is is null.");
		}
		
		// fetch node with child closure, and child node data
		N n = null;
		try {
			n = treeRepository.getNodeWithChild(node);
		} catch (DatabaseException e) {
			throw new ServiceException("Error fetching node with child closure, and child node data.", e);
		}
		
		// error checking
		if(n == null){
			throw new ServiceException("Error, node not found. Fetched node object was null.");
		}
		Set<DBClosure<N>> childClosure = n.getChildClosure();
		if(childClosure == null || childClosure.size() == 0){
			throw new ServiceException("Error, node was fetched, but no closure data...");
		}
		
		// convert closure data to map. loop through child nodes of child closure
		HashMap<Long,List<N>> treeMap = new HashMap<Long,List<N>>();
		for(DBClosure<N> closure : CollectionUtil.emptyIfNull(childClosure)){
			if(treeMap.containsKey(closure.getChildNode().getParentNodeId())){
				treeMap.get(closure.getChildNode().getParentNodeId()).add(closure.getChildNode());
			}else{
				List<N> childList = new ArrayList<N>();
				childList.add(closure.getChildNode());
				treeMap.put(closure.getChildNode().getParentNodeId(), childList);
			}
		}
		
		// create root node of tree
		TreeNode<N> rootNode = new TreeNode<N>();
		rootNode.setData(n);
		
		// add all children under root node
		addChildNodesFromMap(rootNode,treeMap);
		
		// create tree and set root node
		Tree<N> tree = new Tree<N>();
		tree.setRootNode(rootNode);
		
		return tree;
		
	}
	
	/**
	 * Helper method for buildTree(N node)
	 * 
	 * @param parentNode
	 * @param treeMap
	 */
	private void addChildNodesFromMap(TreeNode<N> parentNode, HashMap<Long, List<N>> treeMap) {
		
		TreeNode<N> childTreeNode = null;
		
		for(N childNode : CollectionUtil.emptyIfNull(treeMap.get(parentNode.getData().getNodeId()))){
			
			childTreeNode = new TreeNode<N>();
			childTreeNode.setData(childNode);
			childTreeNode.setParent(parentNode);
			parentNode.addChildNode(childTreeNode);
				
			addChildNodesFromMap(childTreeNode, treeMap);
	
		}
		
	}
	
	
	/*
	 * ----------------- old code below -------------------
	 */
	
	
	
	
	

	/**
	 * @deprecated - old code
	 * Builds a non manage tree object from a database FSTree.
	 * 
	 * @param tree The FSTree entity
	 * @return
	 */
	public Tree<TreeMeta> buildTreeOld(DBTree<N> tree) throws ServiceException {
		
		if(tree == null || tree.getRootNode() == null){
			return null;
		}
		
		// TODO - test this, when we get closure we need to fetch from N (FSTestNode). The HQL query currently fetches from parent FSNode
		
		List<DBClosure<N>> closure = null;
		try {
			closure = treeRepository.getClosure(tree.getRootNode());
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return buildTreeOld(closure);
		
	}
	
	/**
	 * @deprecated - old code
	 * Builds a GenericTree from a list of closure data.
	 * 
	 * @param closureList
	 * @return
	 */
	public Tree<TreeMeta> buildTreeOld(List<DBClosure<N>> closureList){
		
		if(closureList == null || closureList.size() == 0){
			return null;
		}
		
		//closureLogger.logClosure(closureList);
		
		HashMap<Long,List<N>> treeMap = new HashMap<Long,List<N>>();
		
		// get root node of tree
		N rootNode = null;
		for(DBClosure<N> c : closureList){
			if(c.hasParent() && c.hasChild()){
				rootNode = c.getParentNode();
				break;
			}
		}
		
		logger.debug("Build tree, root node => " + closureLogger.getNodeString(rootNode));
		
		//
		// loop through closure list and build tree map
		//
		DBClosure<N> closure = null;
		for(int closureIndex=0; closureIndex<closureList.size(); closureIndex++){
			closure = closureList.get(closureIndex);
			if(closure.hasParent() && closure.hasChild()){
				if(treeMap.containsKey(closure.getParentNode().getNodeId())){
					treeMap.get(closure.getParentNode().getNodeId()).add(closure.getChildNode());
				}else{
					List<N> childList = new ArrayList<N>();
					childList.add(closure.getChildNode());
					treeMap.put(closure.getParentNode().getNodeId(), childList);
				}
			}
		}
		
		logger.debug("Build tree, map size => " + treeMap.size());
		
		//
		// Build meta for root node
		//
		TreeNode<TreeMeta> treeRootNode = new TreeNode<TreeMeta>();
		TreeMeta meta = new TreeMeta();
		meta.setId(rootNode.getNodeId());
		meta.setName(rootNode.getName());
		//meta.setParentId(rootNode.getParentNodeId());
		meta.setCreationDate(rootNode.getDateCreated());
		meta.setUpdatedDate(rootNode.getDateUpdated());
		treeRootNode.setData(meta);
		
		logger.debug("Build tree, created meta for root node");
		
		//
		// recursively build out the tree
		//
		addChildrenOld(treeRootNode, treeMap);
		
		Tree<TreeMeta> tree = new Tree<TreeMeta>();
		tree.setRootNode(treeRootNode);
		
		return tree;
	}
	
	/**
	 * @deprecated - old code
	 * 
	 * @param parentNode
	 * @param treeMap
	 */
	private void addChildrenOld(TreeNode<TreeMeta> parentNode, HashMap<Long,List<N>> treeMap){
		
		TreeNode<TreeMeta> childTreeNode = null;
		
		for(N childNode : CollectionUtil.emptyIfNull(treeMap.get(parentNode.getData().getId()))){
			
			// closure table contains entries where a node is a parent of itself at depth 0. we
			// need to skip over these entries otherwise we'll go into an infinite loop and exhaust the
			// memory stack.
			if(parentNode.getData().getId() != childNode.getNodeId()){
			
				childTreeNode = new TreeNode<TreeMeta>();
				childTreeNode.setData(getMetaOld(childNode));
				
				childTreeNode.setParent(parentNode);
				parentNode.addChildNode(childTreeNode);
				
				addChildrenOld(childTreeNode, treeMap);
			}
		}
		
	}
	
	/**
	 * @deprecated - old code
	 * 
	 * @param node
	 * @return
	 */
	private TreeMeta getMetaOld(N node){
		TreeMeta meta = new TreeMeta();
		meta.setId(node.getNodeId());
		meta.setName(node.getName());
		//meta.setParentId(node.getParentNodeId());
		meta.setCreationDate(node.getDateCreated());
		meta.setUpdatedDate(node.getDateUpdated());
		return meta;
	}	

}
