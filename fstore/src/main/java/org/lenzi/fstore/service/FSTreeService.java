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
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		// When the tree is created, the node is added automatically by hiberate. see code for addTree in closure repository.
		//
		//closureRepository.addNode(fsTree.getRootNodeId(), rootNodeName);
		
		return fsTree;
		
	}
	
	/**
	 * Add a new node.
	 * 
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public FSNode createNode(FSNode parentNode, String nodeName){
		
		FSNode fsNode = null;
		fsNode = closureRepository.addNode(parentNode.getNodeId(), nodeName);
		
		return fsNode;
		
	}
	
	/**
	 * Remove a node
	 * 
	 * @param node
	 */
	public void removeNode(FSNode node){
		
		closureRepository.removeNode(node.getNodeId());
		
	}
	
	/**
	 * Remove children of a node
	 * 
	 * @param node
	 */
	public void removeChildren(FSNode node){
		
		closureRepository.removeChildren(node.getNodeId());
		
	}
	
	/**
	 * Move a node
	 * 
	 * @param nodeToMode
	 * @param newParentNode
	 */
	public void moveNode(FSNode nodeToMode, FSNode newParentNode){
		
		try {
			closureRepository.moveNode(nodeToMode.getNodeId(), newParentNode.getNodeId());
		} catch (DatabaseException e) {
			e.printStackTrace();
			logger.error("Error moving node " + nodeToMode.getNodeId() + 
					" to under node " + newParentNode.getNodeId() + ". " + e.getMessage());
		}
		
	}
	
	/**
	 * Make a leaf node into a root node of a new tree.
	 * 
	 * @param treeName - name of the new tree
	 * @param treeDesc - description of the new tree
	 * @param leafNode - the existing leaf node that will become the root node of the new tree
	 */
	public FSTree leafToRoot(String treeName, String treeDesc, FSNode leafNode){
		
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
	public FSNode rootToLeaf(FSTree tree, FSNode newParentNode){
		
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
	public Tree<TreeMeta> buildTree(FSTree tree){
		
		if(tree == null || tree.getRootNode() == null){
			return null;
		}
		
		List<FSClosure> closure = null;
		try {
			closure = closureRepository.getClosureByNodeId(tree.getRootNode().getNodeId());
		} catch (DatabaseException e) {
			e.printStackTrace();
			logger.error("Error getting closure data for root node " + tree.getRootNode().getNodeId() + ". " + e.getMessage());
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
