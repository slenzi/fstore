package org.lenzi.fstore.core.model.util;

/**
 * Provides specification for copying nodes.
 * 
 * @author sal
 *
 * @param <T>
 */
public interface NodeCopier<N> {

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
	public N copy(N node);
	
}
