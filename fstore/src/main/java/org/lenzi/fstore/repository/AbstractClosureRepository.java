/**
 * 
 */
package org.lenzi.fstore.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.FSClosure;
import org.lenzi.fstore.repository.model.FSNode;
import org.lenzi.fstore.repository.model.FSTree;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.DateUtil;
import org.lenzi.fstore.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Where all the work happens. Contains all code, minus specific database queries, for maintaining tree like
 * structures in a database using a closure table.
 * 
 * See OracleClosureRepository and PostgreSQLClosureRepository for database specific code.
 * 
 * @author sal
 */
@Transactional(propagation=Propagation.REQUIRED)
public abstract class AbstractClosureRepository extends AbstractRepository implements ClosureRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9258076299035972L;

	@InjectLogger
	private Logger logger;	
	
	/**
	 * 
	 */
	public AbstractClosureRepository() {
	
	}

	/**
	 * Add a new node
	 * 
	 * @param parentNodeId - 
	 * @param nodeName -
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#addNode(java.lang.Long, java.lang.String)
	 */
	@Override
	public FSNode addNode(Long parentNodeId, String nodeName) throws DatabaseException {

		// Get next available node id from sequence
		long nodeId = getSequenceVal(getSqlQueryNodeIdSequence());
		
		// create new node
		FSNode newNode = new FSNode();
		newNode.setNodeId(nodeId);
		newNode.setParentNodeId(parentNodeId);
		newNode.setName(nodeName);
		Timestamp now = DateUtil.getCurrentTime();
		newNode.setDateCreated(now);
		newNode.setDateUpdated(now);
		
		// save node to database
		getEntityManager().persist(newNode);
		
		// Get next available link id from sequence
		long linkId = getSequenceVal(getSqlQueryLinkIdSequence());
		
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
		Query queryInsertLinks = getEntityManager().createNativeQuery(getSqlQueryInsertMakeParent());
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
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#getNode(java.lang.Long)
	 */
	@Override
	public FSNode getNode(Long nodeId) throws DatabaseException {

		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryNodeById());
			query.setParameter("nodeId", nodeId);
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
		return (FSNode)getSingleResult(query);		
		
	}
	
	/**
	 * Get an FSNode object with it's parent closure and nodes.
	 * 
	 * @param node
	 * @return
	 * @throws DatabaseException
	 */
	public FSNode getNodeWithParentClosure(FSNode node) throws DatabaseException {
		
		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryNodeWithParentClosure());
			query.setParameter("nodeId", node.getNodeId());
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
		FSNode nodeWithParentClosure = (FSNode)getSingleResult(query);		
		
		return nodeWithParentClosure;
	}
	
	/**
	 * Get an FSNode object with it's child closure and nodes.
	 * 
	 * @param node
	 * @return
	 * @throws DatabaseException
	 */
	public FSNode getNodeWithChildClosure(FSNode node) throws DatabaseException {
		
		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryNodeWithChildClosure());
			query.setParameter("nodeId", node.getNodeId());
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
		FSNode nodeWithChildClosure = (FSNode)getSingleResult(query);		
		
		return nodeWithChildClosure;
	}	

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#getTree(java.lang.Long)
	 */
	@Override
	public FSTree getTree(Long treeId) throws DatabaseException {

		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryTreeById());
			query.setParameter("treeid", treeId);
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
		FSTree tree = (FSTree)getSingleResult(query);
		
		return tree;		
		
	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#addTree(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public FSTree addTree(String treeName, String treeDesc, String rootNodeName) throws DatabaseException {

		FSNode rootNode = addNode(0L, rootNodeName);
		Long rootNodeId = rootNode.getNodeId();
		
		// Get next available tree id from sequence
		long treeId = getSequenceVal(getSqlQueryTreeIdSequence());
	
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
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#addTree(java.lang.String, java.lang.String, org.lenzi.fstore.repository.model.FSNode)
	 */
	@Override
	public FSTree addTree(String treeName, String treeDesc, FSNode existingNode) throws DatabaseException {

		moveNode(existingNode.getNodeId(), 0L);
		
		// Get next available tree id from sequence
		long treeId = getSequenceVal(getSqlQueryTreeIdSequence());
	
		Timestamp now = DateUtil.getCurrentTime();
		
		FSTree tree = new FSTree();
		tree.setTreeId(treeId);
		tree.setName(treeName);
		tree.setDescription(treeDesc);
		tree.setDateCreated(now);
		tree.setDateUpdated(now);
		tree.setRootNodeId(existingNode.getNodeId());
		tree.setRootNode(existingNode); // adds node to database
		
		// save node to database
		//getEntityManager().persist(tree);
		persist(tree);
		
		getEntityManager().flush();
		getEntityManager().clear();		
		
		return tree;		
		
	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#copyNode(java.lang.Long, java.lang.Long, boolean)
	 */
	@Override
	public void copyNode(Long nodeId, Long parentNodeId, boolean copyChildren) throws DatabaseException {
	
		// copy just the node
		if(!copyChildren){
			
			FSNode nodeToCopy = getNode(nodeId);
			
			addNode(parentNodeId, nodeToCopy.getName());
			
		// copy the node and all children	
		}else{
			
			//
			// Get tree structure for the branch / tree section we are moving.
			//
			List<FSClosure> closureList = getClosureByNodeId(nodeId);
			
			if(closureList == null || closureList.size() == 0){
				throw new DatabaseException("Move error. No closure list for node " + nodeId);
			}
			
			HashMap<Long,List<FSNode>> treeMap = new HashMap<Long,List<FSNode>>();
			
			// get the root node of the sub-tree we are copying.
			FSNode rootNode = null;
			for(FSClosure c : closureList){
				if(c.hasParent() && c.hasChild()){
					rootNode = c.getParentNode();
					break;
				}
			}
			
			// loop through closure list and build tree map
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
			
			// get children for root node of sub-tree
			List<FSNode> childList = treeMap.get(rootNode.getNodeId());
			
			// add the root node to the new parent node, then walk the tree and add all the children.
			copyNodes(rootNode, parentNodeId, childList, treeMap);			
			
		}

	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#removeTree(java.lang.Long)
	 */
	@Override
	public void removeTree(Long treeId) throws DatabaseException {

		FSTree treeToDelete = getTree(treeId);
		FSNode rootNode = treeToDelete.getRootNode();
		
		removeNode(rootNode.getNodeId());
		
		getEntityManager().remove(treeToDelete);
		
	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#removeTree(org.lenzi.fstore.repository.model.FSTree, org.lenzi.fstore.repository.model.FSNode)
	 */
	@Override
	public void removeTree(FSTree tree, FSNode newParentNode) throws DatabaseException {

		logger.info("Remove tree but keeps nodes.");
		
		if(isSameTree(tree.getRootNode(), newParentNode)){
			throw new DatabaseException("You cannot move the root node of the existing tree to another node under the "
					+ "same tree. New parent node must be a node in a different tree.");			
		}
		
		logger.info("Not same tree, may proceed with move.");
		
		moveNode(tree.getRootNode().getNodeId(), newParentNode.getNodeId());
		
		logger.info("Moved nodes...Now deleting old tree");
		
		FSTree treeToDelete = getEntityManager().find(FSTree.class, tree.getTreeId());
		
		getEntityManager().remove(treeToDelete);

	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.ClosureRepository#getClosureByNodeId(java.lang.Long)
	 */
	@Override
	public List<FSClosure> getClosureByNodeId(Long nodeId) throws DatabaseException {

		logger.info("Getting closure list for node id => " + nodeId);
		
		List<FSClosure> results = null;
		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryClosureByNodeId());
			query.setParameter(1, nodeId);
			query.setParameter(2, nodeId);			
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}
		results = getResultList(query);
		return results;		
		
	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#moveNode(java.lang.Long, java.lang.Long)
	 */
	@Override
	public void moveNode(Long nodeId, Long newParentNodeId) throws DatabaseException {

		// TODO - make sure node being moved is not a root node.
		// FSTreeService currently performs this check, but it would be better here.
		
		// make sure new parent node is not a current child of the node that's being moved. you
		// cannot move a tree to under itself! we don't need to worry about this for the copy operation.
		if(isChild(getNode(newParentNodeId), getNode(nodeId), true)){
			throw new DatabaseException("Cannot move node " + nodeId + " to under node " + newParentNodeId + 
					". Node " + newParentNodeId + " is a child of node " + nodeId);
		}
		
		logger.debug("Moving node => " + nodeId + " to new parent node => " + newParentNodeId);
		
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
		// add the root node to the new parent node, then walk the tree and add all the children.
		//
		moveNodes(rootNode, newParentNodeId, childList, treeMap, DateUtil.getCurrentTime());

	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#removeNode(java.lang.Long)
	 */
	@Override
	public void removeNode(Long nodeId) throws DatabaseException {

		logger.debug("remove node " + nodeId);
		
		removeNode(nodeId, true, true);	

	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#removeChildren(java.lang.Long)
	 */
	@Override
	public void removeChildren(Long nodeId) throws DatabaseException {
		
		logger.debug("remove children of " + nodeId);
		
		//
		// Get next available prune id from sequence.
		//
		long pruneId = getSequenceVal(getSqlQueryPruneIdSequence());
		
		//
		// Add list of nodes to delete to our prune table
		//
		Query populatePrune = getEntityManager().createNativeQuery(getSqlQueryInsertPruneChildren());
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
		Query queryDeleteFsNode = getEntityManager().createNativeQuery(getSqlQueryDeleteFsNodePruneChildren());
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
		Query queryDeleteFsClosure = getEntityManager().createNativeQuery(getSqlQueryDeleteFsClosurePrune());
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
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#isSameTree(org.lenzi.fstore.repository.model.FSNode, org.lenzi.fstore.repository.model.FSNode)
	 */
	@Override
	public boolean isSameTree(FSNode node1, FSNode node2) throws DatabaseException {
		
		// both are root nodes. they are not in the same tree
		if(node1.getParentNodeId() == 0L && node2.getParentNodeId() == 0L){
			return false;
		}
		// both are not root nodes, but both have the same parent. they are in the same tree
		if( (node1.getParentNodeId() != 0L && node2.getParentNodeId() != 0L) && (node1.getParentNodeId() == node2.getParentNodeId())){
			return true;
		}
		
		logger.info("Getting parent data for node1 => " + node1.getNodeId());
		FSNode parentNode1 = getNodeWithParentClosure(node1);
		logger.info("Getting parent data for node2 => " + node2.getNodeId());
		FSNode parentNode2 = getNodeWithParentClosure(node2);
		
		if(parentNode1 == null || parentNode1.getParentClosure() == null || parentNode1.getParentClosure().size() == 0){
			throw new DatabaseException("Failed to get parent closure and parent node data for node " + node1.getNodeId());
		}
		if(parentNode2 == null || parentNode2.getParentClosure() == null || parentNode2.getParentClosure().size() == 0){
			throw new DatabaseException("Failed to get parent closure and parent node data for node " + node1.getNodeId());
		}
		
		FSNode rootNode1 = null;
		FSNode rootNode2 = null;
		
		logger.info("Iterating through node1 parent data to find tree root node");
		for(FSClosure c : parentNode1.getParentClosure()){
			if(c.getParentNode().getParentNodeId() == 0L){
				rootNode1 = c.getParentNode();
				break;
			}
		}
		logger.info("Iterating through node2 parent data to find tree root node");
		for(FSClosure c : parentNode2.getParentClosure()){
			if(c.getParentNode().getParentNodeId() == 0L){
				rootNode2 = c.getParentNode();
				break;
			}
		}
		if(rootNode1 == null){
			throw new DatabaseException("Failed to locate the root node (parent most node) for node " + node1.getNodeId());
		}
		if(rootNode2 == null){
			throw new DatabaseException("Failed to locate the root node (parent most node) for node " + node2.getNodeId());
		}
		
		// they have the same parent most node. they are in the same tree.
		if(rootNode1.getNodeId() == rootNode2.getNodeId()){
			return true;
		}
		
		return false;		
		
	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#isParent(org.lenzi.fstore.repository.model.FSNode, org.lenzi.fstore.repository.model.FSNode, boolean)
	 */
	@Override
	public boolean isParent(FSNode node1, FSNode node2, boolean fullSearch) throws DatabaseException {
		
		if(node2.getParentNodeId() == node1.getNodeId()){
			return true;
		}
		if(!fullSearch){
			return false;
		}else{
			
			// search all the way up the tree till the root node. if node1 is found, return true.
			FSNode node2Parents = getNodeWithParentClosure(node2);
			if(node2Parents == null || node2Parents.getParentClosure() == null || node2Parents.getParentClosure().size() == 0){
				throw new DatabaseException("Failed to get parent closure and parent node data for node " + node2.getNodeId());
			}
			for(FSClosure c : node2Parents.getParentClosure()){
				if(c.getParentNode().getNodeId() == node1.getNodeId()){
					return true;
				}
			}			
			
		}
		return false;
		
	}

	/**
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#isChild(org.lenzi.fstore.repository.model.FSNode, org.lenzi.fstore.repository.model.FSNode, boolean)
	 */
	@Override
	public boolean isChild(FSNode node1, FSNode node2, boolean fullSearch) throws DatabaseException {

		if(node2.getNodeId() == node1.getParentNodeId()){
			return true;
		}
		if(!fullSearch){
			return false;
		}else{
			
			// search all children of node2, till all leaf nodes are reached. If node 1 is found, return true
			FSNode node2Children = this.getNodeWithChildClosure(node2);
			if(node2Children == null || node2Children.getParentClosure() == null || node2Children.getParentClosure().size() == 0){
				throw new DatabaseException("Failed to get child closure and child node data for node " + node2.getNodeId());
			}
			for(FSClosure c : node2Children.getChildClosure()){
				if(c.getChildNode().getNodeId() == node1.getNodeId()){
					return true;
				}
			}			
			
		}
		return false;		
		
	}	
	
	
	
	
	// --------------------------------------------------------------------------------------------------------
	// private helper methods
	// --------------------------------------------------------------------------------------------------------	
	
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
		long linkId = getSequenceVal(getSqlQueryLinkIdSequence());
		
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
		Query queryInsertLinks = getEntityManager().createNativeQuery(getSqlQueryInsertMakeParent());
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
	 * Remove node helper function.
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
			pruneId = getSequenceVal(getSqlQueryPruneIdSequence());
			
			//
			// Add list of nodes to delete to our prune table
			//
			Query populatePrune = getEntityManager().createNativeQuery(getSqlQueryInsertPruneTree());
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
			Query queryDeleteFsNode = getEntityManager().createNativeQuery(getSqlQueryDeleteFsNodePruneTree());
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
			Query queryDeleteFsClosure = getEntityManager().createNativeQuery(getSqlQueryDeleteFsClosurePrune());
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
	 * Helper function for the move node operation. Re-adds the tree to the new parent node
	 * 
	 * @param rootNode - The root node of the sub tree that is being moved
	 * @param newParentId - The new parent node for the root node
	 * @param childNodes - The set of child nodes for the current root node
	 * @param treeMap - The rest of the tree data that we iterate over.
	 * @param dateUpdated - The updated data that will be set on all the nodes being moved.
	 */
	private void moveNodes(FSNode rootNode, Long newParentId, List<FSNode> childNodes, HashMap<Long,List<FSNode>> treeMap, Timestamp dateUpdated) throws DatabaseException {
		
		logger.debug("Adding " + rootNode.getNodeId() + " (" + rootNode.getName() + ") to parent " + newParentId);
		
		// add root node
		reAddNode(rootNode.getNodeId(), newParentId, rootNode.getName(), rootNode.getDateCreated(), dateUpdated);
		
		if(childNodes != null && childNodes.size() > 0){
			
			logger.debug("Node " + rootNode.getNodeId() + " (" + rootNode.getName() + ") has " + childNodes.size() + " children.");
			
			for(FSNode childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(childNode.getNodeId() != rootNode.getNodeId()){
					
					// recursively add child nodes, and all their children. The next child node becomes the current root node.
					moveNodes(childNode, rootNode.getNodeId(), treeMap.get(childNode.getNodeId()), treeMap, dateUpdated);
					
				}
				
			}
		}
		
	}
	
	/**
	 * Helper function for the copy node operation. Copies the tree to the new parent node.
	 * 
	 * @param rootNode - The root node of the sub tree that is being copied
	 * @param newParentId - The new parent node for the root node
	 * @param childNodes - The set of child nodes for the current root node
	 * @param treeMap - The rest of the tree data that we iterate over.
	 * @param copiedDate - The date the will be used for the created date and uopdated date on the new node copies.
	 */
	private void copyNodes(FSNode rootNode, Long newParentId, List<FSNode> childNodes, HashMap<Long, List<FSNode>> treeMap) throws DatabaseException {
		
		logger.debug("Adding " + rootNode.getNodeId() + " (" + rootNode.getName() + ") to parent " + newParentId);
		
		// add root node
		FSNode newCopy = addNode(newParentId, rootNode.getName());
		
		if(childNodes != null && childNodes.size() > 0){
			
			logger.debug("Node " + rootNode.getNodeId() + " (" + rootNode.getName() + ") has " + childNodes.size() + " children.");
			
			for(FSNode childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(childNode.getNodeId() != rootNode.getNodeId()){
					
					// recursively add child nodes, and all their children. The new copy node becomes the current root node.
					copyNodes(childNode, newCopy.getNodeId(), treeMap.get(childNode.getNodeId()), treeMap);
					
				}
				
			}
		}
		
	}	

	
	
	
	
	
	// --------------------------------------------------------------------------------------------------------
	// All functions below this line have database specific code, or have something else that is unique and 
	// cannot be abstracted.
	// --------------------------------------------------------------------------------------------------------
	
	public abstract String getRepositoryName();
	
	protected abstract long getSequenceVal(String nativeSequenceQuery) throws DatabaseException;
	
	protected abstract String getSqlQueryNodeIdSequence();
	
	protected abstract String getSqlQueryLinkIdSequence();
	
	protected abstract String getSqlQueryPruneIdSequence();
	
	protected abstract String getSqlQueryTreeIdSequence();
	
	// HQL_GET_NODE_BY_ID
	protected abstract String getHqlQueryNodeById();
	
	// HQL_GET_TREE_BY_ID
	protected abstract String getHqlQueryTreeById();
	
	// HQL_NODE_WITH_PARENT_CLOSURE
	protected abstract String getHqlQueryNodeWithParentClosure();
	
	// HQL_NODE_WITH_CHILDREN_CLOSURE
	protected abstract String getHqlQueryNodeWithChildClosure();
	
	// HQL_CLOSURE_BY_NODE_ID
	protected abstract String getHqlQueryClosureByNodeId();
	
	// SQL_INSERT_MAKE_PARENT
	protected abstract String getSqlQueryInsertMakeParent();
	
	// SQL_INSERT_PRUNE_TREE
	protected abstract String getSqlQueryInsertPruneTree();
	
	// SQL_INSERT_PRUNE_CHILDREN
	protected abstract String getSqlQueryInsertPruneChildren();
	
	// SQL_DELETE_FS_NODE_PRUNE_TREE
	protected abstract String getSqlQueryDeleteFsNodePruneTree();
	
	// SQL_DELETE_FS_NODE_PRUNE_CHILDREN
	protected abstract String getSqlQueryDeleteFsNodePruneChildren();
	
	// SQL_DELETE_FS_CLOSURE_PRUNE
	protected abstract String getSqlQueryDeleteFsClosurePrune();

}