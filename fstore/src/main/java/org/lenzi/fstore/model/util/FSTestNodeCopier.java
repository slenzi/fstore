/**
 * 
 */
package org.lenzi.fstore.model.util;

import org.lenzi.fstore.repository.model.impl.FSTestNode;
import org.springframework.stereotype.Service;

/**
 * Performs work for copying a FSTestNode object.
 * 
 * @author sal
 */
@Service
public class FSTestNodeCopier extends AbstractNodeCopier<FSTestNode> {	
	
	@Override
	public String getCanonicalName() {
		
		return FSTestNode.class.getCanonicalName();
		
	}

	@Override
	public FSTestNode doCopyWork(FSTestNode copyTo, FSTestNode copyFrom) {
		
		copyTo.setTestValue(copyFrom.getTestValue());
		
		return copyTo;
	}

}
