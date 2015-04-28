/**
 * 
 */
package org.lenzi.fstore.model.util;

import org.lenzi.fstore.repository.model.impl.FSTestNode;

/**
 * Performs work for copying a FSTestNode object.
 * 
 * @author sal
 */
public class FSTestNodeCopier extends AbstractNodeCopier<FSTestNode> {	
	
	@Override
	public String getCanonicalName() {
		
		return FSTestNode.class.getCanonicalName();
		
	}

	@Override
	public FSTestNode doCopyWork(FSTestNode newNode, FSTestNode node) {
		
		newNode.setTestValue(node.getTestValue());
		
		return newNode;
	}

}
