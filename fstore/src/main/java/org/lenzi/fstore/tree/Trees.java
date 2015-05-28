package org.lenzi.fstore.tree;

import org.lenzi.fstore.repository.exception.DatabaseException;

public final class Trees {


	// http://en.wikipedia.org/wiki/Tree_traversal
	public enum WalkOption {
		
		// bottom-up
		POST_ORDER_TRAVERSAL,
		
		// top-down
		PRE_ORDER_TRAVERSAL
		
	}
	
	/**
	 * Walk a tree
	 * 
	 * @param start
	 * @param visitor
	 * @param option
	 */
	public static void walkTree(TreeNode<?> start, TreeNodeVisitor<? super TreeNode<?>> visitor, WalkOption walkOption){
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
	
	private static void postOrderTraversal(TreeNode<?> node, TreeNodeVisitor<? super TreeNode<?>> visitor) {
		if(node.hasChildren()){
			for(TreeNode<?> childNode : node.getChildren()){
				postOrderTraversal(childNode, visitor);
			}
			visitor.visitNode(node);
		}else{
			visitor.visitNode(node);
		}
	}

}
