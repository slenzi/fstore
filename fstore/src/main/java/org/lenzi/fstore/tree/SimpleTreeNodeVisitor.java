package org.lenzi.fstore.tree;

import org.lenzi.fstore.repository.model.impl.FSNode;

/**
 * A simple tree node visitor which visits each node N and prints the name.
 * 
 * @author slenzi
 *
 * @param <N> A node which extends from FSNode.
 */
public class SimpleTreeNodeVisitor<N extends FSNode<N>> implements TreeNodeVisitor<TreeNode<N>> {

	public SimpleTreeNodeVisitor() {
		
	}

	@Override
	public void visitNode(TreeNode<N> node) {
		
		System.out.println(node.getData().getName());

	}

}
