/**
 * 
 */
package org.lenzi.fstore.cms.repository;

import java.nio.file.Path;

import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.repository.FsResourceStoreAdder;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For adding/creating cms sites
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Throwable.class)
public class FsCmsSiteAdder extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7552781703574519309L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	@Autowired
	private FsResourceStoreAdder fsResourceStoreAdder;
	
	@Autowired
	private FsCmsSiteRepository fsCmsSiteRepository;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;

	/**
	 * 
	 */
	public FsCmsSiteAdder() {
		
	}
	
	/**
	 * Create cms site
	 * 
	 * @param sitePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws DatabaseException
	 */
	public FsCmsSite createCmsSite(Path sitePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		return doCreateSite(sitePath, name, description, clearIfExists);
		
	}
	// helper method for create operation
	private FsCmsSite doCreateSite(Path sitePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		// create resource store for storing cms files
		FsResourceStore cmsStore = null;
		try {
			cmsStore = fsResourceStoreAdder.createResourceStore(sitePath, name, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error creating resource store for new cms site. Path = " + sitePath, e);
		}
		
		// create new cms site
		FsCmsSite cmsSite = new FsCmsSite();
		cmsSite.setName(name);
		cmsSite.setDescription(description);
		cmsSite.setStoreId(cmsStore.getStoreId());
		cmsSite.setDateCreated(DateUtil.getCurrentTime());
		cmsSite.setDateUpdated(DateUtil.getCurrentTime());
		try {
			persist(cmsSite);
		}catch(DatabaseException e){
			throw new DatabaseException("Error saving cms site entry to database. ", e);
		}
		getEntityManager().flush();
		
		return cmsSite;		
		
	}

}
