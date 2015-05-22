package org.lenzi.fstore.repository.tree.query;

import org.lenzi.fstore.repository.exception.DatabaseException;

/**
 * Defines queries & methods to be used by our tree repository.
 * 
 * @author sal
 *
 * @param <N>
 */
public interface TreeQueryRepository {

	public String getRepositoryName();
	
	// execute query to get next value from a sequence
	public long getSequenceValue(String nativeSequenceQuery) throws DatabaseException;
	
	// query for getting next id from node id sequence
	public String getSqlQueryNodeIdSequence();
	
	// query for getting next id from link id sequence
	public String getSqlQueryLinkIdSequence();
	
	// query for getting next id from prune id sequence
	public String getSqlQueryPruneIdSequence();
	
	// query for getting next id from tree id sequence
	public String getSqlQueryTreeIdSequence();
	
	// HQL_NODE_WITH_PARENT_CLOSURE
	public String getHqlQueryNodeWithParentClosure();
	
	// HQL_NODE_WITH_CHILDREN_CLOSURE
	public String getHqlQueryNodeWithChildClosure();
	
	// HQL_CLOSURE_BY_NODE_ID
	public String getHqlQueryClosureByNodeId();
	
	// SQL_INSERT_MAKE_PARENT
	public String getSqlQueryInsertMakeParent();
	
	// SQL_INSERT_PRUNE_TREE
	public String getSqlQueryInsertPruneTree();
	
	// SQL_INSERT_PRUNE_CHILDREN
	public String getSqlQueryInsertPruneChildren();	
	
	// SQL_DELETE_FS_CLOSURE_PRUNE
	public String getSqlQueryDeleteFsClosurePrune();
	
}
