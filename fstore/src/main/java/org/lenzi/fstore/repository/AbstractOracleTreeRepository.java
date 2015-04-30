package org.lenzi.fstore.repository;

import java.math.BigDecimal;

import javax.persistence.Query;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Code for maintaining tree structures within an Oracle database using a closure table.
 * 
 * @author sal
 *
 * @param <N>
 */
@Transactional(propagation=Propagation.REQUIRED)
public abstract class AbstractOracleTreeRepository<N extends FSNode> extends AbstractTreeRepository<N> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7800560911360236185L;

	@InjectLogger
	private Logger logger;
	
	private String HQL_GET_NODE_BY_ID =
		"select n from FSNode n where n.nodeId = :nodeId";	
	
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
	 * Will delete the node from the FS_NODE table, as well as all children
	 * nodes from the FS_NODE table.
	 * 
	 * You still need to delete the parent-child links in the FS_CLOSURE table.
	 * 
	 * @param1 The ID of the node to delete
	 * @deprecated - replaced with a jpa criteria query
	 */
	private String SQL_DELETE_FS_NODE_PRUNE_TREE =
		"delete " +
		"from fs_node n " +
		"where n.node_id in ( " +
		"  select c.child_node_id " +
		"  from fs_closure c " +
		"  where c.parent_node_id = ? " +
		")";
	
	/**
	 * Will delete all children nodes under the specified node, but not
	 * the node itself, from the FS_NODE table.
	 * 
	 * You still need to delete the parent-child links in the FS_CLOSURE table.
	 * 
	 * @param1 The ID of the parent node for which you want to delete all children of. The
	 * parent node itrself will not be deleted.
	 */
	private String SQL_DELETE_FS_NODE_PRUNE_CHILDREN =
		"delete " +
		"from fs_node n " +
		"where n.node_id in ( " +
		"  select c.child_node_id " +
		"  from fs_closure c " +
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
	
	private String HQL_GET_TREE_BY_ID =
		"select t from FSTree as t " +
		"left join fetch t.rootNode " +
		"where t.treeId = :treeid";
	
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
	
	
	public AbstractOracleTreeRepository() {
		
	}
	
	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getNativeNodeIdSequenceQuery()
	 */
	@Override
	protected String getSqlQueryNodeIdSequence() {
		return SQL_SELECT_NEXT_NODE_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getNativeLinkIdSequenceQuery()
	 */
	@Override
	protected String getSqlQueryLinkIdSequence() {
		return SQL_SELECT_NEXT_LINK_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getNativePruneIdSequenceQuery()
	 */
	@Override
	protected String getSqlQueryPruneIdSequence() {
		return SQL_SELECT_NEXT_PRUNE_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getNativeTreeIdSequenceQuery()
	 */
	@Override
	protected String getSqlQueryTreeIdSequence() {
		return SQL_SELECT_NEXT_TREE_ID_SEQUENCE_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getHqlQueryNodeById()
	 */
	@Override
	protected String getHqlQueryNodeById() {
		return HQL_GET_NODE_BY_ID;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getHqlQueryTreeById()
	 */
	@Override
	protected String getHqlQueryTreeById() {
		return HQL_GET_TREE_BY_ID;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getHqlQueryNodeWithParentClosure()
	 */
	@Override
	protected String getHqlQueryNodeWithParentClosure() {
		return HQL_NODE_WITH_PARENT_CLOSURE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getHqlQueryNodeWithChildClosure()
	 */
	@Override
	protected String getHqlQueryNodeWithChildClosure() {
		return HQL_NODE_WITH_CHILDREN_CLOSURE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getHqlQueryClosureByNodeId()
	 */
	@Override
	protected String getHqlQueryClosureByNodeId() {
		return HQL_CLOSURE_BY_NODE_ID;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getSqlQueryInsertMakeParent()
	 */
	@Override
	protected String getSqlQueryInsertMakeParent() {
		return SQL_INSERT_MAKE_PARENT;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getSqlQueryInsertPruneTree()
	 */
	@Override
	protected String getSqlQueryInsertPruneTree() {
		return SQL_INSERT_PRUNE_TREE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getSqlQueryInsertPruneChildren()
	 */
	@Override
	protected String getSqlQueryInsertPruneChildren() {
		return SQL_INSERT_PRUNE_CHILDREN;
	}

	/**
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getSqlQueryDeleteFsNodePruneTree()
	 * @deprecated - replaced with a jpa criteria query
	 */
	@Override
	protected String getSqlQueryDeleteFsNodePruneTree() {
		return SQL_DELETE_FS_NODE_PRUNE_TREE;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getSqlQueryDeleteFsNodePruneChildren()
	 */
	@Override
	protected String getSqlQueryDeleteFsNodePruneChildren() {
		return SQL_DELETE_FS_NODE_PRUNE_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.AbstractClosureRepository#getSqlQueryDeleteFsClosurePrune()
	 */
	@Override
	protected String getSqlQueryDeleteFsClosurePrune() {
		return SQL_DELETE_FS_CLOSURE_PRUNE;
	}
	
	/**
	 * Get the repository name
	 */
	@Override
	public String getRepositoryName() {
		
		logger.info(AbstractOracleTreeRepository.class.getName() + "getRepositoryName() called");
		
		return AbstractOracleTreeRepository.class.getName();
	}	

	/**
	 * Get value from sequence
	 * 
	 * @param nativeSequenceQuery - a native SQL query which returns either nextval or currval from a sequence
	 * @see org.lenzi.fstore.repository.AbstractTreeRepository#getSequenceVal(java.lang.String)
	 */
	@Override
	protected long getSequenceVal(String nativeSequenceQuery) throws DatabaseException {

		Query queryPruneSequence = getEntityManager().createNativeQuery(nativeSequenceQuery);
		BigDecimal result = (BigDecimal)getSingleResult(queryPruneSequence);
		long sequenceId = result.longValue();
		return sequenceId;			
		
	}

}
