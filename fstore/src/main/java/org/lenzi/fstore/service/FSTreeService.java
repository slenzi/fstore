package org.lenzi.fstore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.lenzi.filestore.util.CollectionUtil;
import org.lenzi.filestore.util.LogUtil;
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
	public FSTree createTree(String treeName, String treeDesc, String rootNodeName) {
		
		FSTree fsTree = null;
		try {
			fsTree = closureRepository.addTree(treeName, treeDesc, rootNodeName);
		} catch (DatabaseException e) {
			e.printStackTrace();
			logger.error("Error creating new tree. " + e.getMessage());
		}
		
		//
		// When the tree is created, the node is added automatically by Hibernate. see code for addTree in closure repository.
		//
		//closureRepository.addNode(fsTree.getRootNodeId(), rootNodeName);
		
		return fsTree;
		
	}
	
	/**
	 * Remove a tree
	 * 
	 * @param tree
	 * @throws ServiceException
	 */
	public void removeTree(FSTree tree) throws ServiceException {
		
		// TODO - implement
		
		// delete all children of tree's root node
		
		// then delete tree with root node
		
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
			throw new ServiceException(e.getMessage());
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
			throw new ServiceException(e.getMessage());
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
			throw new ServiceException(e.getMessage());
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
			throw new ServiceException(e.getMessage());
		}
		
	}
	
	/**
	 * Make a leaf node into a root node of a new tree.
	 * 
	 * @param treeName - name of the new tree
	 * @param treeDesc - description of the new tree
	 * @param leafNode - the existing leaf node that will become the root node of the new tree
	 */
	public FSTree leafToRoot(String treeName, String treeDesc, FSNode leafNode) throws ServiceException {
		
		if(leafNode.getParentNodeId() == 0L){
			throw new ServiceException("Node is not a leaf node.");
		}		
		
		// TODO - making a leaf node a root node requires making a new tree!
		
		// make sure the node is actually a leaf node!

		return null;
		
	}
	
	/**
	 * Make an existing root node of a tree a leaf node of some other tree.
	 * 
	 * @param tree
	 * @param newParentNode
	 * @return
	 */
	public FSNode rootToLeaf(FSTree tree, FSNode newParentNode) throws ServiceException {
		
		// TODO - making a current root node a leaf node requires deleting the tree entry!
		
		// make sure the node being moved is an actual root node
		
		// make sure they are not trying to move the root node to another node under the same tree!
		
		return null;
		
	}
	
	
	/**
	 * Builds a non manage Tree object from a database FSTree.
	 * 
	 * @param tree
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
			throw new ServiceException(e.getMessage());
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
		
		LogUtil.logClosure(closureList);
		
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
