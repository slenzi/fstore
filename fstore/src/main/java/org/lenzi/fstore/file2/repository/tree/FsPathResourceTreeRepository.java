/**
 * 
 */
package org.lenzi.fstore.file2.repository.tree;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.AbstractTreeRepository;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sal
 *
 */
@Repository("FsPathResourceTree")
@Transactional(propagation=Propagation.REQUIRED)
public class FsPathResourceTreeRepository extends AbstractTreeRepository<FsPathResource> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5739302631515697345L;

	@Override
	public FsPathResource postAdd(FsPathResource node) throws DatabaseException {
	
		return null;
	}

	@Override
	public FsPathResource postMove(FsPathResource node) throws DatabaseException {

		return null;
	}

	@Override
	public void postRemove(FsPathResource node) throws DatabaseException {

		
	}

	@Override
	public FsPathResource postCopy(FsPathResource originalNode, FsPathResource newCopyNode) throws DatabaseException {

		return null;
	}

	@Override
	public String getRepositoryName() {
		// TODO Auto-generated method stub
		return null;
	}

}
