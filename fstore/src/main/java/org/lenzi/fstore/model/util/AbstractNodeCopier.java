package org.lenzi.fstore.model.util;

import org.lenzi.fstore.repository.model.DbNode;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;

/**
 * Base class for copying DbNode objects. This class copies the base attributes. Extend
 * this class for copying other type of nodes which extend from DbNode.
 * 
 * @author sal
 *
 * @param <T> A node object which extends DbNode
 */
public abstract class AbstractNodeCopier<N extends DbNode> implements NodeCopier {

	@InjectLogger
	private Logger logger;		
	
	public AbstractNodeCopier() {

	}
	
	@SuppressWarnings("rawtypes")
	protected DbNode createNew() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = getCanonicalName();
		Class clazz = Class.forName(name);
		DbNode node = (DbNode) clazz.newInstance();
		return node;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public DbNode copy(DbNode node) {

		logger.info("Copying node of type => " + getCanonicalName());
		
		if(node == null){
			return null;
		}
		
		DbNode newObject = null;
		
		try {
			newObject = (N)createNew();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		newObject.setNodeId(node.getNodeId());
		newObject.setNodeType(node.getNodeType());
		newObject.setParentNodeId(node.getParentNodeId());
		newObject.setDateCreated(node.getDateCreated());
		newObject.setDateUpdated(node.getDateUpdated());
		newObject.setChildClosure(node.getChildClosure());
		newObject.setParentClosure(node.getParentClosure());
		
		newObject = doCopyWork( (N)newObject, (N)node);
		
		return newObject;		
		
	}
	
	public abstract String getCanonicalName();
	
	public abstract N doCopyWork(N newNode, N node);

}
