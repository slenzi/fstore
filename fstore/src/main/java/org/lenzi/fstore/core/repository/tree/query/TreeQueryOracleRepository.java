/**
 * 
 */
package org.lenzi.fstore.core.repository.tree.query;

import java.math.BigDecimal;

import javax.persistence.Query;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Oracle specific queries used by tree repository.
 * 
 * @author sal
 */
@Transactional(propagation=Propagation.REQUIRED)
public class TreeQueryOracleRepository extends AbstractRepository implements TreeQueryRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6094404794215950578L;

	/**
	 * Select next available prune ID from sequence. Used in FS_PRUNE table.
	 */
	private String SQL_SELECT_NEXT_PRUNE_ID_SEQUENCE_VALUE = "SELECT FS_PRUNE_ID_SEQUENCE.nextval from DUAL";
	
	/**
	 * Select next available node ID from sequence. Used in FS_NODE table.
	 */
	private String SQL_SELECT_NEXT_NODE_ID_SEQUENCE_VALUE = "SELECT FS_NODE_ID_SEQUENCE.nextval from DUAL";
	
	/**
	 * Select next available link ID from sequence. Used in FS_CLOSURE table.
	 */
	private String SQL_SELECT_NEXT_LINK_ID_SEQUENCE_VALUE = "SELECT FS_LINK_ID_SEQUENCE.nextval from DUAL";
	
	/**
	 * Select next available link ID from sequence. Used in FS_TREE table.
	 */
	private String SQL_SELECT_NEXT_TREE_ID_SEQUENCE_VALUE = "SELECT FS_TREE_ID_SEQUENCE.nextval from DUAL";	
	
	/**
	 * Fetches a node by ID, and includes the closure entries with the parent node data.
	 */
	private String HQL_NODE_WITH_PARENT_CLOSURE =
		"select " +
		"	distinct n " +
		"from FSNode as n " +
		"inner join fetch n.parentClosure parentClosure " +
		"inner join fetch parentClosure.parentNode parentNode " +
		"where " +
		"	n.nodeId = :nodeId " +
		"	and parentClosure.depth >= 0";
	
	/**
	 * Fetches a node by ID, and includes the closure entries with the child node data.
	 */
	private String HQL_NODE_WITH_CHILDREN_CLOSURE =
		"select " +
		"	distinct n " +
		"from FSNode as n " +
		"inner join fetch n.childClosure childClosure " +
		"inner join fetch childClosure.childNode childNode " +
		"where " +
		"	n.nodeId = :nodeId " +
		"	and childClosure.depth >= 0";	
	
	/**
	 * For any given node in the tree, select all depth-1 parent-child links under that node. This will give
	 * you all the necessary information to build a tree model.
	 * 
	 * @param1 The ID of the starting/parent node.
	 * @param2 The ID of the starting/parent node.
	 */		
	private String HQL_CLOSURE_BY_NODE_ID =
		"select c " +
		"from FSClosure as c " +  
		"join fetch c.childNode child " +  
		"join fetch c.parentNode parent " +
		"where " +
		"	child.nodeId in ( " +
		"		select cc.childNodeId " +
		"		from FSClosure cc " +
		"		where cc.parentNodeId = ?1 " +
		"		and cc.depth >= 0 " +
		"	) " +
		"and c.depth = 1 " + 
		"or (c.depth = 0 and c.parentNodeId = ?2) " +
		"order by c.depth asc";
	
	/**
	 * Make node N1 a parent of node N2.
	 * 
	 * LINK_ID primary key column is populated automatically from sequence FS_LINK_ID_SEQUENCE
	 * 
	 * @param1 Node ID for N1
	 * @param2 Node ID for N2
	 */	
	private String SQL_INSERT_MAKE_PARENT =
		"insert " +
		"	into fs_closure (link_id,parent_node_id, child_node_id, depth) " + 
		"select " +
		"	FS_LINK_ID_SEQUENCE.nextval, p.parent_node_id, c.child_node_id, (p.depth + c.depth + 1) as depth " +
		"from " +
		"	fs_closure p, fs_closure c " +
		"where " +
		"	p.child_node_id = ? and c.parent_node_id = ?";	
	
	/**
	 * Add nodes to prune table. These are the nodes to delete during a delete operation
	 * 
	 * This function selects the current value from the fs_prune_id sequence. You must first call
	 * fs_prune_id_sequence.nextval in the same session to get the next value before currval will work.
	 * 
	 * @param1 The ID of the parent node to prune. This node, as well as all nodes under it will
	 * be deleted.
	 */
	private String SQL_INSERT_PRUNE_TREE =
		"insert into fs_prune " +
		"select FS_PRUNE_ID_SEQUENCE.currval as prune_id, child_to_delete from ( " +
		"  select distinct c.child_node_id as child_to_delete  " +
		"  from fs_closure c  " +
		"  inner join fs_node n  " +
		"  on c.child_node_id = n.node_id " + 
		"  where c.parent_node_id = ? " +
		")";
	
	/**
	 * Add nodes to prune table. These are the nodes to delete during a delete operation
	 * 
	 * This function selects the current value from the fs_prune_id sequence. You must first call
	 * fs_prune_id_sequence.nextval in the same session to get the next value before currval will work.
	 * 
	 * @param1 The ID of the parent node. The node will remain, but all children data will be
	 * deleted from the closure table.
	 */
	private String SQL_INSERT_PRUNE_CHILDREN =
		"insert into fs_prune " +
		"select FS_PRUNE_ID_SEQUENCE.currval as prune_id, child_to_delete from ( " +
		"  select distinct c.child_node_id as child_to_delete  " +
		"  from fs_closure c  " +
		"  inner join fs_node n  " +
		"  on c.child_node_id = n.node_id " + 
		"  where c.parent_node_id = ? " +
		"  and c.depth > 0 " +
		")";
	
	/**
	 * Prune data from the closure table.
	 * 
	 * @param1 The unique prune ID which links to all the nodes you want to prune.
	 */
	private String SQL_DELETE_FS_CLOSURE_PRUNE =
		"delete " +
		"  fs_closure " +
		"where link_id in ( " +
		"  select l.link_id " +
		"  from fs_closure p " +
		"  inner join fs_closure l " +
		"    on p.parent_node_id = l.parent_node_id " +
		"  inner join fs_closure c " +
		"    on c.child_node_id = l.child_node_id " +
		"  inner join fs_closure to_delete " +
		"  on " +
		"    p.child_node_id = to_delete.parent_node_id " +
		"    and c.parent_node_id = to_delete.child_node_id " +
		"    and to_delete.depth < 2 " +
		"  inner join " +
		"  ( " +
		/* select the IDs of the node we are deleting from our prune table */
		"        select p.node_id as child_to_delete " +
		"        from fs_prune p " +
		"        where p.prune_id = ?  " +
		"  ) pruneTable " +
		"  on " +
		"    ( " +
		/* for all nodes in the prune table, delete any parent node links and and child node links */
		"      to_delete.parent_node_id = pruneTable.child_to_delete " +
		"      or " +
		"      to_delete.child_node_id = pruneTable.child_to_delete " +
		"    ) " +
		")";	
	
	/**
	 * 
	 */
	public TreeQueryOracleRepository() {
		
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getRepositoryName()
	 */
	@Override
	public String getRepositoryName() {
		return this.getClass().getName();
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSequenceValue(java.lang.String)
	 */
	@Override
	public long getSequenceValue(String nativeSequenceQuery) throws DatabaseException {

		Query query = getEntityManager().createNativeQuery(nativeSequenceQuery);
		BigDecimal result = (BigDecimal)getSingleResult(query);
		long sequenceId = result.longValue();
		return sequenceId;			
		
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryNodeIdSequence()
	 */
	@Override
	public String getSqlQueryNodeIdSequence() {
		return SQL_SELECT_NEXT_NODE_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryLinkIdSequence()
	 */
	@Override
	public String getSqlQueryLinkIdSequence() {
		return SQL_SELECT_NEXT_LINK_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryPruneIdSequence()
	 */
	@Override
	public String getSqlQueryPruneIdSequence() {
		return SQL_SELECT_NEXT_PRUNE_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryTreeIdSequence()
	 */
	@Override
	public String getSqlQueryTreeIdSequence() {
		return SQL_SELECT_NEXT_TREE_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getHqlQueryNodeWithParentClosure()
	 */
	@Override
	public String getHqlQueryNodeWithParentClosure() {
		return HQL_NODE_WITH_PARENT_CLOSURE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getHqlQueryNodeWithChildClosure()
	 */
	@Override
	public String getHqlQueryNodeWithChildClosure() {
		return HQL_NODE_WITH_CHILDREN_CLOSURE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getHqlQueryClosureByNodeId()
	 */
	@Override
	public String getHqlQueryClosureByNodeId() {
		return HQL_CLOSURE_BY_NODE_ID;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryInsertMakeParent()
	 */
	@Override
	public String getSqlQueryInsertMakeParent() {
		return SQL_INSERT_MAKE_PARENT;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryInsertPruneTree()
	 */
	@Override
	public String getSqlQueryInsertPruneTree() {
		return SQL_INSERT_PRUNE_TREE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryInsertPruneChildren()
	 */
	@Override
	public String getSqlQueryInsertPruneChildren() {
		return SQL_INSERT_PRUNE_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.tree.query.TreeQueryRepository#getSqlQueryDeleteFsClosurePrune()
	 */
	@Override
	public String getSqlQueryDeleteFsClosurePrune() {
		return SQL_DELETE_FS_CLOSURE_PRUNE;
	}

}
