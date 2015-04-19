package org.lenzi.fstore.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Named;
import javax.persistence.Query;

import org.lenzi.filestore.util.DateUtil;
import org.lenzi.filestore.util.LogUtil;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.FSClosure;
import org.lenzi.fstore.repository.model.FSNode;
import org.lenzi.fstore.repository.model.FSTree;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Code for maintaining tree structures within an Oracle database.
 * 
 * FS_NODE - master list of all nodes for all trees.
 * 
 * FS_CLOSURE - maintains parent-child relationship for all nodes.
 * 
 * FS_PRUNE - used during delete operations (i.e. delete node, delete children, and move node.)
 * 
 * @author sal
 */
@Named
@Repository
@Transactional
public class PostgresClosureRepository extends AbstractRepository implements ClosureRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7800560911360236185L;

	@InjectLogger
	private Logger logger;
	
	/**
	 * For any given node in the tree, select all depth-1 parent-child links under that node. This will give
	 * you all the necessary information to build a tree model.
	 * 
	 * @param1 The ID of the starting/parent node.
	 * @param2 The ID of the starting/parent node.
	 */		
	private String HQL_TREE_BY_NODE_ID_ALT =
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
	private String HQL_NODE_WITH_CHILDREN =
		"select " +
		"	distinct n " +
		"from FSNode as n " +
		"inner join fetch n.childClosure childClosure " +
		"inner join fetch childClosure.childNode childNode " +
		"where " +
		"	n.nodeId = :nodeId " +
		"	and childClosure.depth > 0";

	/**
	 * Fetches a node by ID, and includes the closure entries with the parent node data.
	 */
	private String HQL_NODE_WITH_PARENT =
		"select " +
		"	distinct n " +
		"from FSNode as n " +
		"inner join fetch n.parentClosure parentClosure " +
		"inner join fetch parentClosure.parentNode parentNode " +
		"where " +
		"	n.nodeId = :nodeId " +
		"	and parentClosure.depth > 0";
	
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
	
	public PostgresClosureRepository() {
		
	}
	
	/**
	 * Add new tree to the database.
	 * 
	 * @param treeName - name of the
	 * @param treeDesc - description
	 * @param rootNodeName
	 * @return
	 * @throws DatabaseException
	 */
	public FSTree addTree(String treeName, String treeDesc, String rootNodeName) throws DatabaseException {
		
		FSNode rootNode = addNode(0L, rootNodeName);
		Long rootNodeId = rootNode.getNodeId();
		
		// Get next available node id from sequence
		Query queryPruneSequence = getEntityManager().createNativeQuery(SQL_SELECT_NEXT_TREE_ID_SEQUENCE_VALUE);
		BigDecimal result=(BigDecimal)queryPruneSequence.getSingleResult();
		long treeId = result.longValue();			
	
		Timestamp now = DateUtil.getCurrentTime();
		
		FSTree tree = new FSTree();
		tree.setTreeId(treeId);
		tree.setName(treeName);
		tree.setDescription(treeDesc);
		tree.setDateCreated(now);
		tree.setDateUpdated(now);
		tree.setRootNodeId(rootNodeId);
		tree.setRootNode(rootNode); // adds node to database
		
		// save node to database
		//getEntityManager().persist(tree);
		persist(tree);
		
		getEntityManager().flush();
		getEntityManager().clear();		
		
		return tree;
	}
	
	/**
	 * Fetches all child node data from the closure table for the specified parent node. This
	 * will give you all necessary data to build a tree data structure.
	 * 
	 * @param nodeId The root/parent node Id.
	 * @return A list of FSClosure objects with the child node data populated.
	 * @throws EcogDatabaseException
	 */
	public List<FSClosure> getClosureByNodeId(Long nodeId) throws DatabaseException {
		
		logger.info("Getting closure list for node id => " + nodeId);
		
		List<FSClosure> results = null;
		Query query = null;
		try {
			query = getEntityManager().createQuery(HQL_TREE_BY_NODE_ID_ALT);
			query.setParameter(1, nodeId);
			query.setParameter(2, nodeId);			
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}
		results = getResultList(query);
		return results;
	}
	
	/**
	 * Add a new node.
	 * 
	 * @param parentNodeId - The parent node ID for the new node. If this new node has
	 * no parent (it's at the root level) then set the parent node ID to 0.
	 * @param name - The node name. This can be anything. (e.g., folder name, file name, path, etc.)
	 * @return - the ID of the new node.
	 */
	public FSNode addNode(Long parentNodeId, String name) throws DatabaseException{
		
		// Get next available node id from sequence
		Query queryPruneSequence = getEntityManager().createNativeQuery(SQL_SELECT_NEXT_NODE_ID_SEQUENCE_VALUE);
		BigDecimal result=(BigDecimal)queryPruneSequence.getSingleResult();
		long nodeId = result.longValue();			
		
		// create new node
		FSNode newNode = new FSNode();
		newNode.setNodeId(nodeId);
		newNode.setParentNodeId(parentNodeId);
		newNode.setName(name);
		Timestamp now = DateUtil.getCurrentTime();
		newNode.setDateCreated(now);
		newNode.setDateUpdated(now);
		
		// save node to database
		getEntityManager().persist(newNode);
		
		// Get next available link id from sequence
		Query queryLinkIdSequence = getEntityManager().createNativeQuery(SQL_SELECT_NEXT_LINK_ID_SEQUENCE_VALUE);
		result = (BigDecimal)queryLinkIdSequence.getSingleResult();
		long linkId = result.longValue();		
		
		// add depth-0 self link to closure table
		FSClosure selfLink = new FSClosure();
		selfLink.setLinkId(linkId);
		selfLink.setChildNodeId(newNode.getNodeId());
		selfLink.setParentNodeId(newNode.getNodeId());
		selfLink.setDepth(0);
		
		// save closure self link to database
		getEntityManager().persist(selfLink);
		
		// necessary?
		getEntityManager().flush();
		
		// add parent-child links to closure table
		Query queryInsertLinks = getEntityManager().createNativeQuery(SQL_INSERT_MAKE_PARENT);
		queryInsertLinks.setParameter(1,parentNodeId);
		queryInsertLinks.setParameter(2,newNode.getNodeId());		
		try {
			executeUpdate(queryInsertLinks);
		} catch (DatabaseException e) {
			logger.error("Failed to add parent-child links FS_CLOSURE for node "
					+ "(id = " + newNode.getNodeId() + ", name = " + newNode.getName() + "). " +  e.getMessage(),e);
			e.printStackTrace();
			return null;
		}
		
		
		// these two calls are required ( definitely the clear() call )
		getEntityManager().flush();
		getEntityManager().clear();
		
		return newNode;
	}
	
	/**
	 * Re-add node. Called during move operation. Uses merge() because we re-use the same node ids.
	 * 
	 * @param nodeId
	 * @param parentNodeId - 
	 * @param name
	 * @param dateCreated
	 * @param dateUpdated
	 */
	private FSNode reAddNode(Long nodeId, Long parentNodeId, String name, Timestamp dateCreated, Timestamp dateUpdated) throws DatabaseException{
		
		// create new node
		FSNode reAddNode = new FSNode();
		reAddNode.setNodeId(nodeId);
		reAddNode.setParentNodeId(parentNodeId);
		reAddNode.setName(name);
		reAddNode.setDateCreated(dateCreated);
		reAddNode.setDateUpdated(dateUpdated);
		
		// re-add node to database
		getEntityManager().merge(reAddNode);
		
		// Get next available link id from sequence
		Query queryLinkIdSequence = getEntityManager().createNativeQuery(SQL_SELECT_NEXT_LINK_ID_SEQUENCE_VALUE);
		BigDecimal result = (BigDecimal)queryLinkIdSequence.getSingleResult();
		long linkId = result.longValue();		
		
		// add depth-0 self link to closure table
		FSClosure selfLink = new FSClosure();
		selfLink.setLinkId(linkId);
		selfLink.setChildNodeId(nodeId);
		selfLink.setParentNodeId(nodeId);	
		selfLink.setDepth(0);
		
		// save closure self link to database
		getEntityManager().persist(selfLink);
		
		// necessary?
		getEntityManager().flush();
		
		// add parent-child links to closure table
		Query queryInsertLinks = getEntityManager().createNativeQuery(SQL_INSERT_MAKE_PARENT);
		queryInsertLinks.setParameter(1,parentNodeId);
		queryInsertLinks.setParameter(2,nodeId);		
		try {
			executeUpdate(queryInsertLinks);
		} catch (DatabaseException e) {
			logger.error("Failed to add parent-child links FS_CLOSURE for node "
					+ "(id = " + nodeId + ", name = " + name + "). " +  e.getMessage(),e);
			e.printStackTrace();
			return null;
		}
		
		// these two calls are required ( definitely the clear() call )
		getEntityManager().flush();
		getEntityManager().clear();
		
		return reAddNode;		
		
	}
	
	/**
	 * Remove node, and all children.
	 * 
	 * @param nodeId - The ID of the node to remove.
	 */
	public void removeNode(Long nodeId) throws DatabaseException{
		
		logger.debug("remove node " + nodeId);
		
		removeNode(nodeId, true, true);			
		
	}
	
	/**
	 * Remove node.
	 * 
	 * @param nodeId The ID of the node to remove.
	 * @param nodeTable - True to remove the node and all child nodes from the FS_NODE table, false otherwise.
	 * @param pruneTable - True to remove all closure entries from the FS_CLOSURE table, false otherwise.
	 */
	private void removeNode(Long nodeId, boolean nodeTable, boolean pruneTable) throws DatabaseException {
		
		logger.debug("remove node " + nodeId + ", nodeTable => " + nodeTable + ", pruneTable => " + pruneTable);
		
		long pruneId = 0;
		
		if(pruneTable){
			//
			// Get next available prune id from sequence
			//
			Query queryPruneSequence = getEntityManager().createNativeQuery(SQL_SELECT_NEXT_PRUNE_ID_SEQUENCE_VALUE);
			BigDecimal result=(BigDecimal)queryPruneSequence.getSingleResult();
			pruneId = result.longValue();		
			
			//
			// Add list of nodes to delete to our prune table
			//
			Query populatePrune = getEntityManager().createNativeQuery(SQL_INSERT_PRUNE_TREE);
			populatePrune.setParameter(1,nodeId);
			try {
				executeUpdate(populatePrune);
			} catch (DatabaseException e) {
				logger.error("Failed populate prune table with list of nodes to delete. " +  e.getMessage(),e);
				e.printStackTrace();
				return;
			}
			logger.debug("Added list of nodes to delete to prune table under prune id " + pruneId);
		}
		
		// this part must happen in the middle. it relies on data in the closure table
		if(nodeTable){
			//
			// Remove node, plus children, from node table. 
			//
			Query queryDeleteFsNode = getEntityManager().createNativeQuery(SQL_DELETE_FS_NODE_PRUNE_TREE);
			queryDeleteFsNode.setParameter(1,nodeId);
			try {
				executeUpdate(queryDeleteFsNode);
			} catch (DatabaseException e) {
				logger.error("Failed to remove node " + nodeId + ", plus all children, from FS_NODE. " +  e.getMessage(),e);
				e.printStackTrace();
				return;
			}
			logger.debug("Deleted node " + nodeId + " from the node table.");
		}
		
		if(pruneTable){
			//
			// Remove node depth-0 self link, plus all children links, from closure table.
			//
			// This query uses our prune table. Pass the prune ID which links to all the nodes to prune.
			//
			Query queryDeleteFsClosure = getEntityManager().createNativeQuery(SQL_DELETE_FS_CLOSURE_PRUNE);
			queryDeleteFsClosure.setParameter(1,pruneId);
			try {
				executeUpdate(queryDeleteFsClosure);
			} catch (DatabaseException e) {
				logger.error("Failed to remove node " + nodeId + ", plus all children links, from FS_CLOSURE. " +  e.getMessage(),e);
				e.printStackTrace();
				return;
			}
			logger.debug("Deleted node " + nodeId + " from the closure table.");
		}
		
	}	
	
	/**
	 * Remove all children from a node. Node itself is not removed.
	 * 
	 * @param nodeId - The ID of the node
	 */
	public void removeChildren(Long nodeId) throws DatabaseException {
		
		logger.debug("remove children of " + nodeId);
		
		//
		// Get next available prune id from sequence.
		//
		Query queryPruneSequence = getEntityManager().createNativeQuery(SQL_SELECT_NEXT_PRUNE_ID_SEQUENCE_VALUE);
		BigDecimal result=(BigDecimal)queryPruneSequence.getSingleResult();
		long pruneId = result.longValue();		
		
		//
		// Add list of nodes to delete to our prune table
		//
		Query populatePrune = getEntityManager().createNativeQuery(SQL_INSERT_PRUNE_CHILDREN);
		populatePrune.setParameter(1,nodeId);
		try {
			executeUpdate(populatePrune);
		} catch (DatabaseException e) {
			logger.error("Failed populate prune table with list of nodes to delete. " +  e.getMessage(),e);
			e.printStackTrace();
			return;
		}
		logger.debug("Added list of nodes to delete to prune table under prune id " + pruneId);
		
		//
		// Remove children from node table. 
		//
		Query queryDeleteFsNode = getEntityManager().createNativeQuery(SQL_DELETE_FS_NODE_PRUNE_CHILDREN);
		queryDeleteFsNode.setParameter(1,nodeId);
		try {
			executeUpdate(queryDeleteFsNode);
		} catch (DatabaseException e) {
			logger.error("Failed to remove all children for node " + nodeId + ", from FS_NODE. " +  e.getMessage(),e);
			e.printStackTrace();
			return;
		}
		logger.debug("Deleted children of node " + nodeId + " from the node table.");			
		
		//
		// Remove children links from closure table.
		//
		// This query uses our prune table. Pass the prune ID which links to all the nodes to prune.
		//
		Query queryDeleteFsClosure = getEntityManager().createNativeQuery(SQL_DELETE_FS_CLOSURE_PRUNE);
		queryDeleteFsClosure.setParameter(1,pruneId);
		try {
			executeUpdate(queryDeleteFsClosure);
		} catch (DatabaseException e) {
			logger.error("Failed to remove all children for node " + nodeId + ", from FS_CLOSURE. " +  e.getMessage(),e);
			e.printStackTrace();
			return;
		}
		logger.debug("Deleted children of node " + nodeId + " from the closure table.");	
		
	}
	
	/**
	 * Move the node (with all children) to the new parent node.
	 * 
	 * @param nodeId - The ID of the node to move. This node plus all its children will be moved.
	 * @param newParentNodeId - The ID of the new parent node. The node being move will become a child of this node.
	 */
	public void moveNode(Long nodeId, Long newParentNodeId) throws DatabaseException {
		
		logger.info("Moving node => " + nodeId + " to new parent node => " + newParentNodeId);
		
		//
		// Get tree structure for the  branch / tree section we are moving.
		//
		List<FSClosure> closureList = getClosureByNodeId(nodeId);
		
		if(closureList == null || closureList.size() == 0){
			throw new DatabaseException("Move error. No closure list for node " + nodeId);
		}
		
		logger.debug("Fetched tree data for moving.");
		//LogUtil.logClosure(closureList);
		
		//
		// Prune the existing data
		//
		logger.debug("Pruning existing tree/branch node " + nodeId);
		// when performing a move we don't need (or want) to remove the nodes from the fs_node table. we will simply update the parent_ids.
		// we do want to remove the data from the closure table because the data gets rebuilt correctly during the insert operation.
		removeNode(nodeId, false, true);
		// necessary?
		getEntityManager().flush();
		//Tree<FSMeta> treeAfterPune = getTree(1);
		//logger.info("After prune: " + treeAfterPune.toString());		
		logger.debug("Prune operation complete");
		
		HashMap<Long,List<FSNode>> treeMap = new HashMap<Long,List<FSNode>>();
		
		// the child node of the first FSClosure object in the closureList is the root node of the tree that is being moved
		FSNode rootNode = null;
		for(FSClosure c : closureList){
			if(c.hasParent() && c.hasChild()){
				rootNode = c.getParentNode();  // was c.getChildNode()
				break;
			}
		}
		logger.debug("Have root node for tree being moved? => " + ((rootNode != null) ? true : false));
		logger.debug("root => " + LogUtil.getNodeString(rootNode));
		
		//
		// loop through closure list and build tree map
		//
		FSClosure closure = null;
		for(int closureIndex=0; closureIndex<closureList.size(); closureIndex++){
			closure = closureList.get(closureIndex);
			if(closure.hasParent() && closure.hasChild()){
				if(treeMap.containsKey(closure.getParentNode().getNodeId())){
					treeMap.get(closure.getParentNode().getNodeId()).add(closure.getChildNode());
				}else{
					List<FSNode> childList = new ArrayList<FSNode>();
					childList.add(closure.getChildNode());
					treeMap.put(closure.getParentNode().getNodeId(), childList);
				}
			}
		}
		
		logger.debug("Tree map size => " + treeMap.size());
		for(Long parentNodeId : treeMap.keySet()){
			logger.debug("Tree map children for node " + parentNodeId + " => " + ((treeMap.get(parentNodeId).size() > 0) ? treeMap.get(parentNodeId).size() : "0"));
			for(FSNode node : treeMap.get(parentNodeId)){
				logger.debug(" child: " + LogUtil.getNodeString(node));
			}
		}
		
		List<FSNode> childList = treeMap.get(rootNode.getNodeId());
		
		//
		// add the tree to the new parent node
		//
		reAddTree(rootNode, newParentNodeId, childList, treeMap, DateUtil.getCurrentTime());
		
	}
	
	/**
	 * Helper function for the move node operation. Re-adds the tree to the new parent node
	 * 
	 * @param rootNode - The root node of the sub tree that is being moved
	 * @param newParentId - The new parent node for the root node
	 * @param childNodes - The set of child nodes for the current root node
	 * @param treeMap - The rest of the tree data that we iterate over.
	 * @param dateUpdated - The updated data that will be set on all the nodes being moved.
	 */
	private void reAddTree(FSNode rootNode, Long newParentId, List<FSNode> childNodes, HashMap<Long,List<FSNode>> treeMap, Timestamp dateUpdated) throws DatabaseException {
		
		logger.debug("Adding " + rootNode.getNodeId() + " (" + rootNode.getName() + ") to parent " + newParentId);
		
		// add root node
		reAddNode(rootNode.getNodeId(), newParentId, rootNode.getName(), rootNode.getDateCreated(), dateUpdated);
		
		if(childNodes != null && childNodes.size() > 0){
			
			logger.debug("Node " + rootNode.getNodeId() + " (" + rootNode.getName() + ") has " + childNodes.size() + " children.");
			
			for(FSNode childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(childNode.getNodeId() != rootNode.getNodeId()){
					
					// recursively add child nodes, and all their children. The next child node becomes the current root node.
					reAddTree(childNode, rootNode.getNodeId(), treeMap.get(childNode.getNodeId()), treeMap, dateUpdated);
					
				}
				
			}
		}
		
	}

	@Override
	public String getRepositoryName() {
		return PostgresClosureRepository.class.getName();
	}

}
