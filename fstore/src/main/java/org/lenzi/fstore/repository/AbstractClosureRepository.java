/**
 * 
 */
package org.lenzi.fstore.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.lenzi.fstore.logging.ClosureLogger;
import org.lenzi.fstore.model.util.NodeCopier;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.DBNode;
import org.lenzi.fstore.repository.model.DBPrune;
import org.lenzi.fstore.repository.model.DBTree;
import org.lenzi.fstore.repository.model.impl.FSClosure;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.util.DateUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	ClosureLogger closureLogger;
	
	/**
	 * 
	 */
	public AbstractClosureRepository() {
	
	}
	
	/**
	 * Fetch node, just meta data. No closure data with parent child relationships is fetched.
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#getNode(java.lang.Long)
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
	 * @see org.lenzi.fstore.repository.ClosureRepository#addRootNode(org.lenzi.fstore.repository.model.DBNode)
	 */
	@Override
	public DBNode addRootNode(DBNode newNode) throws DatabaseException {
		
		if(newNode == null){
			throw new DatabaseException("Cannot add new node. Node object is null.");
		}
		
		//logger.info("Add root node");
		
		return addNode(0L, newNode);
		
	}

	/**
	 * Add a new child node.
	 * 
	 * @param parentNode - The parent node under which the new node will be added.
	 * @param newNode - The new node to add.
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#addChildNode(org.lenzi.fstore.repository.model.DBNode, org.lenzi.fstore.repository.model.DBNode)
	 */
	@Override
	public DBNode addChildNode(DBNode parentNode, DBNode newNode) throws DatabaseException {
		
		if(parentNode == null || newNode == null){
			throw new DatabaseException("Cannot add new node. Parent node and/or new node objects are null.");
		}
		if(parentNode.getNodeId() == null){
			throw new DatabaseException("Cannot add new node. Parent node ID is null. This information is needed.");
		}
		
		//logger.info("Add child node");
		
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
		
		// Set node id and parent nod id
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
		
		return newNode;		
		
	}
	
	/**
	 * Get closure data for a node. This will give you all the necessary information to build a tree model.
	 * Usually you would do this for a root node of a tree.
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#getClosure(org.lenzi.fstore.repository.model.DBNode)
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
	 * @see org.lenzi.fstore.repository.ClosureRepository#copyNode(org.lenzi.fstore.repository.model.DBNode, org.lenzi.fstore.repository.model.DBNode, boolean)
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
			
			// TODO - persist this copy (remove closure data?)
			
			if(nodeToCopy.isRootNode()){
				return addRootNode(newCopy);
			}else{
				return addChildNode(parentNode, newCopy);
			}
			
		// copy the node and all children	
		}else{
			
			//logger.info("Copy node with children");
			
			//
			// Get closure data for the sub-tree we are copying
			//
			List<DBClosure> closureList = getClosure(nodeToCopy);
			
			//logger.info("Fetched closure data for node => " + nodeToCopy.getNodeId());
			
			//closureLogger.logClosure(closureList);
			
			if(closureList == null || closureList.size() == 0){
				throw new DatabaseException("Move error. No closure list for node " + nodeToCopy.getNodeId());
			}
			
			// needed for copy node with children...
			//getEntityManager().flush();
			//getEntityManager().clear();
			
			//logger.info("Cleared entity manager.");
			
			HashMap<Long,List<DBNode>> treeMap = new HashMap<Long,List<DBNode>>();
			
			// get the root node of the sub-tree we are copying.
			DBNode rootNode = null;
			for(DBClosure c : closureList){
				if(c.hasParent() && c.hasChild()){
					rootNode = c.getParentNode();
					break;
				}
			}
			
			//logger.info("Found root node for sub-tree id => " + rootNode.getNodeId());
			
			// loop through closure list and build tree map
			DBClosure closure = null;
			for(int closureIndex=0; closureIndex<closureList.size(); closureIndex++){
				closure = closureList.get(closureIndex);
				if(closure.hasParent() && closure.hasChild()){
					if(treeMap.containsKey(closure.getParentNode().getNodeId())){
						treeMap.get(closure.getParentNode().getNodeId()).add(closure.getChildNode());
					}else{
						List<DBNode> childList = new ArrayList<DBNode>();
						childList.add(closure.getChildNode());
						treeMap.put(closure.getParentNode().getNodeId(), childList);
					}
				}
			}
			
			//logger.info("Built tree map in preparation for copy.");
			
			// get children for root node of sub-tree
			List<DBNode> childList = treeMap.get(rootNode.getNodeId());
			
			// add the root node of the sub-tree to the new parent node, then walk the tree and add all the children.
			return copyNodes(nodeToCopy, parentNode, childList, treeMap, copier);			
			
		}		
		
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
	private DBNode copyNodes(DBNode nodeToCopy, DBNode parentNode, List<DBNode> childNodes, 
			HashMap<Long, List<DBNode>> treeMap, NodeCopier copier) throws DatabaseException {
		
		Long copyNodeId = nodeToCopy.getNodeId();
		
		/*
		logger.info("Adding " + nodeToCopy.getNodeId() + " (" + nodeToCopy.getName() + ") to parent " + 
				parentNode.getNodeId() + " (" + parentNode.getName() + ")");
		
		if(childNodes != null && childNodes.size() > 0){
			logger.info("Before copy:");
			logger.info("-Node " + nodeToCopy.getNodeId() + " (" + nodeToCopy.getName() + ") has " + childNodes.size() + 
					" children (including itself, depth-0 link).");
		}
		*/
		
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
		
		// the node id of nodeToCopy has changed!
		
		if(childNodes != null && childNodes.size() > 0){
			
			//logger.info("Node " + nodeToCopy.getNodeId() + " (" + nodeToCopy.getName() + ") has " + childNodes.size() + " children (including itself, depth-0 link).");
			
			for(DBNode childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(childNode.getNodeId() != copyNodeId){
					
					//logger.info("Add next child node " + childNode.getNodeId() + " (" + childNode.getName() + 
					//		") to newly added parent " + newCopy.getNodeId() + " (" + newCopy.getName() + ")");
					
					// recursively add child nodes, and all their children. The new copy node becomes the current root node.
					copyNodes(childNode, newCopy, treeMap.get(childNode.getNodeId()), treeMap, copier);
					
				}
				
			}
		}
		
		return newCopy;
		
	}	

	/**
	 * Add a tree.
	 * 
	 * @see org.lenzi.fstore.repository.ClosureRepository#addTree(org.lenzi.fstore.repository.model.DBTree, org.lenzi.fstore.repository.model.DBNode)
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
	
	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.ClosureRepository#isSameTree(org.lenzi.fstore.repository.model.DbNode, org.lenzi.fstore.repository.model.DbNode)
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

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.ClosureRepository#isParent(org.lenzi.fstore.repository.model.DbNode, org.lenzi.fstore.repository.model.DbNode, boolean)
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
	
	/* (non-Javadoc)
	 * @see org.lenzi.fstore.repository.ClosureRepository#isChild(org.lenzi.fstore.repository.model.DbNode, org.lenzi.fstore.repository.model.DbNode, boolean)
	 */
	@Override
	public boolean isChild(DBNode node1, DBNode node2, boolean fullSearch) throws DatabaseException {

		if(node2.getNodeId() == node1.getParentNodeId()){
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
			for(DBClosure c : node2Children.getChildClosure()){
				if(c.getChildNode().getNodeId() == node1.getNodeId()){
					return true;
				}
			}			
			
		}
		return false;
		
	}
	
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

	@Override
	public void removeNode(Long nodeId) throws DatabaseException {

		logger.debug("remove node " + nodeId);
		
		removeNode(nodeId, true, true);	

	}

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
	/*
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
	*/

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
	/*
	private void moveNodes(Node rootNode, Long newParentId, List<Node> childNodes, HashMap<Long,List<Node>> treeMap, Timestamp dateUpdated) throws DatabaseException {
		
		logger.debug("Adding " + rootNode.getNodeId() + " (" + rootNode.getName() + ") to parent " + newParentId);
		
		// add root node
		reAddNode(rootNode.getNodeId(), newParentId, rootNode.getName(), rootNode.getDateCreated(), dateUpdated);
		
		if(childNodes != null && childNodes.size() > 0){
			
			logger.debug("Node " + rootNode.getNodeId() + " (" + rootNode.getName() + ") has " + childNodes.size() + " children.");
			
			for(Node childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(childNode.getNodeId() != rootNode.getNodeId()){
					
					// recursively add child nodes, and all their children. The next child node becomes the current root node.
					moveNodes(childNode, rootNode.getNodeId(), treeMap.get(childNode.getNodeId()), treeMap, dateUpdated);
					
				}
				
			}
		}
		
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
	
	// SQL_DELETE_FS_NODE_PRUNE_TREE
	protected abstract String getSqlQueryDeleteFsNodePruneTree();
	
	// SQL_DELETE_FS_NODE_PRUNE_CHILDREN
	protected abstract String getSqlQueryDeleteFsNodePruneChildren();
	
	// SQL_DELETE_FS_CLOSURE_PRUNE
	protected abstract String getSqlQueryDeleteFsClosurePrune();

}
