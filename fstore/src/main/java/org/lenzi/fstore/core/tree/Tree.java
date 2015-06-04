package org.lenzi.fstore.core.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lenzi.fstore.core.tree.Trees.PrintOption;
import org.lenzi.fstore.core.tree.Trees.WalkOption;

public class Tree<N> {

	private TreeNode<N> rootNode = null;
	
	public Tree() {
		super();
	}

	public Tree(TreeNode<N> rootNode) {
		super();
		this.rootNode = rootNode;
	}

	/**
	 * @return the rootNode
	 */
	public TreeNode<N> getRootNode() {
		return rootNode;
	}

	/**
	 * @param rootNode the rootNode to set
	 */
	public void setRootNode(TreeNode<N> rootNode) {
		this.rootNode = rootNode;
	}
	
    public String printTree(){
    	
    	return Trees.printTree(rootNode, PrintOption.TERMINAL);
    	
    }
    
    public String printHtmlTree(){
    	
    	return Trees.printTree(rootNode, PrintOption.HTML);
    	
    }
    
    public List<TreeNode<N>> toList(WalkOption option){
    	
    	if(rootNode == null){
    		return null;
    	}
    	
    	List<TreeNode<N>> nodeList = new ArrayList<TreeNode<N>>();
    	
    	try {
			Trees.walkTree(rootNode,
					(treeNode) -> {
						nodeList.add(treeNode);
					}
					, option);
		} catch (TreeNodeVisitException e) {
			// eat it
		}
    	
    	return nodeList;
    	
    }
    
	/**
	 * Iterates through the list of child nodes for this node, and compares each child to 'compareNode'. If the
	 * nodes are equal, defined by the comparator, the first match is returned.
	 * 
	 * @param node
	 * @param comparator
	 * @return
	 */
	public TreeNode<N> getFirstChildMatch(TreeNode<N> compareNode, Comparator<TreeNode<N>> comparator){
		
		if(rootNode == null){
			return null;
		}
		return rootNode.getFirstChildMatch(compareNode, comparator);
		
	}
	
	/**
	 * Iterates through the list of child nodes for this node, and compares each child to 'compareNode'. All
	 * nodes that match, defined by the caparator, are returned.
	 * 
	 * @param node
	 * @param comparator
	 * @return
	 */
	public List<TreeNode<N>> getChildMatch(TreeNode<N> compareNode, Comparator<TreeNode<N>> comparator){
		
		if(rootNode == null){
			return null;
		}
		return rootNode.getChildMatch(compareNode, comparator);
	}    
    
}
