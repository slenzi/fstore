package org.lenzi.fstore.tree;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {

	private TreeNode<T> rootNode = null;
	
	public Tree() {
		super();
	}

	public Tree(TreeNode<T> rootNode) {
		super();
		this.rootNode = rootNode;
	}

	/**
	 * @return the rootNode
	 */
	public TreeNode<T> getRootNode() {
		return rootNode;
	}

	/**
	 * @param rootNode the rootNode to set
	 */
	public void setRootNode(TreeNode<T> rootNode) {
		this.rootNode = rootNode;
	}	

    public List<TreeNode<T>> toList() {
        List<TreeNode<T>> list = new ArrayList<TreeNode<T>>();
        walk(rootNode, list);
        return list;
    }
    
    private void walk(TreeNode<T> node, List<TreeNode<T>> list){
    	list.add(node);
        for (TreeNode<T> data : node.getChildren()) {
            walk(data, list);
        }    	
    }
    
    public String toString() {
        return toList().toString();
    }
    
    public String printTree(String lineSeparator){
    	return rootNode.printTree(lineSeparator);
    }
	
    public String printTree(){
    	return rootNode.printTree();
    }
    
    public String printHtmlTree(){
    	return rootNode.printHtmlTree();
    }
    
}
