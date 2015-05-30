package org.lenzi.fstore.tree;

import java.util.List;

/**
 * Contains a bunch of static methods for working on Trees.
 * 
 * @author sal
 */
public final class Trees {

	// http://en.wikipedia.org/wiki/Tree_traversal
	public enum WalkOption {
		
		// bottom-up
		POST_ORDER_TRAVERSAL,
		
		// top-down
		PRE_ORDER_TRAVERSAL
		
	}
	
	public enum PrintOption {
		
		// uses <br> tag to separate lines
		HTML,
		
		// uses system line.separator to separate lines
		TERMINAL
		
	}
	
	/**
	 * Walk a tree
	 * 
	 * @param tree - will start at the tree's root node
	 * @param visitor
	 * @param walkOption
	 */
	public static <N> void walkTree(Tree<N> tree, TreeNodeVisitor<N> visitor, WalkOption walkOption) throws TreeNodeVisitException {
		walkTree(tree.getRootNode(), visitor, walkOption);
	}
	
	/**
	 * Walk a tree
	 * 
	 * @param start - node to start at
	 * @param visitor
	 * @param option
	 */
	public static <N> void walkTree(TreeNode<N> start, TreeNodeVisitor<N> visitor, WalkOption walkOption) throws TreeNodeVisitException {
		
		switch(walkOption){
		
			case POST_ORDER_TRAVERSAL:
				postOrderTraversal(start, visitor);
				break;
				
			case PRE_ORDER_TRAVERSAL:
				break;
				
			default:
				break;
				
		}
		
	}
	
	/**
	 * Walk tree in post-order traversal
	 * 
	 * @param node
	 * @param visitor
	 */
	private static <N> void postOrderTraversal(TreeNode<N> node, TreeNodeVisitor<N> visitor) throws TreeNodeVisitException {
		if(node.hasChildren()){
			for(TreeNode<N> childNode : node.getChildren()){
				postOrderTraversal(childNode, visitor);
			}
			visitor.visitNode(node);
		}else{
			visitor.visitNode(node);
		}
	}
	
	/**
	 * Print a tree.
	 * 
	 * Traverses the tree and calls TreeNode.getData().toString() for each node.
	 * 
	 * @param start - the node to start at
	 * @param option - options on how to print the tree
	 * @return
	 */
	public static <N> String printTree(TreeNode<N> start, PrintOption option){
		
		StringBuffer buffer = new StringBuffer();
		
		switch(option){
		
			case HTML:
				printHtml(start, "", true, buffer);
				break;
			
			case TERMINAL:
				printTerminal(start, "", true, buffer);
				break;
				
			default:
				break;
		
		}
		
		return buffer.toString();
		
	}
	
	/**
	 * Print for html page
	 * 
	 * @param node
	 * @param linePrefix
	 * @param isTail
	 * @param buffer
	 */
	private static <N> void printHtml(TreeNode<N> node, String linePrefix, boolean isTail, StringBuffer buffer){
		
		buffer.append(linePrefix + (isTail ? "|__" : "|__") + node.getData().toString() + "<br>");
		
		if(node.hasChildren()){
			
			List<TreeNode<N>> children = node.getChildren();
		
			for(int i = 0; i < children.size() - 1; i++) {
				printHtml(children.get(i), linePrefix + (isTail ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : "|&nbsp;&nbsp;&nbsp;&nbsp;"), false, buffer);
			}
			if(node.getChildren().size() >= 1){
				printHtml(children.get(children.size() - 1), linePrefix + (isTail ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : "|&nbsp;&nbsp;&nbsp;&nbsp;"), true, buffer);
			}
		}
		
	}
	
	/**
	 * Print for terminal
	 * 
	 * @param node
	 * @param linePrefix
	 * @param isTail
	 * @param buffer
	 */
	private static <N> void printTerminal(TreeNode<N> node, String linePrefix, boolean isTail, StringBuffer buffer){
		
		buffer.append(linePrefix + (isTail ? "|__" : "|__") + node.getData().toString() + System.getProperty("line.separator"));
		
		if(node.hasChildren()){
			
			List<TreeNode<N>> children = node.getChildren();
		
			for(int i = 0; i < children.size() - 1; i++) {
				printTerminal(children.get(i), linePrefix + (isTail ? "   " : "|  "), false, buffer);
			}
			if(node.getChildren().size() >= 1){
				printTerminal(children.get(children.size() - 1), linePrefix + (isTail ? "   " : "|  "), true, buffer);
			}
		}
		
	}	

}
