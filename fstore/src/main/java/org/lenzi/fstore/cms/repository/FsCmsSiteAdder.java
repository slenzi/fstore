/**
 * 
 */
package org.lenzi.fstore.cms.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.core.constants.FsConstants;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.repository.FsDirectoryResourceRepository;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
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
	private FsDirectoryResourceRepository fsDirectoryResourceRepository;
	
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
	public FsCmsSite createResourceStore(Path sitePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		return doCreateSite(sitePath, name, description, clearIfExists);
		
	}
	// helper method for create operation
	private FsCmsSite doCreateSite(Path sitePath, String name, String description, boolean clearIfExists) throws DatabaseException {
		
		// check for existing site paths that will conflict with the new site path
		List<FsCmsSite> conflictingSites = null;
		try {
			conflictingSites = fsCmsSiteRepository.validatePath(sitePath);
		} catch (Exception e) {
			throw new DatabaseException("Error checking for conflicting store paths.", e);
		}
		if(conflictingSites != null && conflictingSites.size() > 0){
			StringBuffer buf = new StringBuffer();
			buf.append("The following existing sites conflict with the new site path " + sitePath.toString() + 
					System.getProperty("line.separator"));
			buf.append("New site path must not be the same as an existing site path. Additionally, new path "
					+ "must not be a child directory of an existing site path, and must not be a parent directory of "
					+ "an existing site path." + System.getProperty("line.separator"));
			for(FsCmsSite site : conflictingSites){
				buf.append("Store name: " + site.getName() + ", store path: " + site.getPath() + System.getProperty("line.separator"));
			}
			throw new DatabaseException(buf.toString());
		}
 		
		//
		// Create root directory for new site
		//
		FsDirectoryResource siteRootDirectory = null;
		try {
			siteRootDirectory = (FsDirectoryResource) treeRepository.addRootNode(
					new FsDirectoryResource(0L, sitePath.getFileName().toString(), FsConstants.FILE_SEPARATOR));
		} catch (DatabaseException e) {
			throw new DatabaseException("Failed to create root directory tree node for cms site, name => " + 
					name + ", path => " + sitePath.toString(), e);
		}
		
		// all paths in database use forward slash
		String convertedSitePath = sitePath.toString();
		convertedSitePath = convertedSitePath.replace("\\", FsConstants.FILE_SEPARATOR);
		
		//
		// create new file store and save to db
		//
		FsCmsSite cmsSite = new FsCmsSite();
		cmsSite.setName(name);
		cmsSite.setDescription(description);
		cmsSite.setNodeId(siteRootDirectory.getNodeId());
		cmsSite.setPath(convertedSitePath);
		cmsSite.setDateCreated(DateUtil.getCurrentTime());
		cmsSite.setDateUpdated(DateUtil.getCurrentTime());
		try {
			persist(cmsSite);
		}catch(DatabaseException e){
			throw new DatabaseException("Error saving cms site entry to database. ", e);
		}
		getEntityManager().flush();
		

		//
		// Update root dir store id
		//
		try {
			siteRootDirectory.setStoreId(cmsSite.getStoreId());
			merge(siteRootDirectory);
		}catch(DatabaseException e){
			throw new DatabaseException("Error updating root dir store id. ", e);
		}
		
		// want to avoid insert operation...
		cmsSite.setRootDirectory(siteRootDirectory);
		
		//
		// Create directory on file system
		//
		try {
			fsResourceHelper.createDirOnFileSystem(sitePath, true);
		} catch (SecurityException | IOException e) {
			throw new DatabaseException("Error creating directory on local file system. ", e);
		}
		
		return cmsSite;		
		
	}

}
