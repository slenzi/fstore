/**
 * 
 */
package org.lenzi.fstore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.tree.Tree;
import org.lenzi.fstore.tree.TreeNode;
import org.lenzi.fstore.util.CollectionUtil;
import org.springframework.stereotype.Service;

/**
 * @author sal
 *
 */
@Service
public class TreeBuilder<N extends FSNode<N>> {

	/**
	 * 
	 */
	public TreeBuilder() {
		
	}
	
	/**
	 * Build an unmanaged tree object from a managed database node which contains all the child closure data.
	 * 
	 * @param node
	 * @return
	 * @throws ServiceException
	 */
	public Tree<N> buildTree(N node) throws ServiceException {
		
		// initial error checking
		if(node == null){
			throw new ServiceException("Cannont build tree, node object is null.");
		}

		Set<DBClosure<N>> childClosure = node.getChildClosure();
		if(childClosure == null || childClosure.size() == 0){
			throw new ServiceException("Cannont build tree, node has no child closure data. This is required.");
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
		rootNode.setData(node);
		
		// add all children under root node
		addChildNodesFromMap(rootNode, treeMap);
		
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

}
