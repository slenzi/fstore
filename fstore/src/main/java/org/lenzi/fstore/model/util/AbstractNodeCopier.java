package org.lenzi.fstore.model.util;

import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;

/**
 * Base class for copying DbNode objects. This class copies the base attributes. Extend
 * this class for copying other type of nodes which extend from DbNode.
 * 
 * @author sal
 *
 * @param <N> A node object which extends DbNode
 */
public abstract class AbstractNodeCopier<N extends FSNode> implements NodeCopier<N> {

	@InjectLogger
	private Logger logger;		
	
	public AbstractNodeCopier() {

	}
	
	@SuppressWarnings("rawtypes")
	protected N createNew() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = getCanonicalName();
		Class clazz = Class.forName(name);
		N node = (N) clazz.newInstance();
		return node;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public N copy(N node) {

		logger.info("Copying node of type => " + getCanonicalName());
		
		if(node == null){
			return null;
		}
		
		N newObject = null;
		
		try {
			newObject = (N)createNew();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		// copy all attributes from parent class
		newObject.setNodeId(node.getNodeId());
		newObject.setNodeType(node.getNodeType());
		newObject.setName(node.getName());
		newObject.setParentNodeId(node.getParentNodeId());
		newObject.setDateCreated(node.getDateCreated());
		newObject.setDateUpdated(node.getDateUpdated());
		newObject.setChildClosure(node.getChildClosure());
		newObject.setParentClosure(node.getParentClosure());
		
		// copy all attributes from users child class.
		newObject = doCopyWork( newObject, node);
		
		return newObject;		
		
	}
	
	/**
	 * Return the fully qualified name (package + name) of the class being copied.
	 */
	public abstract String getCanonicalName();
	
	/**
	 * Override to copy over attributes from child implementation.
	 * 
	 * @param copyTo - the new copy
	 * @param copyFrom - the node being copied
	 * @return A reference to the new copy
	 */
	public abstract N doCopyWork(N copyTo, N copyFrom);

}
