package org.lenzi.fstore.model.util;

import org.lenzi.fstore.repository.model.DBNode;

/**
 * Provides specification for copying nodes.
 * 
 * @author sal
 *
 * @param <T>
 */
public interface NodeCopier {

	/**
	 * Get the fully qualified name (package + name) for the node being copied.
	 * 
	 * @return
	 */
	public String getCanonicalName();

	/**
	 * Copy a node
	 * 
	 * @param node - The node to copy
	 * @return A new copy of the node
	 */
	public DBNode copy(DBNode node);
	
}
