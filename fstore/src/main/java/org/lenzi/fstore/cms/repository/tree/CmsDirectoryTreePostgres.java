package org.lenzi.fstore.cms.repository.tree;

import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.AbstractPostgreSQLTreeRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation=Propagation.REQUIRED)
public class CmsDirectoryTreePostgres extends AbstractPostgreSQLTreeRepository<CmsDirectory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 133464396108585307L;

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

}
