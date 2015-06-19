/**
 * 
 */
package org.lenzi.fstore.core.repository.tree;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.lenzi.fstore.core.logging.ClosureLogger;
import org.lenzi.fstore.core.model.util.NodeCopier;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.model.DBClosure;
import org.lenzi.fstore.core.repository.model.impl.FSClosure;
import org.lenzi.fstore.core.repository.model.impl.FSNode;
import org.lenzi.fstore.core.repository.model.impl.FSTree;
import org.lenzi.fstore.core.repository.tree.query.TreeQueryRepository;
import org.lenzi.fstore.core.service.ClosureMapBuilder;
import org.lenzi.fstore.core.service.TreeBuilder;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.core.tree.TreeNodeVisitException;
import org.lenzi.fstore.core.tree.Trees;
import org.lenzi.fstore.core.tree.Trees.WalkOption;
import org.lenzi.fstore.core.util.CollectionUtil;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.core.repository.model.impl.FSClosure_;
import org.lenzi.fstore.core.repository.model.impl.FSNode_;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Where all the work happens. Contains all code, minus specific database queries, for maintaining tree like
 * structures in a database using a closure table.
 * 
 * Rollbacks by default only happen for unchecked exceptions. In the transaction annotation
 * we add rollbackFor=Throwable.class so rollbacks will happen for checked exceptions as
 * well, e.g., our DatabaseException class.
 * 
 * @see org.lenzi.fstore.core.repository.tree.query.TreeQueryOracleRepository
 * @see org.lenzi.fstore.core.repository.tree.query.TreeQueryPostgresqlRepository
 * 
 * @author sal
 *
 * @param <N>
 */
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public abstract class AbstractTreeRepository<N extends FSNode<N>> extends AbstractRepository implements TreeRepository<N> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9258076299035972L;

	@InjectLogger
	private Logger logger;
	
	// contains database specific queries and methods
	@Autowired
	private TreeQueryRepository queryRepository;
	
	// log closure data
	@Autowired
	private ClosureLogger<N> closureLogger;
	
	// builds a tree object from database tree data
	@Autowired
	private TreeBuilder<N> treeBuilder;
	
	@Autowired
	private ClosureMapBuilder<N> closureMapBuilder;
	
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
	public abstract N postAdd(N node) throws DatabaseException;
	
	/**
	 * Implement to perform an necessary logic when a node is moved.
	 * 
	 * @param node - The node that was moved.
	 * @throws DatabaseException
	 */
	public abstract N postMove(N node) throws DatabaseException;
	
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
	public abstract N postCopy(N originalNode, N newCopyNode) throws DatabaseException;
	
	/**
	 * Fetch node, just meta data. No closure data with parent child relationships is fetched.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getNode(java.lang.Long)
	 */
	@Override
	public N getNode(N node) throws DatabaseException {
		
		return getNodeCriteria(node);
		
	}	

	/**
	 * Fetch a node with it's parent closure data, plus parent and child nodes for all closure entries.
	 */
	@Override
	public N getNodeWithParent(N node) throws DatabaseException {
		
		return getNodeWithParentClosureCriteria(node);
		
	}

	/**
	 * Fetch a node with it's child closure data, plus parent an child nodes for all closure entries.
	 */
	@Override
	public N getNodeWithChild(N node) throws DatabaseException {
		
		//return getNodeWithChildClosureCriteria(node);
		
		return getNodeWithChildClosureHql(node);
		
	}

	/**
	 * Fetch a node with it's child closure data, plus parent an child nodes for all closure entries.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getNodeWithChild(java.lang.Long, java.lang.Class)
	 */
	@Override
	public N getNodeWithChild(Long nodeId, Class<N> clazz) throws DatabaseException {
		
		return getNodeWithChildClosureHql(nodeId, clazz);
		
	}

	/**
	 * 
	 * Fetch a node with it's child closure data, plus parent an child nodes for all closure entries, up to the
	 * specified max depth
	 */
	@Override
	public N getNodeWithChild(N node, int maxDepth) throws DatabaseException {
		
		//return getNodeWithChildClosureCriteria(node, maxDepth);
		
		return getNodeWithChildClosureHql(node, maxDepth);
		
	}

	/**
	 * Fetch a node with its parent AND child closure data, plus parent and child nodes for all closure entries.
	 */
	@Override
	public N getNodewithParentChild(N node) throws DatabaseException {
		
		return getNodeWithParentChildClosureCriteria(node);
		
	}
	
	/**
	 * Fetch the node's parent node. If this node is a root node then null is returned.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getParentNode(org.lenzi.fstore.core.repository.model.impl.FSNode)
	 */
	@Override
	public N getParentNode(N node) throws DatabaseException {
		
		if(node == null){
			throw new DatabaseException("Node parameter is null");
		}
		if(node.getNodeId() == null){
			throw new DatabaseException("Node ID of node parameter is null");
		}
		
		logger.info("Get parent node for node => " + node.getNodeId());
		
		N thisNode = getNodeWithParentClosureCriteria(node);
		if(thisNode == null){
			throw new DatabaseException("Failed to fetch node with it's parent closure data");
		}
		if(thisNode.isRootNode()){
			return null;
		}
		Set<DBClosure<N>> parentClosure = thisNode.getParentClosure();
		if(parentClosure == null || parentClosure.size() == 0){
			throw new DatabaseException("Failed to fetch parent closure data for node => " + thisNode.getNodeId() + ". This is not a root node, it should have parent closure data.");
		}
		
		// loop through closure data and locate the depth-1 entry. this is the closure entry that specifies the node's immediate parent.
		N parentNode = null;
		for(DBClosure<N> closure : CollectionUtil.emptyIfNull(parentClosure)){
			if(closure.getDepth() == 1){
				parentNode = closure.getParentNode();
				break;
			}
		}
		
		if(parentNode == null){
			throw new DatabaseException("Failed to locate parent node from parent closure data. Depth-1 entry not found...");
		}
		
		return parentNode;
	}

	/**
	 * Fetch the first level children of the node (does not include the children's children, etc). If the
	 * node is a leaf node (has no children) then null is returned.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getChildNodes(org.lenzi.fstore.core.repository.model.impl.FSNode)
	 */
	@Override
	public List<N> getChildNodes(N node) throws DatabaseException {

		if(node == null){
			throw new DatabaseException("Node parameter is null");
		}
		if(node.getNodeId() == null){
			throw new DatabaseException("Node ID of node parameter is null");
		}
		
		logger.info("Get child nodes for node => " + node.getNodeId());
		
		//N thisNode = getNodeWithChildClosureCriteria(node);
		N thisNode = getNodeWithChildClosureHql(node, 1);
		
		if(thisNode == null){
			throw new DatabaseException("Failed to fetch node with it's child closure data");
		}
		Set<DBClosure<N>> childClosure = thisNode.getChildClosure();
		if(childClosure == null || childClosure.size() == 0){
			throw new DatabaseException("Failed to fetch child closure data for node => " + thisNode.getNodeId());
		}
		
		// loop through closure data and locate all depth-1 entries. these are the closure entries that contain the first level child nodes
		List<N> children = new ArrayList<N>();
		for(DBClosure<N> closure : CollectionUtil.emptyIfNull(childClosure)){
			if(closure.getDepth() == 1){
				children.add(closure.getChildNode());
			}
		}		
		
		return children.size() > 0 ? children : null;
	}

	/**
	 * Retrieve the root node of the tree that this node belongs too.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getRootNode(org.lenzi.fstore.core.repository.model.impl.FSNode)
	 */
	@Override
	public N getRootNode(N node) throws DatabaseException {

		if(node == null){
			throw new DatabaseException("Node parameter is null");
		}
		if(node.getNodeId() == null){
			throw new DatabaseException("Node ID of node parameter is null");
		}
		
		logger.info("Get parent node for node => " + node.getNodeId());
		
		N thisNode = getNodeWithParentClosureCriteria(node);
		if(thisNode == null){
			throw new DatabaseException("Failed to fetch node with it's parent closure data");
		}
		if(thisNode.isRootNode()){
			return thisNode;
		}
		Set<DBClosure<N>> parentClosure = thisNode.getParentClosure();
		if(parentClosure == null || parentClosure.size() == 0){
			throw new DatabaseException("Failed to fetch parent closure data for node => " + thisNode.getNodeId() + ". This is not a root node, it should have parent closure data.");
		}
		
		// loop through closure data and locate the the node where isRootNode() is true.
		N rootNode = null;
		for(DBClosure<N> closure : CollectionUtil.emptyIfNull(parentClosure)){
			if(closure.getParentNode().isRootNode()){
				rootNode = closure.getParentNode();
				break;
			}
		}
		
		if(rootNode == null){
			throw new DatabaseException("Failed to locate root node from parent closure data...");
		}
		
		return rootNode;
	}

	/**
	 * Add a new root node. Parent node ID will be set to 0.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#addRootNode(org.lenzi.fstore.core.repository.model.DBNode)
	 */
	@Override
	public N addRootNode(N newNode) throws DatabaseException {
		
		if(newNode == null){
			throw new DatabaseException("Cannot add new node. Node object is null.");
		}
		
		N addedNode = addNode(0L, newNode);
		
		// call users custom add node method
		postAdd( addedNode );
		
		return addedNode;		
		
	}

	/**
	 * Add a new child node.
	 * 
	 * @param parentNode - The parent node under which the new node will be added.
	 * @param newNode - The new node to add.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#addChildNode(org.lenzi.fstore.core.repository.model.DBNode, org.lenzi.fstore.core.repository.model.DBNode)
	 */
	@Override
	public N addChildNode(N parentNode, N newNode) throws DatabaseException {
		
		if(parentNode == null || newNode == null){
			throw new DatabaseException("Cannot add new node. Parent node and/or new node objects are null.");
		}
		if(parentNode.getNodeId() == null){
			throw new DatabaseException("Cannot add new node. Parent node ID is null. This information is needed.");
		}
		
		N addedNode = addNode(parentNode.getNodeId(), newNode);
		
		// call users custom add node method
		postAdd( addedNode );
		
		return addedNode;
		
	}
	
	/**
	 * Add a new node.
	 * 
	 * @param parentNodeId - The ID of the parent node under which the new new will be added.
	 * @param newNode - The new node to add.
	 * @param isCopy - True to trigger the postCopy() method, false to trigger to postAdd() method.
	 * @return
	 * @throws DatabaseException
	 */
	private N addNode(Long parentNodeId, N newNode) throws DatabaseException {
		
		if(newNode.getName() == null){
			throw new DatabaseException("Cannot add new node, new node is missing a name. This is a required field.");
		}
		Timestamp dateNow = DateUtil.getCurrentTime();
		newNode.setDateCreated(dateNow);
		newNode.setDateUpdated(dateNow);	
		
		// Get next available node id from sequence
		long nodeId = queryRepository.getSequenceValue(queryRepository.getSqlQueryNodeIdSequence());
		
		// Set node id and parent node id
		newNode.setNodeId(nodeId);
		newNode.setParentNodeId(parentNodeId);
		
		//logger.debug("Adding node " + nodeId + " (" + newNode.getName() + ") to parent node " + parentNodeId);
		
		// Save node to database
		getEntityManager().persist(newNode);
		
		// Get next available link id from sequence
		long linkId = queryRepository.getSequenceValue(queryRepository.getSqlQueryLinkIdSequence());
		
		// Add depth-0 self link to closure table
		DBClosure<N> selfLink = new FSClosure<N>();
		selfLink.setLinkId(linkId);
		selfLink.setChildNodeId(newNode.getNodeId());
		selfLink.setParentNodeId(newNode.getNodeId());
		selfLink.setDepth(0);
		
		// Save closure self link to database
		getEntityManager().persist(selfLink);
		
		// Necessary?
		getEntityManager().flush();
		
		// Add parent-child links to closure table
		Query queryInsertLinks = getEntityManager().createNativeQuery(queryRepository.getSqlQueryInsertMakeParent());
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
	 * Copy the child node, and add the copy to the specified parent.
	 * 
	 * @param parentNodeId - The ID of the parent node where the copy of the new node will be placed under.
	 * @param nodeToCopy - The node to copy
	 * @param copier - The copier which knows how to copy the node.
	 * @return - A reference to the new copy.
	 * @throws DatabaseException
	 */
	private N addChildNodeCopy(N parentNode, N nodeToCopy, NodeCopier<N> copier) throws DatabaseException {
		
		if(parentNode == null || nodeToCopy == null){
			throw new DatabaseException("Cannot copy child node. Parent node and/or child node objects are null.");
		}
		if(parentNode.getNodeId() == null){
			throw new DatabaseException("Cannot copy child node. Parent node ID is null. This information is needed.");
		}		
		
		logger.info("Add copy of child node => " + nodeToCopy.getNodeId() + " under parent node " + parentNode.getNodeId());
		
		// copy node
		N newCopy = copier.copy(nodeToCopy);
		
		// clear any data that will be set by the insert process.
		newCopy.setNodeId(null);
		newCopy.setParentNodeId(null);
		newCopy.setChildClosure(null);
		newCopy.setParentClosure(null);
		
		// add new copy
		newCopy = addNode(parentNode.getNodeId(), newCopy);
		
		// call the post copy method
		postCopy( nodeToCopy, newCopy);		
		
		return newCopy;
		
	}
	
	/**
	 * Copy the root node.
	 * 
	 * @param nodeToCopy - The root node to copy.
	 * @param copier - The copier which knows how to copy the node.
	 * @return - A reference to the new copy.
	 * @throws DatabaseException
	 */
	private N addRootNodeCopy(N nodeToCopy, NodeCopier<N> copier) throws DatabaseException {
		
		if(nodeToCopy == null){
			throw new DatabaseException("Cannot copy root node. Node object is null.");
		}
		
		logger.info("Add copy of root node => " + nodeToCopy.getNodeId());
		
		// copy node
		N newCopy = copier.copy(nodeToCopy);
		
		// clear any data that will be set by the insert process.
		newCopy.setNodeId(null);
		newCopy.setParentNodeId(null);
		newCopy.setChildClosure(null);
		newCopy.setParentClosure(null);
		
		// add new copy
		newCopy = addNode(0L, newCopy);
		
		// call the post copy method
		postCopy( nodeToCopy, newCopy);		
		
		return newCopy;
		
	}	
	
	/**
	 * Get closure data for a node. This will give you all the necessary information to build a tree model.
	 * Usually you would do this for a root node of a tree.
	 * 
	 * UPDATE - Confirmed, the query does properly pull from type N (e.g. FSTestNode, or other). Hibernate magic!
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getClosure(org.lenzi.fstore.core.repository.model.DBNode)
	 */
	@Override
	public List<DBClosure<N>> getClosure(N node) throws DatabaseException {
		
		if(node == null){
			throw new DatabaseException("Cannot fetch closure data for node. Node object is null.");
		}
		if(node.getNodeId() == null){
			throw new DatabaseException("Cannot fetch closure data for node. Node ID is null. This value is needed.");
		}
		
		logger.info("GET CLOSURE FOR NODE, ID => " + node.getNodeId() + ", NAME => " + node.getName() + ", CLASS => " + node.getClass().getCanonicalName());
		
		List<DBClosure<N>> results = null;
		Query query = null;
		try {
			query = getEntityManager().createQuery(queryRepository.getHqlQueryClosureByNodeId());
			query.setParameter(1, node.getNodeId());
			query.setParameter(2, node.getNodeId());			
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage(), e);
		}
		
		results = ResultFetcher.getResultListOrNull(query);
		
		return results;			
		
	}
	
	// TODO - not tested
	/**
	 * Criteria version.
	 * 
	 * @param node
	 * @return
	 * @throws DatabaseException
	 */
	private List<DBClosure<N>> getClosureCriteria(N node) throws DatabaseException {
		
		if(node == null){
			throw new DatabaseException("Cannot fetch closure data for node. Node object is null.");
		}
		if(node.getNodeId() == null){
			throw new DatabaseException("Cannot fetch closure data for node. Node ID is null. This value is needed.");
		}
		
		Class<FSClosure> type = (Class<FSClosure>)FSClosure.class;
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<FSClosure> nodeSelect = criteriaBuilder.createQuery(type);
		Root<FSClosure> nodeSelectRoot = nodeSelect.from(type);
		
		Join<FSClosure,FSNode> childClosureJoin = nodeSelectRoot.join(FSClosure_.childNode, JoinType.LEFT);
		
		Fetch<FSClosure,FSNode> childClosureFetch = nodeSelectRoot.fetch(FSClosure_.childNode, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( criteriaBuilder.equal(nodeSelectRoot.get(FSClosure_.parentNodeId), node.getNodeId()) );
		//andPredicates.add( criteriaBuilder.equal(nodeSelectRoot.get(FSClosure_.depth), MAX_DEPTH) );
		
		nodeSelect.distinct(true);
		nodeSelect.select(nodeSelectRoot);
		nodeSelect.where(
				criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);

		List<DBClosure<N>> result = ResultFetcher.getResultListOrNull( getEntityManager().createQuery(nodeSelect));
		
		return result;
		
	}
	
	/**
	 * Copy a node.
	 */
	@Override
	public N copyNode(N nodeToCopy, N parentNode, boolean copyChildren, NodeCopier<N> copier) throws DatabaseException {
		
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
			
			N newCopy = null;
			if(nodeToCopy.isRootNode()){
				newCopy = addRootNodeCopy(nodeToCopy, copier);
			}else{
				newCopy = addChildNodeCopy(parentNode, nodeToCopy, copier);
			}
			return newCopy;
			
		// copy the node and all children	
		}else{
			
			// Get closure data for the sub-tree we are copying
			List<DBClosure<N>> closureList = getClosure(nodeToCopy);
			
			if(closureList == null || closureList.size() == 0){
				throw new DatabaseException("Move error. No closure list for node " + nodeToCopy.getNodeId());
			}
			
			// needed for copy node with children?
			// TODO - probably not necessary
			getEntityManager().flush();
			getEntityManager().clear();
			
			// get the root node of the sub-tree we are copying.
			N rootNode = null;
			for(DBClosure<N> c : closureList){
				if(c.hasParent() && c.hasChild()){
					rootNode = c.getParentNode();
					break;
				}
			}
			
			//HashMap<Long,List<N>> treeMap = buildMapFromClosure(closureList);
			HashMap<Long,List<N>> treeMap = closureMapBuilder.buildChildMapFromClosure(closureList);

			// get children for root node of sub-tree
			List<N> childList = treeMap.get(rootNode.getNodeId());	
			
			// add the root node of the sub-tree to the new parent node, then walk the tree and add all the children.
			return copyNodes(nodeToCopy, parentNode, childList, treeMap, copier);			
			
		}		
		
	}
	
	/**
	 * Move a node
	 */
	@Override
	public N moveNode(N nodeToMode, N newParentNode) throws DatabaseException {
		
		Long moveNodeId = nodeToMode.getNodeId();
		Long newParentNodeId = newParentNode.getNodeId();
		
		// make sure node being moved is not a root node.
		if(nodeToMode.getParentNodeId() == 0L){
			throw new DatabaseException("Cannot move a root node. Use future rootToLeaf() method...coming soon...");
		}
		
		// make sure new parent node is not a current child of the node that's being moved. you
		// cannot move a tree to under itself! we don't need to worry about this for the copy operation.
		if(isChild(newParentNode, nodeToMode, true)){
			throw new DatabaseException("Cannot move node " + moveNodeId + " to under node " + newParentNodeId + 
					". Node " + newParentNodeId + " is a child of node " + moveNodeId);
		}
		
		logger.debug("Moving node => " + moveNodeId + " to new parent node => " + newParentNodeId);
		
		// Get tree structure for the  branch / tree section we are moving.
		List<DBClosure<N>> closureList = getClosure(nodeToMode);
		if(closureList == null || closureList.size() == 0){
			throw new DatabaseException("Move error. No closure list for node " + moveNodeId);
		}
		//logger.debug("Fetched tree data for moving.");
		//closureLogger.logClosure(closureList);
		
		// Prune the existing data
		logger.debug("Pruning existing tree/branch node " + moveNodeId);
		// when performing a move we don't need (or want) to remove the nodes from the fs_node table. we will simply update the parent_ids.
		// we do want to remove the data from the closure table because the data gets rebuilt correctly during the insert operation.
		removeNode(nodeToMode, false, true);
		// necessary?
		getEntityManager().flush();
		
		// get the root node of the sub-tree we are copying.
		N rootNode = null;
		for(DBClosure<N> c : closureList){
			if(c.hasParent() && c.hasChild()){
				rootNode = c.getParentNode();
				break;
			}
		}
		
		//HashMap<Long,List<N>> treeMap = buildMapFromClosure(closureList);
		HashMap<Long,List<N>> treeMap = closureMapBuilder.buildChildMapFromClosure(closureList);
		
		// get children for root node of sub-tree
		List<N> childList = treeMap.get(rootNode.getNodeId());		
	
		// add the root node to the new parent node, then walk the tree and add all the children.
		return moveNodes(rootNode, newParentNode, childList, treeMap, DateUtil.getCurrentTime());	
		
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
	private N moveNodes(N rootNode, N parentNode, List<N> childNodes, HashMap<Long,List<N>> treeMap, Timestamp dateUpdated) throws DatabaseException {
		
		Long rootNodeId = rootNode.getNodeId();
		Long parentNodeNodeId = parentNode.getNodeId();		
		
		logger.debug("Adding " + rootNodeId + " (" + rootNode.getName() + ") to parent " + parentNodeNodeId);
		
		// re-add node
		rootNode.setParentNodeId(parentNodeNodeId);
		rootNode.setDateUpdated(dateUpdated);
		N updatedRootNode = reAddNode(rootNode);
		
		postMove( updatedRootNode );
		
		if(childNodes != null && childNodes.size() > 0){
			
			logger.debug("Node " + rootNode.getNodeId() + " (" + rootNode.getName() + ") has " + childNodes.size() + " children.");
			
			for(N childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(!childNode.getNodeId().equals(rootNodeId)){
					
					// recursively add child nodes, and all their children. The next child node becomes the current root node.
					moveNodes(childNode, rootNode, treeMap.get(childNode.getNodeId()), treeMap, dateUpdated);
					
				}else{
					logger.debug("Child node is the depth-0 self link. Skipping move.");
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
	private N reAddNode(N node) throws DatabaseException{
		
		// re-add node to database
		N updatedEntity = getEntityManager().merge(node);
		
		// Get next available link id from sequence
		long linkId = queryRepository.getSequenceValue(queryRepository.getSqlQueryLinkIdSequence());
		
		// add depth-0 self link to closure table
		FSClosure<N> selfLink = new FSClosure<N>();
		selfLink.setLinkId(linkId);
		selfLink.setChildNodeId(node.getNodeId());
		selfLink.setParentNodeId(node.getNodeId());	
		selfLink.setDepth(0);
		
		// save closure self link to database
		getEntityManager().persist(selfLink);
		
		// necessary?
		getEntityManager().flush();
		
		// add parent-child links to closure table
		Query queryInsertLinks = getEntityManager().createNativeQuery(queryRepository.getSqlQueryInsertMakeParent());
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
	 * @param copier
	 * @return
	 * @throws DatabaseException
	 */
	private N copyNodes(N nodeToCopy, N parentNode, List<N> childNodes, HashMap<Long, List<N>> treeMap, NodeCopier<N> copier) throws DatabaseException {
		
		Long copyNodeId = nodeToCopy.getNodeId();
		logger.debug("id of node we are copying => " + copyNodeId);
		
		N newCopy = null;
		if(nodeToCopy.isRootNode()){
			newCopy = addRootNodeCopy(nodeToCopy, copier);
		}else{
			newCopy = addChildNodeCopy(parentNode, nodeToCopy, copier);
		}
		
		logger.debug("id of new copy => " + newCopy.getNodeId());
		
		// the node id of nodeToCopy has changed!
		
		if(childNodes != null && childNodes.size() > 0){
			
			for(N childNode : childNodes){
				
				// closure table contains rows where a node is it's own child at depth 0. We want to skip over these.
				if(!childNode.getNodeId().equals(copyNodeId)){
					
					// recursively add child nodes, and all their children. The new copy node becomes the current root node.
					copyNodes(childNode, newCopy, treeMap.get(childNode.getNodeId()), treeMap, copier);
					
				}else{
					logger.debug("Child node is the depth-0 self link. Skipping copy.");
				}
				
			}
		}
		
		return newCopy;
		
	}
	

	/**
	 * Fetch a tree, including it's root node data.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getTree(org.lenzi.fstore.core.repository.model.impl.FSTree)
	 */
	@Override
	public FSTree<N> getTree(FSTree<N> tree) throws DatabaseException {
		
		if(tree == null){
			throw new DatabaseException("Cannot fetch tree. Tree object passed in is null.");
		}
		if(tree.getTreeId() == null){
			throw new DatabaseException("Cannot fetch tree. Tree object contains null tree ID. This value is required.");
		}
		
		return getTreeByIdCriteria(tree);
	}
	
	/**
	 * Fetch a tree, including it's root node data. Also specify the type of the nodes in the tree when performing the join.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#getTree(org.lenzi.fstore.core.repository.model.impl.FSTree, java.lang.Class)
	 */
	@Override
	public FSTree<N> getTree(FSTree<N> tree, Class<N> nodeClass) throws DatabaseException {

		if(tree == null){
			throw new DatabaseException("Cannot fetch tree. Tree object passed in is null.");
		}
		if(tree.getTreeId() == null){
			throw new DatabaseException("Cannot fetch tree. Tree object contains null tree ID. This value is required.");
		}
		if(nodeClass == null){
			throw new DatabaseException("Cannot fetch tree. Node class type was not provided.");
		}
		
		return getTreeByIdCriteriaNodeSpecific(tree, nodeClass);		
		
	}

	/**
	 * Fetch a tree using a criteria query. This does not specify the type of node to join on...
	 * 
	 * @param tree - tree object with ID set.
	 * @return A tree with it's root node.
	 * @throws DatabaseException
	 */
	private FSTree<N> getTreeByIdCriteria(FSTree<N> tree) throws DatabaseException {
		
		logger.info("Getting tree by id, with root node, criteria => " + tree.getTreeId());
		
		Class<FSTree<N>> treeType = (Class<FSTree<N>>) tree.getClass();
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<FSTree<N>> treeSelect = criteriaBuilder.createQuery(treeType);
		
		Root<FSTree<N>> treeRoot = treeSelect.from(treeType);
		//Root<N>  nodeRoot = treeSelect.from(nodeType);
		
		Join<FSTree<N>,N> rootNodeJoin = treeRoot.join("rootNode");
		
		Fetch<FSTree<N>,N> rootNodeFetch =  treeRoot.fetch("rootNode", JoinType.LEFT);
		
		treeSelect.select(treeRoot);
		treeSelect.where(
				criteriaBuilder.equal(treeRoot.get("treeId"), tree.getTreeId())
				);
		
		FSTree<N> result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(treeSelect));
		
		return result;
		
	}
	
	/**
	 * Fetch a tree using a criteria query, and specify the type of nodes when performing the join.
	 * 
	 * @param tree - tree object with ID set.
	 * @param nodeClass - the type of the node in the tree, used when performing the join.
	 * @return
	 * @throws DatabaseException
	 */
	private FSTree<N> getTreeByIdCriteriaNodeSpecific(FSTree<N> tree, Class<N> nodeClass) throws DatabaseException {
		
		logger.info("Getting tree by id, with root node, criteria => " + tree.getTreeId());
		
		Class<FSTree<N>> treeType = (Class<FSTree<N>>) tree.getClass();
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<FSTree<N>> treeSelect = criteriaBuilder.createQuery(treeType);
		
		Root<FSTree<N>> treeRoot = treeSelect.from(treeType);
		//Root<N>  nodeRoot = treeSelect.from(nodeClass);
		
		Join<FSTree<N>,N> rootNodeJoin = treeRoot.join("rootNode" /*, JoinType.LEFT*/);
		
		//Path<N> rootNodePath = treeRoot.get("rootNode");
		
		Fetch<FSTree<N>,N> rootNodeFetch =  treeRoot.fetch("rootNode"/*, JoinType.LEFT*/);
		
		// all AND conditions
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		// where tree id => id the user passed in
		andPredicates.add( criteriaBuilder.equal(treeRoot.get("treeId"), tree.getTreeId()) );
		// add root node type => the class type of the node the user passed in
		//andPredicates.add( criteriaBuilder.equal(rootNodeJoin.type(), criteriaBuilder.literal(nodeClass) ) );		
		
		
		treeSelect.select(treeRoot);
		treeSelect.where(
				criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		FSTree<N> result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(treeSelect));
		
		return result;
		
	}	

	/**
	 * Add a tree.
	 * 
	 * Tree object should contain name.
	 * Root node object should also contain a name.
	 * That is all that is required.
	 * 
	 * @see org.lenzi.fstore.core.repository.tree.TreeRepository#addTree(org.lenzi.fstore.core.repository.model.DBTree, org.lenzi.fstore.core.repository.model.DBNode)
	 */
	@Override
	public FSTree<N> addTree(FSTree<N> newTree, N newRootNode) throws DatabaseException {

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
		
		N rootNode = addRootNode(newRootNode);
		
		// Get next available tree id from sequence
		long treeId = queryRepository.getSequenceValue(queryRepository.getSqlQueryTreeIdSequence());
		
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
	 * Remove a tree
	 * 
	 * @param tree - tree object with a valid tree ID
	 * @throws DatabaseException
	 */
	public void removeTree(FSTree<N> tree) throws DatabaseException {

		if(tree == null){
			throw new DatabaseException("Cannot delete tree. Tree parameter is null.");
		}
		if(tree.getTreeId() == null){
			throw new DatabaseException("Cannot delete tree. Tree parameter contains null tree ID. This value is required.");
		}
		
		FSTree<N> treeToDelete = this.getTree(tree);
		N rootNode = treeToDelete.getRootNode();
		
		removeNode(rootNode, true, true);
		
		getEntityManager().remove(treeToDelete);
		
	}
	
	/**
	 * Are two nodes in the same tree
	 */
	@Override
	public boolean isSameTree(N node1, N node2) throws DatabaseException {

		// both are root nodes. they are not in the same tree
		if(node1.getParentNodeId() == 0L && node2.getParentNodeId() == 0L){
			return false;
		}
		// both are not root nodes, but both have the same parent. they are in the same tree
		if( (node1.getParentNodeId() != 0L && node2.getParentNodeId() != 0L) && (node1.getParentNodeId().equals(node2.getParentNodeId()))){
			return true;
		}
		
		// get root node for each node, and compare IDs.
		N rootNode1 = getRootNode(node1);
		if(rootNode1 == null){
			throw new DatabaseException("Failed to locate the root node (parent most node) for node " + node1.getNodeId());
		}
		N rootNode2 = getRootNode(node2);
		if(rootNode2 == null){
			throw new DatabaseException("Failed to locate the root node (parent most node) for node " + node2.getNodeId());
		}
		if(rootNode1.getNodeId().equals(rootNode2.getNodeId())){
			return true;
		}
		
		return false;
		
	}

	/**
	 * Is node1 a parent of node2
	 */
	@Override
	public boolean isParent(N node1, N node2, boolean fullSearch) throws DatabaseException {

		if(node2.getParentNodeId().equals(node1.getNodeId())){
			return true;
		}
		if(!fullSearch){
			return false;
		}else{
			
			// search all the way up the tree till the root node. if node1 is found, return true.
			N node2Parents = getNodeWithParentClosure(node2);
			if(node2Parents == null || node2Parents.getParentClosure() == null || node2Parents.getParentClosure().size() == 0){
				throw new DatabaseException("Failed to get parent closure and parent node data for node " + node2.getNodeId());
			}
			for(DBClosure<N> c : node2Parents.getParentClosure()){
				if(c.getParentNode().getNodeId().equals(node1.getNodeId())){
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
	public boolean isChild(N node1, N node2, boolean fullSearch) throws DatabaseException {

		if(node2.getNodeId().equals(node1.getParentNodeId())){
			//logger.debug("Node " + node2.getNodeId() + " is and immediate parent of node " + node1.getNodeId());
			return true;
		}
		if(!fullSearch){
			return false;
		}else{
			
			// search all children of node2, till all leaf nodes are reached. If node 1 is found, return true
			N node2Children = getNodeWithChildClosure(node2);
			if(node2Children == null || node2Children.getParentClosure() == null || node2Children.getParentClosure().size() == 0){
				throw new DatabaseException("Failed to get child closure and child node data for node " + node2.getNodeId());
			}
			//logger.debug("Searching all children of node2 => " + node2.getNodeId() + " to see if node " + node1.getNodeId() + " exists");
			for(DBClosure<N> c : node2Children.getChildClosure()){
				//logger.debug("Child found of node " + node2.getNodeId() + " => " + c.getChildNode().getNodeId());
				if(c.getChildNode().getNodeId().equals(node1.getNodeId())){
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
	public N getNodeWithParentClosure(N node) throws DatabaseException {
		
		Query query = null;
		try {
			// TODO - query needs to pull from N node, not FSNode
			query = getEntityManager().createQuery(queryRepository.getHqlQueryNodeWithParentClosure());
			query.setParameter("nodeId", node.getNodeId());
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage(), e);
		}	
		
		N nodeWithParentClosure = ResultFetcher.getSingleResultOrNull(query);
		
		return nodeWithParentClosure;
	}

	/**
	 * Get a node with its child closure data
	 * 
	 * @param node
	 * @return
	 * @throws DatabaseException
	 */
	public N getNodeWithChildClosure(N node) throws DatabaseException {
		
		Query query = null;
		try {
			// TODO - query needs to pull from N node, not FSNode
			query = getEntityManager().createQuery(queryRepository.getHqlQueryNodeWithChildClosure());
			query.setParameter("nodeId", node.getNodeId());
		} catch (IllegalArgumentException e) {
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage(), e);
		}
		
		N nodeWithChildClosure = ResultFetcher.getSingleResultOrNull(query);
		
		return nodeWithChildClosure;
	}
	
	/**
	 * Removes the node and all it's children from the database.
	 */
	@Override
	public void removeNode(N node) throws DatabaseException {
		
		logger.debug("remove node " + node.getNodeId());
		
		//if(node.getParentNodeId() == 0L){
		//	throw new DatabaseException("Cannot remove root node of tree. Use removeTree() method.");
		//}		
		
		// remove node data from main node table, and closure table.
		removeNode(node, true, true);	
		
	}
	
	/**
	 * Removes all the children of the node, but not the node itself.
	 */
	@Override
	public void removeChildren(N node) throws DatabaseException {
		
		N rootDeleteNode = null;
		Tree<N> treeToDelete = null;
		List<N> userNodesToDelete = null;
		
		Long parentNodeId = node.getNodeId();
		
		// Fetch the node with all it's child closure data. We need this data so we can construct a tree model, and perform
		// a "post-order" traversal of the tree (traverse the tree backwards from the furthest leaf nodes, all the way up to
		// the root node.)
		rootDeleteNode = getNodeWithChild(node);
		try {
			treeToDelete = treeBuilder.buildTree(rootDeleteNode);
		} catch (ServiceException e1) {
			throw new DatabaseException("Falied to build tree for the node that is being deleted. Need tree for post-order traveral.");
		}
		
		// Get next available prune id from sequence.
		long pruneId = queryRepository.getSequenceValue(queryRepository.getSqlQueryPruneIdSequence());
		
		// Add list of nodes to delete to our prune table
		Query populatePrune = getEntityManager().createNativeQuery(queryRepository.getSqlQueryInsertPruneChildren());
		populatePrune.setParameter(1, parentNodeId);
		try {
			executeUpdate(populatePrune);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed populate prune table with list of nodes to delete. " +  e.getMessage(), e);
		}
		logger.debug("Added list of nodes to delete to prune table under prune id " + pruneId);
		
		userNodesToDelete = doCriteriaDeleteNode(node, true);
		
		logger.debug("Deleted children of node " + parentNodeId + " from the node table.");	
		
		// Remove children links from closure table.
		// This query uses our prune table. Pass the prune ID which links to all the nodes to prune.
		Query queryDeleteFsClosure = getEntityManager().createNativeQuery(queryRepository.getSqlQueryDeleteFsClosurePrune());
		queryDeleteFsClosure.setParameter(1, pruneId);
		try {
			executeUpdate(queryDeleteFsClosure);
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to remove all children for node " + parentNodeId + ", from FS_CLOSURE. " +  e.getMessage(), e);
		}
		logger.debug("Deleted children of node " + parentNodeId + " from the closure table.");
		
		// allow user access to each node that was deleted so that they may perform post delete cleanup
		//postOrderTraversalRemoveCallback(treeToDelete, true);
		handlePostRemove(treeToDelete, true);
		
	}

	/**
	 * Remove node helper function.
	 * 
	 * @param nodeId The ID of the node to remove.
	 * @param nodeTable - True to remove the node and all child nodes from the FS_NODE table, false otherwise.
	 * @param pruneTable - True to remove all closure entries from the FS_CLOSURE table, false otherwise.
	 */
	private void removeNode(N node, boolean nodeTable, boolean pruneTable) throws DatabaseException {
		
		Long deleteNodeId = node.getNodeId();
		
		logger.debug("remove node " + deleteNodeId + ", nodeTable => " + nodeTable + ", pruneTable => " + pruneTable);
		
		long pruneId = 0;
		N rootDeleteNode = null;
		Tree<N> treeToDelete = null;
		List<N> userNodesToDelete = null;
		
		if(pruneTable){
			
			// Get next available prune id from sequence
			pruneId = queryRepository.getSequenceValue(queryRepository.getSqlQueryPruneIdSequence());
			
			// Add list of nodes to delete to our prune table
			Query populatePrune = getEntityManager().createNativeQuery(queryRepository.getSqlQueryInsertPruneTree());
			populatePrune.setParameter(1, deleteNodeId);
			try {
				executeUpdate(populatePrune);
			} catch (DatabaseException e) {
				throw new DatabaseException("Failed populate prune table with list of nodes to delete. " +  e.getMessage(), e);
			}
			logger.debug("Added list of nodes to delete to prune table under prune id " + pruneId);
			
		}
		
		// this part must happen in the middle. it relies on data in the closure table
		if(nodeTable){
			
			// Fetch the node with all it's child closure data. We need this data so we can construct a tree model, and perform
			// a "post-order" traversal of the tree (traverse the tree backwards from the furthest leaf nodes, all the way up to
			// the root node.)
			rootDeleteNode = getNodeWithChild(node);
			try {
				treeToDelete = treeBuilder.buildTree(rootDeleteNode);
			} catch (ServiceException e1) {
				throw new DatabaseException("Falied to build tree for the node that is being deleted. Need tree for post-order traveral.");
			}			
			
			// TODO = we can get this data from the 'treeToDelete' object.... redundant. 
			userNodesToDelete = doCriteriaDeleteNode(node, false);
			
			logger.debug("Deleted node " + deleteNodeId + " from the node table.");
			
		}
		
		if(pruneTable){
			
			// Remove node depth-0 self link, plus all children links, from closure table.
			// This query uses our prune table. Pass the prune ID which links to all the nodes to prune.
			Query queryDeleteFsClosure = getEntityManager().createNativeQuery(queryRepository.getSqlQueryDeleteFsClosurePrune());
			queryDeleteFsClosure.setParameter(1,pruneId);
			try {
				executeUpdate(queryDeleteFsClosure);
			} catch (DatabaseException e) {
				throw new DatabaseException("Failed to remove node " + deleteNodeId + ", plus all children links, from FS_CLOSURE. " +  e.getMessage(), e);
			}
			logger.debug("Deleted node " + deleteNodeId + " from the closure table.");
			
		}
		
		if(nodeTable){
			
			handlePostRemove(treeToDelete, false);		
			
		}
		
	}
	
	/**
	 * Walks tree in post-order traversal and calls postRemove(N) for each node
	 * 
	 * @param treeToDelete
	 * @throws DatabaseException
	 */
	private void handlePostRemove(Tree<N> treeToDelete, boolean childrenOnly) throws DatabaseException {
	
		if(childrenOnly){
			for(TreeNode<N> child : treeToDelete.getRootNode().getChildren()){
				_handlePostRemove(child);
			}
		}else{
			_handlePostRemove(treeToDelete.getRootNode());
		}		
	}
	private void _handlePostRemove(TreeNode<N> nodeToDelete) throws DatabaseException {
		try {
			Trees.walkTree(nodeToDelete,
					(n) -> { 
						try {
							postRemove(n.getData());
						} catch (DatabaseException e) {
							throw new TreeNodeVisitException("Error calling postRemove(N) for node " + n.getData().toString(), e);
						} 
					},
					WalkOption.POST_ORDER_TRAVERSAL);
		} catch (TreeNodeVisitException e) {
			throw new DatabaseException(e.getMessage(), e.getCause());
		}
	}
	
	/**
	 * Perform a post-order traversal of the tree and call the postRemove(node) callback method for every node that
	 * was deleted.
	 * 
	 * @param treeToDelete
	 * @param childrenOnly - true to delete just the children of the node. False to delete all children, plus the node itself.
	 * @throws DatabaseException
	 * 
	 * @deprecated - replaced with handlePostRemove(..) method
	 */
	/*
	private void postOrderTraversalRemoveCallback(Tree<N> treeToDelete, boolean childrenOnly) throws DatabaseException {
		if(treeToDelete == null){
			throw new DatabaseException("Cannot perform post-order traversal of tree to delete nodes. Tree object is null.");
		}
		if(childrenOnly){
			for(TreeNode<N> childNode : treeToDelete.getRootNode().getChildren()){
				postOrderTraversalRemoveCallback(childNode);
			}
		}else{
			postOrderTraversalRemoveCallback(treeToDelete.getRootNode());
		}
	}
	*/
	/**
	 * @deprecated - replaced with handlePostRemove(..) method
	 */
	/*
	private void postOrderTraversalRemoveCallback(TreeNode<N> nodeToDelete) throws DatabaseException {
		if(nodeToDelete.hasChildren()){
			for(TreeNode<N> childNode : nodeToDelete.getChildren()){
				postOrderTraversalRemoveCallback(childNode);
			}
			postRemove(nodeToDelete.getData());
		}else{
			postRemove(nodeToDelete.getData());
		}
	}
	*/
	
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
	 * @param childrenOnly - false to delete the node and all its children, or true to delete all the children but keep the node itself.
	 * @return A list of all the nodes that were deleted.
	 * @throws DatabaseException
	 */
	private List<N> doCriteriaDeleteNode(N node, boolean childrenOnly) throws DatabaseException {
		
		List<Long> nodeIdList = getNodeIdList(node, childrenOnly);
		
		if(CollectionUtil.isEmpty(nodeIdList)){
			throw new DatabaseException("Failed to get list of node IDs for the " + ((childrenOnly) ? "child" : "") + " nodes. Cannot delete.");
		}
		
		List<N> userNodesToDelete = getNodesCriteria(nodeIdList, node.getClass());
		
		if(CollectionUtil.isEmpty(userNodesToDelete)){
			throw new DatabaseException("Failed to get " + ((childrenOnly) ? "child" : "") + " node data in preparation for deletion. Cannot delete");
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
	public List<Long> getNodeIdList(N node, boolean onlyChildren) throws DatabaseException {
		
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
			//andPredicates.add( criteriaBuilder.greaterThanOrEqualTo(closureRoot.get(FSClosure_.depth), 0) );
			childQuery.where(
					criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
					);
		}else{
			childQuery.where(
					criteriaBuilder.equal(closureRoot.get(FSClosure_.parentNodeId), nodeId)
					);
		}

		nodeIdList = ResultFetcher.getResultListOrNull(getEntityManager().createQuery(childQuery));
		
		return nodeIdList;
		
	}
	
	/**
	 * Retrieve the node, and all nodes under it, along with the nodes closure data, and
	 * the parent and child nodes for the closure data. Set 'onlyChildren' to true to only get
	 * the child nodes.
	 * 
	 * @deprecated - not used anywhere...
	 * 
	 * @param node The node to fetch, plus all child nodes.
	 * @param onlyChildren - true to only get the child nodes, false to include the node you passes in.
	 * @return
	 * @throws DatabaseException
	 */
	/*
	public List<N> getNodes(N node, boolean onlyChildren) throws DatabaseException {
	
		List<Long> nodeIdList = getNodeIdList(node, onlyChildren);
		
		List<N> nodeList = getNodesCriteria(nodeIdList, node.getClass());
		
		return nodeList;
		
	}
	*/
	
	/**
	 * Get a node with it's parent and child closure data, and fetch the parent and child nodes for the closure entries
	 * 
	 * @param node - The node to fetch (with node ID set)
	 * @return
	 * @throws DatabaseException
	 */
	private N getNodeWithParentChildClosureCriteria(N node) throws DatabaseException {
		
		Class<N> type = (Class<N>) node.getClass();
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<N> nodeSelect = criteriaBuilder.createQuery(type);
		Root<N> nodeSelectRoot = nodeSelect.from(type);
		
		SetJoin<N, FSClosure> childClosureJoin = nodeSelectRoot.join(FSNode_.childClosure, JoinType.LEFT);
		SetJoin<N, FSClosure> parentClosureJoin = nodeSelectRoot.join(FSNode_.parentClosure, JoinType.LEFT);
		
		Fetch<N, FSClosure> childClosureFetch =  nodeSelectRoot.fetch(FSNode_.childClosure, JoinType.LEFT);
		Fetch<N, FSClosure> parentClosureFetch =  nodeSelectRoot.fetch(FSNode_.parentClosure, JoinType.LEFT);
		
		childClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		childClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		parentClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		parentClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( criteriaBuilder.equal(nodeSelectRoot.get(FSNode_.nodeId), node.getNodeId()) );
		//andPredicates.add( criteriaBuilder.greaterThan(parentClosureJoin.get(FSClosure_.depth), 0) );
		//andPredicates.add( criteriaBuilder.greaterThan(childClosureJoin.get(FSClosure_.depth), 0) );
		andPredicates.add( criteriaBuilder.greaterThanOrEqualTo(parentClosureJoin.get(FSClosure_.depth), 0) );
		andPredicates.add( criteriaBuilder.greaterThanOrEqualTo(childClosureJoin.get(FSClosure_.depth), 0) );		
		
		nodeSelect.distinct(true);
		nodeSelect.select(nodeSelectRoot);
		nodeSelect.where(
				criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		N result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(nodeSelect));
		
		return result;
	}
	
	/**
	 * The criteria version of this query was not working so here we have the HQL version...
	 * 
	 * @param node
	 * @param maxDepth
	 * @return
	 * @throws DatabaseException
	 */
	private N getNodeWithChildClosureHql(N node, int maxDepth) throws DatabaseException {
		
		String selectQuery =
				"select distinct r from " + node.getClass().getName() + " as r " +
				"inner join fetch r.childClosure cc " +
				"inner join fetch cc.childNode cn " +
				"inner join fetch cc.parentNode pn " +
				"where r.nodeId = :nodeid " +
				"and cc.depth <= :depth";
		
		Query query = getEntityManager().createQuery(selectQuery);
		query.setParameter("nodeid", node.getNodeId());
		query.setParameter("depth", maxDepth);
		
		return ResultFetcher.getSingleResultOrNull(query);
		
	}
	
	/**
	 * The criteria version of this query was not working so here we have the HQL version...
	 * 
	 * @param node
	 * @param maxDepth
	 * @return
	 * @throws DatabaseException
	 */
	private N getNodeWithChildClosureHql(N node) throws DatabaseException {
		
		String selectQuery =
				"select distinct r from " + node.getClass().getName() + " as r " +
				"inner join fetch r.childClosure cc " +
				"inner join fetch cc.childNode cn " +
				"inner join fetch cc.parentNode pn " +
				"where r.nodeId = :nodeid";
		
		Query query = getEntityManager().createQuery(selectQuery);
		query.setParameter("nodeid", node.getNodeId());
		
		return ResultFetcher.getSingleResultOrNull(query);
		
	}
	
	private N getNodeWithChildClosureHql(Long nodeId, Class<N> clazz) throws DatabaseException {
		
		logger.info("getNodeWithChildClosureHql(Long, Class)");
		
		/*
		logger.info("Clazz => " + clazz);
		logger.info("Clazz name => " + clazz.getName());
		logger.info("Clazz canonical name => " + clazz.getCanonicalName());
		logger.info("Clazz simple name => " + clazz.getSimpleName());
		logger.info("Clazz type name => " + clazz.getTypeName());
		
		logger.info("Clazz class => " + clazz.getClass());
		logger.info("Clazz class name => " + clazz.getClass().getName());
		logger.info("Clazz class canonical name => " + clazz.getClass().getCanonicalName());
		logger.info("Clazz class simple name => " + clazz.getClass().getSimpleName());
		logger.info("Clazz class type name => " + clazz.getClass().getTypeName());
		*/
		
		String selectQuery =
				"select distinct r from " + clazz.getCanonicalName() + " as r " +
				"inner join fetch r.childClosure cc " +
				"inner join fetch cc.childNode cn " +
				"inner join fetch cc.parentNode pn " +
				"where r.nodeId = :nodeid";
		
		Query query = getEntityManager().createQuery(selectQuery);
		query.setParameter("nodeid", nodeId);
		
		return ResultFetcher.getSingleResultOrNull(query);
		
	}
	
	/**
	 * @deprecated - the criteria version which takes a second parameter for maxDepth was not work. This one was
	 * also deprecated to be consistent.
	 * 
	 * Get a node with it's child closure data, and fetch the parent and child nodes for the closure entries
	 * 
	 * @param node - The node to fetch (with node ID set)
	 * @return
	 * @throws DatabaseException
	 */
	private N getNodeWithChildClosureCriteria(N node) throws DatabaseException {
		
		logger.info("Getting node with child closure criteria => " + node.getNodeId());
		
		Class<N> type = (Class<N>) node.getClass();
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<N> nodeSelect = criteriaBuilder.createQuery(type);
		Root<N> nodeSelectRoot = nodeSelect.from(type);
		
		SetJoin<N, FSClosure> childClosureJoin = nodeSelectRoot.join(FSNode_.childClosure, JoinType.LEFT);
		
		Fetch<N, FSClosure> childClosureFetch =  nodeSelectRoot.fetch(FSNode_.childClosure, JoinType.LEFT);
		
		childClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		childClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( criteriaBuilder.equal(nodeSelectRoot.get(FSNode_.nodeId), node.getNodeId()) );
		andPredicates.add( criteriaBuilder.greaterThanOrEqualTo(childClosureJoin.get(FSClosure_.depth), 0) );
		
		nodeSelect.distinct(true);
		nodeSelect.select(nodeSelectRoot);
		nodeSelect.where(
				criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		N result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(nodeSelect));
		
		return result;
	}
	
	/**
	 * @deprecated - doesn't seem to work properly, closure entries deeper than maxDepth are wrongfully being returned.
	 * 
	 * Get a node with it's child closure data, and fetch the parent and child nodes for the closure entries.
	 * 
	 * @param node - The node to fetch (with node ID set)
	 * @param maxDepth - fetch only the child closure data up to the specified depth. 
	 * @return
	 * @throws DatabaseException
	 */
	private N getNodeWithChildClosureCriteria(N node, int maxDepth) throws DatabaseException {
		
		logger.info("Getting node with child closure criteria => " + node.getNodeId() + " at max depth " + maxDepth);
		
		Class<N> type = (Class<N>) node.getClass();
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<N> nodeSelect = criteriaBuilder.createQuery(type);
		Root<N> nodeSelectRoot = nodeSelect.from(type);
		
		SetJoin<N, FSClosure> childClosureJoin = nodeSelectRoot.join(FSNode_.childClosure, JoinType.INNER);
		
		Path<Integer> closureDepth = childClosureJoin.get(FSClosure_.depth);
		
		Fetch<N, FSClosure> childClosureFetch = nodeSelectRoot.fetch(FSNode_.childClosure, JoinType.INNER);
		
		childClosureFetch.fetch(FSClosure_.parentNode, JoinType.INNER);
		childClosureFetch.fetch(FSClosure_.childNode, JoinType.INNER);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( criteriaBuilder.equal(nodeSelectRoot.get(FSNode_.nodeId), node.getNodeId()) );
		andPredicates.add( criteriaBuilder.lessThanOrEqualTo(closureDepth, maxDepth) );
		
		nodeSelect.distinct(true);
		nodeSelect.select(nodeSelectRoot);
		nodeSelect.where(
				criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		N result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(nodeSelect));
		
		return result;
	}	
	
	/**
	 * Get a node with it's parent closure data, and fetch the parent and child nodes for the closure entries.
	 * 
	 * @param node - The node to fetch (with node ID set)
	 * @return
	 * @throws DatabaseException
	 */
	private N getNodeWithParentClosureCriteria(N node) throws DatabaseException {
		
		Class<N> type = (Class<N>) node.getClass();
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<N> nodeSelect = criteriaBuilder.createQuery(type);
		Root<N> nodeSelectRoot = nodeSelect.from(type);
		
		SetJoin<N, FSClosure> parentClosureJoin = nodeSelectRoot.join(FSNode_.parentClosure, JoinType.LEFT);
		
		Fetch<N, FSClosure> parentClosureFetch =  nodeSelectRoot.fetch(FSNode_.parentClosure, JoinType.LEFT);
		
		parentClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		parentClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( criteriaBuilder.equal(nodeSelectRoot.get(FSNode_.nodeId), node.getNodeId()) );
		andPredicates.add( criteriaBuilder.greaterThanOrEqualTo(parentClosureJoin.get(FSClosure_.depth), 0) );
		
		nodeSelect.distinct(true);
		nodeSelect.select(nodeSelectRoot);
		nodeSelect.where(
				criteriaBuilder.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		N result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(nodeSelect));
		
		return result;
	}
	
	/**
	 * Get a node without fetching closure data. Just retieve the main node data.
	 * 
	 * @param node - The node to fetch (with node ID set)
	 * @return
	 * @throws DatabaseException
	 */
	private N getNodeCriteria(N node) throws DatabaseException {
		
		Class<N> type = (Class<N>) node.getClass();
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery<N> nodeSelect = criteriaBuilder.createQuery(type);
		Root<N> nodeSelectRoot = nodeSelect.from(type);
		
		//nodeSelect.distinct(true);
		nodeSelect.select(nodeSelectRoot);
		nodeSelect.where(
				criteriaBuilder.equal(nodeSelectRoot.get(FSNode_.nodeId), node.getNodeId())
				);
		
		N result = ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(nodeSelect));
		
		return result;
	}
	
	/**
	 * Get all nodes of the specified type, whose IDs are in the list
	 * 
	 * @param nodeIdList - The list of node IDs
	 * @param c The class type of the node which extends from FSNode
	 * @return
	 * @throws DatabaseException
	 */
	private List<N> getNodesCriteria(List<Long> nodeIdList, Class c) throws DatabaseException {
		
		List<N> nodeList = null;
		
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		
		CriteriaQuery selectNodes = criteriaBuilder.createQuery(c);
		Root nodeSelectRoot = selectNodes.from(c);
		/*
		CriteriaQuery<N> selectNodesToDelete = criteriaBuilder.createQuery(c);
		Root<N> nodeSelectRoot = selectNodesToDelete.from(c);
		*/
		SetJoin childClosureJoin = nodeSelectRoot.join(FSNode_.childClosure, JoinType.LEFT);
		SetJoin parentClosureJoin = nodeSelectRoot.join(FSNode_.parentClosure, JoinType.LEFT);
		Fetch childClosureFetch =  nodeSelectRoot.fetch(FSNode_.childClosure, JoinType.LEFT);
		Fetch parentClosureFetch =  nodeSelectRoot.fetch(FSNode_.parentClosure, JoinType.LEFT);
		childClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		childClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		parentClosureFetch.fetch(FSClosure_.parentNode, JoinType.LEFT);
		parentClosureFetch.fetch(FSClosure_.childNode, JoinType.LEFT);
		selectNodes.distinct(true);
		selectNodes.select(nodeSelectRoot);
		selectNodes.where(
				nodeSelectRoot.get(FSNode_.nodeId).in(nodeIdList)
				);
		
		nodeList = ResultFetcher.getResultListOrNull(getEntityManager().createQuery(selectNodes));
		
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

		logger.debug("Remove tree but keeps nodes.");
		
		if(isSameTree(tree.getRootNode(), newParentNode)){
			throw new DatabaseException("You cannot move the root node of the existing tree to another node under the "
					+ "same tree. New parent node must be a node in a different tree.");			
		}
		
		logger.debug("Not same tree, may proceed with move.");
		
		moveNode(tree.getRootNode().getNodeId(), newParentNode.getNodeId());
		
		logger.debug("Moved nodes...Now deleting old tree");
		
		FSTree treeToDelete = getEntityManager().find(FSTree.class, tree.getTreeId());
		
		getEntityManager().remove(treeToDelete);

	}
	*/
	
	public abstract String getRepositoryName();
	
}
