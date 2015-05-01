/**
 * 
 */
package org.lenzi.fstore.repository.tree;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.repository.AbstractRepository;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.DBTree;
import org.lenzi.fstore.repository.model.impl.FSClosure;
import org.lenzi.fstore.repository.model.impl.FSClosure_;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.repository.model.impl.FSNode_;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.CollectionUtil;
import org.lenzi.fstore.util.DateUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Where all the work happens. Contains all code, minus specific database queries, for maintaining tree like
 * structures in a database using a closure table.
 * 
 * See AbstractOracleTreeRepository and AbstractPostgreSQLTreeRepository for database specific code.
 * 
 * @author sal
 *
 * @param <N>
 */
@Transactional(propagation=Propagation.REQUIRED)
public abstract class AbstractTreeRepository<N extends FSNode> extends AbstractRepository implements TreeRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9258076299035972L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	ClosureLogger closureLogger;
	
	/**
	 * 
	 */
	public AbstractTreeRepository() {
	
	}
	
	/**
	 * Implement to perform an necessary logic when a node is added.
	 * 
	 * @param node - The node that was added.
	 * @throws DatabaseException
	 */
	public abstract DBNode postAdd(N node) throws DatabaseException;
	
	/**
	 * Implement to perform an necessary logic when a node is moved.
	 * 
	 * @param node - The node that was moved.
	 * @throws DatabaseException
	 */
	public abstract DBNode postMove(N node) throws DatabaseException;
	
	/**
	 * Implement to perform an necessary logic when a node is removed.
	 * 
	 * @param node - The node that was removed.
	 * @throws DatabaseException
	 */
	public abstract void postRemove(N node) throws DatabaseException;
	
	/**
	 * Implement to perform an necessary logic when a node is copied.
	 * 
	 * @param originalNode - The original node
	 * @param newCopyNode - The copy of the new node.
	 * @return
	 * @throws DatabaseException
	 */
	public abstract DBNode postCopy(N originalNode, N newCopyNode) throws DatabaseException;
	
	/**
	 * Fetch node, just meta data. No closure data with parent child relationships is fetched.
	 * 
	 * @see org.lenzi.fstore.repository.tree.TreeRepository#getNode(java.lang.Long)
	 */
	@Override
	public DBNode getNode(Long nodeId) throws DatabaseException {
		
		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryNodeById());
			query.setParameter("nodeId", nodeId);
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
		return (DBNode)getSingleResult(query);	
	}	

	/**
	 * Add a new root node. Parent node ID will be set to 0.
	 * 
	 * @see org.lenzi.fstore.repository.tree.TreeRepository#addRootNode(org.lenzi.fstore.repository.model.DBNode)
	 */
	@Override
	public DBNode addRootNode(DBNode newNode) throws DatabaseException {
		
		if(newNode == null){
			throw new DatabaseException("Cannot add new node. Node object is null.");
		}
		
		return addNode(0L, newNode);
		
	}

	/**
	 * Add a new child node.
	 * 
	 * @param parentNode - The parent node under which the new node will be added.
	 * @param newNode - The new node to add.
	 * 
	 * @see org.lenzi.fstore.repository.tree.TreeRepository#addChildNode(org.lenzi.fstore.repository.model.DBNode, org.lenzi.fstore.repository.model.DBNode)
	 */
	@Override
	public DBNode addChildNode(DBNode parentNode, DBNode newNode) throws DatabaseException {
		
		if(parentNode == null || newNode == null){
			throw new DatabaseException("Cannot add new node. Parent node and/or new node objects are null.");
		}
		if(parentNode.getNodeId() == null){
			throw new DatabaseException("Cannot add new node. Parent node ID is null. This information is needed.");
		}
		
		return addNode(parentNode.getNodeId(), newNode);
		
	}
	
	/**
	 * Add a new node.
	 * 
	 * @param parentNodeId - The ID of the parent node under which the new new will be added.
	 * @param newNode - The new node to add.
	 * @return
	 * @throws DatabaseException
	 */
	private DBNode addNode(Long parentNodeId, DBNode newNode) throws DatabaseException {
		
		if(newNode.getName() == null){
			throw new DatabaseException("Cannot add new node, new node is missing a name. This is a required field.");
		}
		Timestamp dateNow = DateUtil.getCurrentTime();
		newNode.setDateCreated(dateNow);
		newNode.setDateUpdated(dateNow);	
		
		// Get next available node id from sequence
		long nodeId = getSequenceVal(getSqlQueryNodeIdSequence());
		
		// Set node id and parent node id
		newNode.setNodeId(nodeId);
		newNode.setParentNodeId(parentNodeId);
		
		//logger.info("Adding node " + nodeId + " (" + newNode.getName() + ") to parent node " + parentNodeId);
		
		// Save node to database
		getEntityManager().persist(newNode);
		
		// Get next available link id from sequence
		long linkId = getSequenceVal(getSqlQueryLinkIdSequence());
		
		// Add depth-0 self link to closure table
		DBClosure selfLink = new FSClosure();
		selfLink.setLinkId(linkId);
		selfLink.setChildNodeId(newNode.getNodeId());
		selfLink.setParentNodeId(newNode.getNodeId());
		selfLink.setDepth(0);
		
		// Save closure self link to database
		getEntityManager().persist(selfLink);
		
		// Necessary?
		getEntityManager().flush();
		
		// Add parent-child links to closure table
		Query queryInsertLinks = getEntityManager().createNativeQuery(getSqlQueryInsertMakeParent());
		queryInsertLinks.setParameter(1, newNode.getParentNodeId());
		queryInsertLinks.setParameter(2, newNode.getNodeId());		
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
		
		// call users custom add node method
		postAdd( (N)newNode );
		
		return newNode;		
		
	}
	
	/**
	 * Get closure data for a node. This will give you all the necessary information to build a tree model.
	 * Usually you would do this for a root node of a tree.
	 * 
	 * @see org.lenzi.fstore.repository.tree.TreeRepository#getClosure(org.lenzi.fstore.repository.model.DBNode)
	 */
	@Override
	public List<DBClosure> getClosure(DBNode node) throws DatabaseException {

		if(node == null){
			throw new DatabaseException("Cannot fetch closure data for node. Node object is null.");
		}
		if(node.getNodeId() == null){
			throw new DatabaseException("Cannot fetch closure data for node. Node ID is null. This value is needed.");
		}
		
		//logger.info("Fetching closure data for node id => " + node.getNodeId());
		
		List<DBClosure> results = null;
		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryClosureByNodeId());
			query.setParameter(1, node.getNodeId());
			query.setParameter(2, node.getNodeId());			
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}
		results = getResultList(query);
		
		return results;			
		
	}
	
	/**
	 * Copy a node.
	 * 
	 * @see org.lenzi.fstore.repository.tree.TreeRepository#copyNode(org.lenzi.fstore.repository.model.DBNode, org.lenzi.fstore.repository.model.DBNode, boolean)
	 */
	@Override
	public DBNode copyNode(DBNode nodeToCopy, DBNode parentNode, boolean copyChildren, NodeCopier copier) throws DatabaseException {
		
		if(nodeToCopy == null || parentNode == null){
			throw new DatabaseException("Cannot copy node. Node object is null, and/or parent node object is null.");
		}
		if(parentNode.getNodeId() == null){
			throw new DatabaseException("Cannot copy node. Node ID of parent node is null. This value is needed.");
		}
		if(nodeToCopy.getParentNodeId() == null){
			throw new DatabaseException("Cannot copy node. Parent node ID of node being copied is null. Need this data to determin if node is a root node or a child node.");
		}
		
		// copy just the node
		if(!copyChildren){
			
			//logger.info("Copy node without children");
			
			DBNode newCopy = copier.copy(nodeToCopy);
			newCopy.setNodeId(null);
			newCopy.setParentNodeId(null);
			newCopy.setChildClosure(null);
			newCopy.setParentClosure(null);
			
			if(nodeToCopy.isRootNode()){
				newCopy = addRootNode(newCopy);
			}else{
				newCopy = addChildNode(parentNode, newCopy);
			}
			postCopy( (N)nodeToCopy, (N)newCopy);
			return newCopy;
			
		// copy the node and all children	
		}else{
			
			// Get closure data for the sub-tree we are copying
			List<DBClosure> closureList = getClosure(nodeToCopy);
			
			//logger.info("Fetched closure data for node => " + nodeToCopy.getNodeId());
			//closureLogger.logClosure(closureList);
			
			if(closureList == null || closureList.size() == 0){
				throw new DatabaseException("Move error. No closure list for node " + nodeToCopy.getNodeId());
			}
			
			// needed for copy node with children...
			//getEntityManager().flush();
			//getEntityManager().clear();
			
			// get the root node of the sub-tree we are copying.
			DBNode rootNode = null;
			for(DBClosure c : closureList){
				if(c.hasParent() && c.hasChild()){
					rootNode = c.getParentNode();
					break;
				}
			}
			
			HashMap<Long,List<DBNode>> treeMap = buildMapFromClosure(closureList);
			
			// get children for root node of sub-tree
			List<DBNode> childList = treeMap.get(rootNode.getNodeId());
			
			// add the root node of the sub-tree to the new parent node, then walk the tree and add all the children.
			return copyNodes(nodeToCopy, parentNode, childList, treeMap, copier);			
			
		}		
		
	}
	
	/**
	 * Move a node
	 */
	@Override
	public DBNode moveNode(DBNode nodeToMode, DBNode newParentNode) throws DatabaseException {
		
		Long moveNodeId = nodeToMode.getNodeId();
		Long newParentNodeId = newParentNode.getNodeId();
		
		// make sure node being moved is not a root node.
		if(nodeToMode.getParentNodeId() == 0L){
			throw new DatabaseException("Cannot move a root node. Use future rootToLeaf() method...coming soon...");
		}
		
		//TODO - REMOVE THIS BLOCK
		/*
		if(true){
			
			List<DBClosure> closureList = null;
			
			// Get tree structure for the  branch / tree section we are moving.
			closureList = getClosure(nodeToMode);
			if(closureList == null || closureList.size() == 0){
				throw new DatabaseException("Move error. No closure list for node being moved => " + moveNodeId);
			}
			logger.info("Fetched closure data for node being moved => " + moveNodeId);
			closureLogger.logClosure(closureList);
			
			closureList = getClosure(newParentNode);
			if(closureList == null || closureList.size() == 0){
				throw new DatabaseException("Move error. No closure list for new parent node => " + newParentNodeId);
			}
			logger.info("Fetched closure data for new parent node => " + newParentNodeId);
			closureLogger.logClosure(closureList);
			
		} // END
		*/
		
		// make sure new parent node is not a current child of the node that's being moved. you
		// cannot move a tree to under itself! we don't need to worry about this for the copy operation.
		if(isChild(newParentNode, nodeToMode, true)){
			throw new DatabaseException("Cannot move node " + moveNodeId + " to under node " + newParentNodeId + 
					". Node " + newParentNodeId + " is a child of node " + moveNodeId);
		}
		
		logger.info("Moving node => " + moveNodeId + " to new parent node => " + newParentNodeId);
		
		// Get tree structure for the  branch / tree section we are moving.
		List<DBClosure> closureList = getClosure(nodeToMode);
		if(closureList == null || closureList.size() == 0){
			throw new DatabaseException("Move error. No closure list for node " + moveNodeId);
		}
		logger.info("Fetched tree data for moving.");
		closureLogger.logClosure(closureList);
		
		// Prune the existing data
		logger.info("Pruning existing tree/branch node " + moveNodeId);
		// when performing a move we don't need (or want) to remove the nodes from the fs_node table. we will simply update the parent_ids.
		// we do want to remove the data from the closure table because the data gets rebuilt correctly during the insert operation.
		removeNode(nodeToMode, false, true);
		// necessary?
		getEntityManager().flush();
		
		// get the root node of the sub-tree we are copying.
		DBNode rootNode = null;
		for(DBClosure c : closureList){
			if(c.hasParent() && c.hasChild()){
				rootNode = c.getParentNode();
				break;
			}
		}
		
		HashMap<Long,List<DBNode>> treeMap = buildMapFromClosure(closureList);
		
		// get children for root node of sub-tree
		List<DBNode> childList = treeMap.get(rootNode.getNodeId());		
	
		// add the root node to the new parent node, then walk the tree and add all the children.
		return moveNodes(rootNode, newParentNode, childList, treeMap, DateUtil.getCurrentTime());	
		
	}
	
	/**
	 * Builds a map where the keys are node IDs, and the values are Lists of DBNode objects.
	 * 
	 * map.get(nodeId) will return a list containing all the child nodes for that node.
	 * 
	 * @param closureList
	 * @return
	 */
	private HashMap<Long,List<DBNode>> buildMapFromClosure(List<DBClosure> closureList) {
		
		if(CollectionUtil.isEmpty(closureList)){
			return null;
		}
		
		DBClosure closure = null;
		HashMap<Long,List<DBNode>> map = new HashMap<Long,List<DBNode>>();
		
		for(int closureIndex=0; closureIndex<closureList.size(); closureIndex++){
			closure = closureList.get(closureIndex);
			if(closure.hasParent() && closure.hasChild()){
				if(map.containsKey(closure.getParentNode().getNodeId())){
					map.get(closure.getParentNode().getNodeId()).add(closure.getChildNode());
				}else{
					List<DBNode> childList = new ArrayList<DBNode>();
					childList.add(closure.getChildNode());
					map.put(closure.getParentNode().getNodeId(), childList);
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Helper method for the move nodes operation.
	 * 
	 * @param rootNode
	 * @param parentNode
	 * @param childNodes
	 * @param treeMap
	 * @param dateUpdated
	 * @throws DatabaseException
	 */
	private DBNode moveNodes(DBNode rootNode, DBNode parentNode, List<DBNode> childNodes, HashMap<Long,List<DBNode>> treeMap, Timestamp dateUpdated) throws DatabaseException {
		
		Long rootNodeId = rootNode.getNodeId();
		Long parentNodeNodeId = parentNode.getNodeId();		
		
		logger.info("Adding " + rootNodeId + " (" + rootNode.getName() + ") to parent " + parentNodeNodeId);
		
		// re-add node
		rootNode.setParentNodeId(parentNodeNodeId);
		rootNode.setDateUpdated(dateUpdated);
		DBNode updatedRootNode = reAddNode(rootNode);
		
		if(childNodes != null && childNodes.size() > 0){
			
			logger.info("Node " + rootNode.getNodeId() + " (" + rootNode.getName() + ") has " + childNodes.size() + " children.");
			
			for(DBNode childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(childNode.getNodeId() != rootNodeId){
					
					// recursively add child nodes, and all their children. The next child node becomes the current root node.
					moveNodes(childNode, rootNode, treeMap.get(childNode.getNodeId()), treeMap, dateUpdated);
					
				}else{
					logger.info("Child node is the depth-0 self link. Skipping move.");
				}
				
			}
		}
		
		return updatedRootNode;
		
	}
	
	/**
	 * Merge the node with the updated data, and add new closure links.
	 * 
	 * @param node - the updated node to re-add/merge in the database.
	 * @return
	 * @throws DatabaseException
	 */
	private DBNode reAddNode(DBNode node) throws DatabaseException{
		
		// re-add node to database
		DBNode updatedEntity = getEntityManager().merge(node);
		
		// Get next available link id from sequence
		long linkId = getSequenceVal(getSqlQueryLinkIdSequence());
		
		// add depth-0 self link to closure table
		FSClosure selfLink = new FSClosure();
		selfLink.setLinkId(linkId);
		selfLink.setChildNodeId(node.getNodeId());
		selfLink.setParentNodeId(node.getNodeId());	
		selfLink.setDepth(0);
		
		// save closure self link to database
		getEntityManager().persist(selfLink);
		
		// necessary?
		getEntityManager().flush();
		
		// add parent-child links to closure table
		Query queryInsertLinks = getEntityManager().createNativeQuery(getSqlQueryInsertMakeParent());
		queryInsertLinks.setParameter(1, node.getParentNodeId());
		queryInsertLinks.setParameter(2, node.getNodeId());		
		try {
			executeUpdate(queryInsertLinks);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to add parent-child links FS_CLOSURE for node "
					+ "(id = " + node.getNodeId() + ", name = " + node.getName() + "). " +  e.getMessage(), e);
		}
		
		// these two calls are required ( definitely the clear() call )
		getEntityManager().flush();
		getEntityManager().clear();
		
		return updatedEntity;		
		
	}	

	/**
	 * Helper function for the copy node operation.
	 * 
	 * @param nodeToCopy
	 * @param parentNode
	 * @param childNodes
	 * @param treeMap
	 * @throws DatabaseException
	 */
	private DBNode copyNodes(DBNode nodeToCopy, DBNode parentNode, List<DBNode> childNodes, HashMap<Long, List<DBNode>> treeMap, NodeCopier copier) throws DatabaseException {
		
		Long copyNodeId = nodeToCopy.getNodeId();
		
		DBNode newCopy = copier.copy(nodeToCopy);
		newCopy.setNodeId(null);
		newCopy.setParentNodeId(null);
		newCopy.setChildClosure(null);
		newCopy.setParentClosure(null);
		
		// add root node of sub-tree
		if(nodeToCopy.isRootNode()){
			newCopy = addRootNode(newCopy);
		}else{
			newCopy = addChildNode(parentNode, newCopy);
		}
		postCopy( (N)nodeToCopy, (N)newCopy);
		
		// the node id of nodeToCopy has changed!
		
		if(childNodes != null && childNodes.size() > 0){
			
			for(DBNode childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(childNode.getNodeId() != copyNodeId){
					
					// recursively add child nodes, and all their children. The new copy node becomes the current root node.
					copyNodes(childNode, newCopy, treeMap.get(childNode.getNodeId()), treeMap, copier);
					
				}else{
					logger.info("Child node is the depth-0 self link. Skipping copy.");
				}
				
			}
		}
		
		return newCopy;
		
	}	

	/**
	 * Add a tree.
	 * 
	 * @see org.lenzi.fstore.repository.tree.TreeRepository#addTree(org.lenzi.fstore.repository.model.DBTree, org.lenzi.fstore.repository.model.DBNode)
	 */
	@Override
	public DBTree addTree(DBTree newTree, DBNode newRootNode) throws DatabaseException {

		if(newTree == null || newRootNode == null){
			throw new DatabaseException("Cannot add tree. Tree object is null, and/or root node object is null.");
		}
		if(newTree.getName() == null){
			throw new DatabaseException("Cannot add tree. Tree name is null. This is a required field.");
		}
		if(newRootNode.getName() == null){
			throw new DatabaseException("Cannot add tree. Root node name is null. This is a required field.");
		}		
		if(newTree.getDateCreated() == null || newTree.getDateUpdated() == null || 
				newRootNode.getDateCreated() == null || newRootNode.getDateUpdated() == null){
			
			Timestamp dateNow = DateUtil.getCurrentTime();
			newTree.setDateCreated(dateNow);
			newTree.setDateUpdated(dateNow);
			newRootNode.setDateCreated(dateNow);
			newRootNode.setDateUpdated(dateNow);
		}		
		
		DBNode rootNode = addRootNode(newRootNode);
		
		// Get next available tree id from sequence
		long treeId = getSequenceVal(getSqlQueryTreeIdSequence());
		
		newTree.setTreeId(treeId);
		newTree.setRootNodeId(rootNode.getNodeId());
		newTree.setRootNode(rootNode);
		
		// save tree and its root node to database
		persist(newTree);
		
		getEntityManager().flush();
		getEntityManager().clear();		
		
		return newTree;		
		
	}
	
	/**
	 * Are two nodes in the same tree
	 */
	@Override
	public boolean isSameTree(DBNode node1, DBNode node2) throws DatabaseException {

		// both are root nodes. they are not in the same tree
		if(node1.getParentNodeId() == 0L && node2.getParentNodeId() == 0L){
			return false;
		}
		// both are not root nodes, but both have the same parent. they are in the same tree
		if( (node1.getParentNodeId() != 0L && node2.getParentNodeId() != 0L) && (node1.getParentNodeId() == node2.getParentNodeId())){
			return true;
		}
		
		logger.info("Getting parent data for node1 => " + node1.getNodeId());
		DBNode parentNode1 = getNodeWithParentClosure(node1);
		logger.info("Getting parent data for node2 => " + node2.getNodeId());
		DBNode parentNode2 = getNodeWithParentClosure(node2);
		
		if(parentNode1 == null || parentNode1.getParentClosure() == null || parentNode1.getParentClosure().size() == 0){
			throw new DatabaseException("Failed to get parent closure and parent node data for node " + node1.getNodeId());
		}
		if(parentNode2 == null || parentNode2.getParentClosure() == null || parentNode2.getParentClosure().size() == 0){
			throw new DatabaseException("Failed to get parent closure and parent node data for node " + node1.getNodeId());
		}
		
		DBNode rootNode1 = null;
		DBNode rootNode2 = null;
		
		logger.info("Iterating through node1 parent data to find tree root node");
		for(DBClosure c : parentNode1.getParentClosure()){
			if(c.getParentNode().getParentNodeId() == 0L){
				rootNode1 = c.getParentNode();
				break;
			}
		}
		logger.info("Iterating through node2 parent data to find tree root node");
		for(DBClosure c : parentNode2.getParentClosure()){
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
	 * Is node1 a parent of node2
	 */
	@Override
	public boolean isParent(DBNode node1, DBNode node2, boolean fullSearch) throws DatabaseException {

		if(node2.getParentNodeId() == node1.getNodeId()){
			return true;
		}
		if(!fullSearch){
			return false;
		}else{
			
			// search all the way up the tree till the root node. if node1 is found, return true.
			DBNode node2Parents = getNodeWithParentClosure(node2);
			if(node2Parents == null || node2Parents.getParentClosure() == null || node2Parents.getParentClosure().size() == 0){
				throw new DatabaseException("Failed to get parent closure and parent node data for node " + node2.getNodeId());
			}
			for(DBClosure c : node2Parents.getParentClosure()){
				if(c.getParentNode().getNodeId() == node1.getNodeId()){
					return true;
				}
			}			
			
		}
		return false;
		
	}
	
	/**
	 * Is node1 a child of node2
	 */
	@Override
	public boolean isChild(DBNode node1, DBNode node2, boolean fullSearch) throws DatabaseException {

		if(node2.getNodeId() == node1.getParentNodeId()){
			//logger.info("Node " + node2.getNodeId() + " is and immediate parent of node " + node1.getNodeId());
			return true;
		}
		if(!fullSearch){
			return false;
		}else{
			
			// search all children of node2, till all leaf nodes are reached. If node 1 is found, return true
			DBNode node2Children = getNodeWithChildClosure(node2);
			if(node2Children == null || node2Children.getParentClosure() == null || node2Children.getParentClosure().size() == 0){
				throw new DatabaseException("Failed to get child closure and child node data for node " + node2.getNodeId());
			}
			//logger.info("Searching all children of node2 => " + node2.getNodeId() + " to see if node " + node1.getNodeId() + " exists");
			for(DBClosure c : node2Children.getChildClosure()){
				//logger.info("Child found of node " + node2.getNodeId() + " => " + c.getChildNode().getNodeId());
				if(c.getChildNode().getNodeId() == node1.getNodeId()){
					return true;
				}
			}			
			
		}
		return false;
		
	}
	
	/**
	 * Get a node with its parent closure data
	 * 
	 * @param node
	 * @return
	 * @throws DatabaseException
	 */
	public DBNode getNodeWithParentClosure(DBNode node) throws DatabaseException {
		
		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryNodeWithParentClosure());
			query.setParameter("nodeId", node.getNodeId());
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
		DBNode nodeWithParentClosure = (DBNode)getSingleResult(query);		
		
		return nodeWithParentClosure;
	}

	/**
	 * Get a node with its child closure data
	 * 
	 * @param node
	 * @return
	 * @throws DatabaseException
	 */
	public DBNode getNodeWithChildClosure(DBNode node) throws DatabaseException {
		
		Query query = null;
		try {
			query = getEntityManager().createQuery(getHqlQueryNodeWithChildClosure());
			query.setParameter("nodeId", node.getNodeId());
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
		DBNode nodeWithChildClosure = (DBNode)getSingleResult(query);		
		
		return nodeWithChildClosure;
	}
	
	/**
	 * Removes the node and all it's children from the database.
	 */
	@Override
	public void removeNode(DBNode node) throws DatabaseException {
		
		logger.info("remove node " + node.getNodeId());
		
		if(node.getParentNodeId() == 0L){
			throw new DatabaseException("Cannot remove root node of tree. Use removeTree() method.");
		}		
		
		removeNode(node, true, true);	
		
	}
	
	/**
	 * Removes all the children of the node, but not the node itself.
	 */
	@Override
	public void removeChildren(DBNode node) throws DatabaseException {
		
		List<N> userNodesToDelete = null;
		
		Long parentNodeId = node.getNodeId();
		
		// Get next available prune id from sequence.
		long pruneId = getSequenceVal(getSqlQueryPruneIdSequence());
		
		// Add list of nodes to delete to our prune table
		Query populatePrune = getEntityManager().createNativeQuery(getSqlQueryInsertPruneChildren());
		populatePrune.setParameter(1, parentNodeId);
		try {
			executeUpdate(populatePrune);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed populate prune table with list of nodes to delete. " +  e.getMessage(),e);
		}
		logger.debug("Added list of nodes to delete to prune table under prune id " + pruneId);
		
		userNodesToDelete = doCriteriaDeleteNode(node, true);
		
		logger.debug("Deleted children of node " + parentNodeId + " from the node table.");	
		
		// Remove children links from closure table.
		// This query uses our prune table. Pass the prune ID which links to all the nodes to prune.
		Query queryDeleteFsClosure = getEntityManager().createNativeQuery(getSqlQueryDeleteFsClosurePrune());
		queryDeleteFsClosure.setParameter(1, pruneId);
		try {
			executeUpdate(queryDeleteFsClosure);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to remove all children for node " + parentNodeId + ", from FS_CLOSURE. " +  e.getMessage(),e);
		}
		logger.debug("Deleted children of node " + parentNodeId + " from the closure table.");
		
		// allow user access to each node that was delete so that they may perform post delete cleanup
		for(N n : userNodesToDelete){
			//  remove users data
			postRemove( (N)n);				
		}		
		
	}

	/**
	 * Remove node helper function.
	 * 
	 * @param nodeId The ID of the node to remove.
	 * @param nodeTable - True to remove the node and all child nodes from the FS_NODE table, false otherwise.
	 * @param pruneTable - True to remove all closure entries from the FS_CLOSURE table, false otherwise.
	 */
	private void removeNode(DBNode node, boolean nodeTable, boolean pruneTable) throws DatabaseException {
		
		Long deleteNodeId = node.getNodeId();
		
		logger.info("remove node " + deleteNodeId + ", nodeTable => " + nodeTable + ", pruneTable => " + pruneTable);
		
		long pruneId = 0;
		
		List<N> userNodesToDelete = null;
		
		if(pruneTable){
			
			// Get next available prune id from sequence
			pruneId = getSequenceVal(getSqlQueryPruneIdSequence());
			
			// Add list of nodes to delete to our prune table
			Query populatePrune = getEntityManager().createNativeQuery(getSqlQueryInsertPruneTree());
			populatePrune.setParameter(1, deleteNodeId);
			try {
				executeUpdate(populatePrune);
			} catch (DatabaseException e) {
				throw new DatabaseException("Failed populate prune table with list of nodes to delete. " +  e.getMessage(),e);
			}
			logger.debug("Added list of nodes to delete to prune table under prune id " + pruneId);
			
		}
		
		// this part must happen in the middle. it relies on data in the closure table
		if(nodeTable){
			
			userNodesToDelete = doCriteriaDeleteNode(node, false);
			
			logger.debug("Deleted node " + deleteNodeId + " from the node table.");
			
		}
		
		if(pruneTable){
			
			// Remove node depth-0 self link, plus all children links, from closure table.
			// This query uses our prune table. Pass the prune ID which links to all the nodes to prune.
			Query queryDeleteFsClosure = getEntityManager().createNativeQuery(getSqlQueryDeleteFsClosurePrune());
			queryDeleteFsClosure.setParameter(1,pruneId);
			try {
				executeUpdate(queryDeleteFsClosure);
			} catch (DatabaseException e) {
				throw new DatabaseException("Failed to remove node " + deleteNodeId + ", plus all children links, from FS_CLOSURE. " +  e.getMessage(),e);
			}
			logger.debug("Deleted node " + deleteNodeId + " from the closure table.");
			
		}
		
		if(nodeTable){
			// allow user access to each node that was delete so that they may perform post delete cleanup
			for(N n : userNodesToDelete){
				//  remove users data
				postRemove( (N)n);				
			}
		}
		
	}
	
	/**
	 * Delete the node and all its children, or just its children.
	 * 
	 * // delete node and all children SQL
		delete
		from fs_node n
		where n.node_id in (
		  select c.child_node_id
		  from fs_closure c
		  where c.parent_node_id = ?
		)
		
		// delete children of node SQL
		delete
		from fs_node n
		where n.node_id in (
		  select c.child_node_id
		  from fs_closure c
		  where c.parent_node_id = ?
		  and c.depth > 0
		)
	 * 
	 * @param node Will delete this node and all its children, or just its children if 'onlyChildren' is true
	 * @param onlyChildren - false to delete the node and all its children, or true to delete all the children but keep the node itself.
	 * @return A list of all the nodes that were deleted.
	 * @throws DatabaseException
	 */
	private List<N> doCriteriaDeleteNode(DBNode node, boolean onlyChildren) throws DatabaseException {
		
		List<Long> nodeIdList = getNodeIdList(node, onlyChildren);
		
		if(CollectionUtil.isEmpty(nodeIdList)){
			throw new DatabaseException("Failed to get list of node IDs for the " + ((onlyChildren) ? "child" : "") + " nodes. Cannot delete.");
		}
		
		List<N> userNodesToDelete = getNodes(nodeIdList, node.getClass());
		
		if(CollectionUtil.isEmpty(userNodesToDelete)){
			throw new DatabaseException("Failed to get " + ((onlyChildren) ? "child" : "") + " node data in preparation for deletion. Cannot delete");
		}
		
		// create delete query which uses the list of child node ID we just retrieved.
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaDelete criteriaDelete = criteriaBuilder.createCriteriaDelete(node.getClass());
		Root nodeDeleteRoot = criteriaDelete.from(node.getClass());
		criteriaDelete.where(
				nodeDeleteRoot.get(FSNode_.nodeId).in(nodeIdList)
			);
		
		getEntityManager().createQuery(criteriaDelete).executeUpdate();
		
		return userNodesToDelete;
		
	}
	
	/**
	 * Get the IDs of all the child nodes under the node. Optionally include the ID of the node
	 * itself in the list if 'onlyChildren' is set to false.
	 * 
	 * @param node The parent node. Will get the IDs of all nodes under this node. Set 'onlyChildren' to false
	 * 	to include the ID of this node in the list.
	 * @param onlyChildren true to only retrieve the IDs of the child nodes, false to also include the ID of
	 * 	the node you passes in.
	 * @return
	 * @throws DatabaseException
	 */
	public List<Long> getNodeIdList(DBNode node, boolean onlyChildren) throws DatabaseException {
		
		Long nodeId = node.getNodeId();
		List<Long> nodeIdList = null;
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		// create sub query to get list of all child nodes (excluding the node itself, the depth-0 self link entry in the closure)
		CriteriaQuery<Long> childQuery = criteriaBuilder.createQuery(Long.class);
		Root<FSClosure> closureRoot = childQuery.from(FSClosure.class);	
		childQuery.select(closureRoot.<Long>get(FSClosure_.childNodeId));
		
		// if only children, add the greater than depth 0 condition.
		if(onlyChildren){
			List<Predicate> andPredicates = new ArrayList<Predicate>();
			andPredicates.add( criteriaBuilder.equal(closureRoot.get(FSClosure_.parentNodeId), nodeId) );
			andPredicates.add( criteriaBuilder.greaterThan(closureRoot.get(FSClosure_.depth), 0) );			
			childQuery.where(
					criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
					);
		}else{
			childQuery.where(
					criteriaBuilder.equal(closureRoot.get(FSClosure_.parentNodeId), nodeId)
					);
		}
		
		nodeIdList = getEntityManager().createQuery(childQuery).getResultList();
		
		return nodeIdList;
		
	}
	
	/**
	 * Retrieve the node, and all nodes under it, along with the nodes closure data, and
	 * the parent and child nodes for the closure data. Set 'onlyChildren' to true to only get
	 * the child nodes.
	 * 
	 * @param node The node to fetch, plus all child nodes.
	 * @param onlyChildren - true to only get the child nodes, false to include the node you passes in.
	 * @return
	 * @throws DatabaseException
	 */
	protected List<N> getNodes(DBNode node, boolean onlyChildren) throws DatabaseException {
	
		List<Long> nodeIdList = getNodeIdList(node, onlyChildren);
		
		List<N> nodeList = getNodes(nodeIdList, node.getClass());
		
		return nodeList;
		
	}
	
	/**
	 * Get all nodes of the specified type, whose IDs are in the list
	 * 
	 * @param nodeIdList - The list of node IDs
	 * @param c The class type of the node which extends from FSNode
	 * @return
	 * @throws DatabaseException
	 */
	private List<N> getNodes(List<Long> nodeIdList, Class c) throws DatabaseException {
		
		List<N> nodeList = null;
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery selectNodesToDelete = criteriaBuilder.createQuery(c);
		Root nodeSelectRoot = selectNodesToDelete.from(c);
		
		SetJoin childClosureJoin = nodeSelectRoot.join(FSNode_.childClosure, JoinType.LEFT);
		SetJoin parentClosureJoin = nodeSelectRoot.join(FSNode_.parentClosure, JoinType.LEFT);
		Fetch childClosureFetch =  nodeSelectRoot.fetch(FSNode_.childClosure, JoinType.LEFT);
		Fetch parentClosureFetch =  nodeSelectRoot.fetch(FSNode_.parentClosure, JoinType.LEFT);
		childClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		childClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		parentClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		parentClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		selectNodesToDelete.distinct(true);
		selectNodesToDelete.select(nodeSelectRoot);
		selectNodesToDelete.where(
				nodeSelectRoot.get(FSNode_.nodeId).in(nodeIdList)
				);
		nodeList = getEntityManager().createQuery(selectNodesToDelete).getResultList();
		
		return nodeList;
		
	}
	
	/*

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

	@Override
	public void removeTree(Long treeId) throws DatabaseException {

		FSTree treeToDelete = getTree(treeId);
		FSNode rootNode = treeToDelete.getRootNode();
		
		removeNode(rootNode.getNodeId());
		
		getEntityManager().remove(treeToDelete);
		
	}

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
	*/
	

	
	
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
	
	/**
	 * SQL_DELETE_FS_NODE_PRUNE_TREE
	 * 
	 * @deprecated - replaced with a jpa criteria query
	 * @return
	 */
	protected abstract String getSqlQueryDeleteFsNodePruneTree();
	
	/**
	 * SQL_DELETE_FS_NODE_PRUNE_CHILDREN
	 * 
	 * @deprecated - replaced with a jpa criteria query
	 * @return
	 */	
	protected abstract String getSqlQueryDeleteFsNodePruneChildren();
	
	// SQL_DELETE_FS_CLOSURE_PRUNE
	protected abstract String getSqlQueryDeleteFsClosurePrune();

}
