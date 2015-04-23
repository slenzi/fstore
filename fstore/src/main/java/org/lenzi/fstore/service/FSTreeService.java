package org.lenzi.fstore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.model.tree.TreeNode;
import org.lenzi.fstore.repository.ClosureRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.FSClosure;
import org.lenzi.fstore.repository.model.FSNode;
import org.lenzi.fstore.repository.model.FSTree;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.CollectionUtil;
import org.lenzi.fstore.util.LogUtil;
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
public class FSTreeService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	ClosureRepository closureRepository;
	
	// debug method for testing factory generation
	public String getClosureRepoType(){
		return closureRepository.getRepositoryName();
	}
	
	/**
	 * Get tree by id
	 * 
	 * @param treeId
	 * @return
	 * @throws ServiceException
	 */
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
	
	/**
	 * Create a new tree.
	 * 
	 * @param treeName
	 * @param treeDesc
	 * @param rootNodeName
	 * @return
	 */
	public FSTree createTree(String treeName, String treeDesc, String rootNodeName) throws ServiceException {
		
		FSTree fsTree = null;
		try {
			fsTree = closureRepository.addTree(treeName, treeDesc, rootNodeName);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return fsTree;
		
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
	public FSTree createTree(String treeName, String treeDesc, FSNode existingNode) throws ServiceException {
		
		if(existingNode.getParentNodeId() == 0L){
			throw new ServiceException("Existing node is a root node. Cannot make this node a root node of a new tree.");
		}
		FSTree newTree = null;
		try {
			newTree = closureRepository.addTree(treeName, treeDesc, existingNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return newTree;
		
	}	
	
	/**
	 * Remove a tree
	 * 
	 * @param tree
	 * @throws ServiceException
	 */
	public void removeTree(FSTree tree) throws ServiceException {
		
		try {
			closureRepository.removeTree(tree.getTreeId());
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Take the root node of an tree you want to delete, and move it plus all children to under any node
	 * of some other tree. Then delete the old tree. 
	 * 
	 * @param tree - The tree you want to delete (but keep all nodes under it.)
	 * @param newParentNode - The root node (plus all children) of the tree being deleted will be moved to
	 * 	under this node. This node must be a node in a different tree.
	 * @return
	 */
	public void removeTree(FSTree tree, FSNode newParentNode) throws ServiceException {
		
		try {
			closureRepository.removeTree(tree, newParentNode);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}	
	
	/**
	 * Add a new node.
	 * 
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public FSNode createNode(FSNode parentNode, String nodeName) throws ServiceException {
		
		FSNode fsNode = null;
		try {
			fsNode = closureRepository.addNode(parentNode.getNodeId(), nodeName);
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return fsNode;
		
	}
	
	/**
	 * Remove a node
	 * 
	 * @param node
	 */
	public void removeNode(FSNode node) throws ServiceException{
		
		if(node.getParentNodeId() == 0L){
			throw new ServiceException("Cannot remove root node of tree. Use removeTree() method.");
		}		
		
		try {
			closureRepository.removeNode(node.getNodeId());
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Remove children of a node
	 * 
	 * @param node
	 */
	public void removeChildren(FSNode node) throws ServiceException {
		
		try {
			closureRepository.removeChildren(node.getNodeId());
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Copy a node and all it's children.
	 * 
	 * @param nodeToCopy - the node to copy
	 * @param copyToNode - where everything is copied to.
	 * @param copyChildren - true to copy over all the nodes children nodes as well, false to just copy the node.
	 * @throws ServiceException
	 */
	public void copyNode(FSNode nodeToCopy, FSNode copyToNode, boolean copyChildren) throws ServiceException {
		
		try {
			closureRepository.copyNode(nodeToCopy.getNodeId(), copyToNode.getNodeId(), copyChildren);
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
	public void moveNode(FSNode nodeToMode, FSNode newParentNode) throws ServiceException {
		
		if(nodeToMode.getParentNodeId() == 0L){
			throw new ServiceException("Cannot move root node of tree. Use rootToLeaf() method.");
		}
		
		try {
			closureRepository.moveNode(nodeToMode.getNodeId(), newParentNode.getNodeId());
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
	}

	/**
	 * Builds a non managed tree object from a database FSTree.
	 * 
	 * @param treeId - ID of the FSTree.
	 * @return
	 * @throws ServiceException
	 */
	public Tree<TreeMeta> buildTree(Long treeId) throws ServiceException {
		
		FSTree fsTree = getTree(treeId);
		
		return buildTree(fsTree);
		
	}
	
	/**
	 * Builds a non manage tree object from a database FSTree.
	 * 
	 * @param tree The FSTree entity
	 * @return
	 */
	public Tree<TreeMeta> buildTree(FSTree tree) throws ServiceException {
		
		if(tree == null || tree.getRootNode() == null){
			return null;
		}
		
		List<FSClosure> closure = null;
		try {
			closure = closureRepository.getClosureByNodeId(tree.getRootNode().getNodeId());
		} catch (DatabaseException e) {
			throw new ServiceException(e.getMessage(), e);
		}
		
		return buildTree(closure);
		
	}
	
	/**
	 * Builds a GenericTree from a list of closure data.
	 * 
	 * @param closureList
	 * @return
	 */
	private Tree<TreeMeta> buildTree(List<FSClosure> closureList){
		
		if(closureList == null || closureList.size() == 0){
			return null;
		}
		
		//LogUtil.logClosure(closureList);
		
		HashMap<Long,List<FSNode>> treeMap = new HashMap<Long,List<FSNode>>();
		
		// get root node of tree
		FSNode rootNode = null;
		for(FSClosure c : closureList){
			if(c.hasParent() && c.hasChild()){
				rootNode = c.getParentNode();
				break;
			}
		}
		
		logger.debug("Build tree, root node => " + LogUtil.getNodeString(rootNode));
		
		//
		// loop through closure list and build tree map
		//
		FSClosure closure = null;
		for(int closureIndex=0; closureIndex<closureList.size(); closureIndex++){
			closure = closureList.get(closureIndex);
			if(closure.hasParent() && closure.hasChild()){
				if(treeMap.containsKey(closure.getParentNode().getNodeId())){
					treeMap.get(closure.getParentNode().getNodeId()).add(closure.getChildNode());
				}else{
					List<FSNode> childList = new ArrayList<FSNode>();
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
		addChildren(treeRootNode, treeMap);
		
		Tree<TreeMeta> tree = new Tree<TreeMeta>();
		tree.setRootNode(treeRootNode);
		
		return tree;
	}
	
	// walk the data in the tree map and add children to parentNode
	private void addChildren(TreeNode<TreeMeta> parentNode, HashMap<Long,List<FSNode>> treeMap){
		
		TreeNode<TreeMeta> childTreeNode = null;
		
		for(FSNode childNode : CollectionUtil.emptyIfNull(treeMap.get(parentNode.getData().getId()))){
			
			// closure table contains entries where a node is a parent of itself at depth 0. we
			// need to skip over these entries otherwise we'll go into an infinite loop and exhaust the
			// memory stack.
			if(parentNode.getData().getId() != childNode.getNodeId()){
			
				childTreeNode = new TreeNode<TreeMeta>();
				childTreeNode.setData(getMeta(childNode));
				
				childTreeNode.setParent(parentNode);
				parentNode.addChildNode(childTreeNode);
				
				addChildren(childTreeNode, treeMap);
			}
		}
		
	}
	
	// build TreeMeta object from FSNode object
	private TreeMeta getMeta(FSNode node){
		TreeMeta meta = new TreeMeta();
		meta.setId(node.getNodeId());
		meta.setName(node.getName());
		//meta.setParentId(node.getParentNodeId());
		meta.setCreationDate(node.getDateCreated());
		meta.setUpdatedDate(node.getDateUpdated());
		return meta;
	}	

}
