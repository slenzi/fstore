package org.lenzi.fstore.tree;

import java.util.ArrayList;
import java.util.List;

import org.lenzi.fstore.tree.Trees.PrintOption;
import org.lenzi.fstore.tree.Trees.WalkOption;

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
    
}
