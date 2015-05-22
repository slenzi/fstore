package org.lenzi.fstore.cms.repository.tree;

import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.AbstractTreeRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation=Propagation.REQUIRED)
public class CmsDirectoryTreeOracle extends AbstractTreeRepository<CmsDirectory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1518745351840755333L;

	@Override
	public CmsDirectory postAdd(CmsDirectory node) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CmsDirectory postMove(CmsDirectory node) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postRemove(CmsDirectory node) throws DatabaseException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CmsDirectory postCopy(CmsDirectory originalNode,
			CmsDirectory newCopyNode) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRepositoryName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected long getSequenceVal(String nativeSequenceQuery)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getSqlQueryNodeIdSequence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryLinkIdSequence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryPruneIdSequence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryTreeIdSequence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getHqlQueryNodeById() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getHqlQueryTreeById() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getHqlQueryNodeWithParentClosure() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getHqlQueryNodeWithChildClosure() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getHqlQueryClosureByNodeId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryInsertMakeParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryInsertPruneTree() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryInsertPruneChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryDeleteFsNodePruneTree() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryDeleteFsNodePruneChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSqlQueryDeleteFsClosurePrune() {
		// TODO Auto-generated method stub
		return null;
	}

}
