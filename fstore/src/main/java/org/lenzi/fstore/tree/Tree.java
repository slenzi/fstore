package org.lenzi.fstore.tree;

import org.lenzi.fstore.tree.Trees.PrintOption;

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

	/* deprecated
    public List<TreeNode<N>> toList() {
        List<TreeNode<N>> list = new ArrayList<TreeNode<N>>();
        walk(rootNode, list);
        return list;
    }
    */
    
    /* deprecated
    private void walk(TreeNode<N> node, List<TreeNode<N>> list){
    	list.add(node);
        for (TreeNode<N> data : node.getChildren()) {
            walk(data, list);
        }    	
    }
    */
    
	/* deprecated
    public String toString() {
        return toList().toString();
    }
    */
    
    /* deprecated
    public String printTree(String lineSeparator){
    	return rootNode.printTree(lineSeparator);
    }
    */
	
    public String printTree(){
    	
    	return Trees.printTree(rootNode, PrintOption.TERMINAL);
    	//return rootNode.printTree();
    	
    }
    
    public String printHtmlTree(){
    	
    	return Trees.printTree(rootNode, PrintOption.HTML);
    	//return rootNode.printHtmlTree();
    	
    }
    
}
