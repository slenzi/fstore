package org.lenzi.fstore.repository.tree.visitor;

import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.tree.TreeNode;
import org.lenzi.fstore.tree.TreeNodeVisitException;
import org.lenzi.fstore.tree.TreeNodeVisitor;

/**
 * A simple tree node visitor which visits each node N and prints the name.
 * 
 * @author slenzi
 *
 * @param <N> A node which extends from FSNode.
 */
public class SimpleFSNodeVisitor<N extends FSNode<N>> implements TreeNodeVisitor<N> {

	public SimpleFSNodeVisitor() {
		
	}

	@Override
	public void visitNode(TreeNode<N> node) throws TreeNodeVisitException {
		
		System.out.println("visit => " + node.getData().toString());
		
	}



}
