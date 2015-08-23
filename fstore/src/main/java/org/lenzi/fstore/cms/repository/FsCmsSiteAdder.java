/**
 * 
 */
package org.lenzi.fstore.cms.repository;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;

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
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.lenzi.fstore.web.util.AppServices;
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
	private AppServices appServices;
	
    @Autowired
    private ManagedProperties appProps; 
	
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
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws DatabaseException
	 */
	public FsCmsSite createCmsSite(String name, String description, boolean clearIfExists) throws DatabaseException {
		
		return doCreateSite(name, description, clearIfExists);
		
	}
	// helper method for create operation
	private FsCmsSite doCreateSite(String name, String description, boolean clearIfExists) throws DatabaseException {
		
		String appPath = appServices.getRuntimePath();
		String offline = appProps.getProperty("cms.sites.offline");
		String online = appProps.getProperty("cms.sites.online");
		
		Path siteOfflinePath = Paths.get(appPath + offline + File.separator + name);
		Path siteOnlinePath = Paths.get(appPath + online + File.separator + name);
		
		String offlineStoreName = "CMS Site Offline: " + name;
		String onlineStoreName = "CMS Site Online: " + name;
		
		// create offline resource store
		FsResourceStore offlineStore = null;
		try {
			offlineStore = fsResourceStoreAdder.createResourceStore(siteOfflinePath, offlineStoreName, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error creating offline resource store for new cms site. Path = " + siteOfflinePath, e);
		}
		
		// create online resource store
		FsResourceStore onlineStore = null;
		try {
			onlineStore = fsResourceStoreAdder.createResourceStore(siteOnlinePath, onlineStoreName, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new DatabaseException("Error creating online resource store for new cms site. Path = " + siteOnlinePath, e);
		}
		
		// create new cms site
		Timestamp nowTime = DateUtil.getCurrentTime();
		FsCmsSite cmsSite = new FsCmsSite();
		cmsSite.setName(name);
		cmsSite.setDescription(description);
		cmsSite.setOfflineStoreId(offlineStore.getStoreId());
		cmsSite.setOnlineStoreId(onlineStore.getStoreId());
		cmsSite.setDateCreated(nowTime);
		cmsSite.setDateUpdated(nowTime);
		try {
			persist(cmsSite);
		}catch(DatabaseException e){
			throw new DatabaseException("Error saving cms site entry to database. ", e);
		}
		getEntityManager().flush();
		
		return cmsSite;		
		
	}

}
