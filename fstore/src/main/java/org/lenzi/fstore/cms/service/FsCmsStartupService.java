package org.lenzi.fstore.cms.service;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNodeVisitException;
import org.lenzi.fstore.core.tree.Trees;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Makes sure the RFS has the same data as the fstore CMS VFS. 
 * 
 * @author sal
 */
@Service
public class FsCmsStartupService {

	@InjectLogger
	private Logger logger;		
	
	@Autowired
	private FsCmsService fsCmsService;
	
	@Autowired
	private FsResourceService fsResourceService;
	
    private ExecutorService executor = Executors.newSingleThreadExecutor();		
	
	public FsCmsStartupService() {
		
	}
	
	/**
	 * On startup sync RFS with VFS for all CMS sites
	 */
	@PostConstruct
	private void init(){
		
		executor.submit(() -> {
			
			List<FsCmsSite> sites = null;
			try {
				sites = fsCmsService.getAllSites();
			} catch (ServiceException e) {
				logger.error("Cannot sync RFS with VFS. Failed to fetch CMS sites. " + e.getMessage(), e);
			}
			if(sites != null && sites.size() > 0){
				for(FsCmsSite site : sites){
					try {
						syncSite(site);
					} catch (ServiceException e) {
						logger.error("Failed to sync RFS with VFS for CMS site '[id=" + site.getSiteId() 
								+ ", name=" + site.getName()+ "]'. " + e.getMessage(), e);
					}
				}
			}
			
		});
		
	}
	
	/**
	 * Sync RFS with VFS for site's offline and online resource stores.
	 * 
	 * @param site
	 * @throws ServiceException
	 */
	private void syncSite(FsCmsSite site) throws ServiceException {
		
		FsResourceStore offlineStore = site.getOfflineResourceStore();
		FsResourceStore onlineStore = site.getOnlineResourceStore();
		
		if(offlineStore != null){
			syncStore(offlineStore);
		}else{
			logger.error("Offline resource store for site '[id=" + site.getSiteId() 
					+ ", name=" + site.getName()+ "]' is null. Cannont sync offline RFS with VFS.");
		}
		
		if(onlineStore != null){
			syncStore(onlineStore);
		}else{
			logger.error("Online resource store for site '[id=" + site.getSiteId() 
					+ ", name=" + site.getName()+ "]' is null. Cannont sync online RFS with VFS.");
		}	
		
	}
	
	/**
	 * Sync resource store RFS with VFS
	 * 
	 * @param store
	 * @throws ServiceException
	 */
	private void syncStore(FsResourceStore store) throws ServiceException {
		
		Long rootDirId = store.getNodeId();
		
		Tree<FsPathResource> resourceTree = fsResourceService.getTree(rootDirId);
		
		final String storePath = store.getStorePath();
		
		logger.info("Store RFS-VFS sync: [id=" + store.getNodeId() + ", name=" + store.getName() + ", path=" + store.getStorePath() + "]");
		
		try {
			Trees.walkTree(
					resourceTree,
					(treeNode) -> {
						
						FsPathResource pathResource = treeNode.getData();
						
						try {
							syncResource(store, pathResource);
						} catch (ServiceException e) {
							throw new TreeNodeVisitException("Failed to sync path resource " +
									"[node id=" + pathResource.getNodeId() + ", name=" + pathResource.getName() + 
									", type=" + pathResource.getPathType().toString() + 
									", path=" + storePath + pathResource.getRelativePath() + "]. " + e.getMessage(), e);
						}
						
					}
					, Trees.WalkOption.PRE_ORDER_TRAVERSAL);
		} catch (TreeNodeVisitException e) {
			throw new ServiceException("Error while walking resource store tree '[store id = " + 
					store.getStoreId() + ", store name = " + store.getName() + "]' to sync RFS with VFS. " + e.getMessage());
		}
		
	}
	
	/**
	 * Sync path resource
	 * 
	 * @param store
	 * @param pathResource
	 * @throws ServiceException
	 */
	private void syncResource(FsResourceStore store, FsPathResource pathResource) throws ServiceException {
		
		final String storePath = store.getStorePath();
		final String resourceStringPath = storePath + pathResource.getRelativePath();
		final Path resourcePath = Paths.get(resourceStringPath);
		boolean rfsExists = Files.exists(resourcePath);
		
		logger.info("Resource RFS-VFS sync => " + resourceStringPath + ", (" + rfsExists + ")");
		
		if(!rfsExists){
			if(pathResource.getPathType().equals(FsPathType.DIRECTORY)){
				try {
					FileUtil.createDirectory(resourcePath, true);
				} catch (IOException e) {
					throw new ServiceException("Faile to create RFS directory " + resourcePath.toString() + ". " + e.getMessage(), e);
				}
			}else{
				writeVfsFileResourceToRfs(store, pathResource);
			}
		}
		
	}
	
	/**
	 * Sync file resource
	 * 
	 * @param store
	 * @param pathResource
	 * @throws ServiceException
	 */
	private void writeVfsFileResourceToRfs(FsResourceStore store, FsPathResource pathResource) throws ServiceException {
		
		final String storePath = store.getStorePath();
		final String resourceStringPath = storePath + pathResource.getRelativePath();		
		final Path resourcePath = Paths.get(resourceStringPath);
		
		logger.info("Fetching data from VFS and writing to RFS => " + resourceStringPath);
		
		FsFileMetaResource fileMetaResource = null;
		try {
			fileMetaResource = fsResourceService.getFileResourceById(pathResource.getNodeId(), FsFileResourceFetch.FILE_META_WITH_DATA);
		} catch (ServiceException e) {
			throw new ServiceException("Failed to fetch file resource for node id " + pathResource.getNodeId() + 
					" while attempting to sync RFS with VFS. " + e.getMessage(), e);
		}
		
		FsFileResource fileResource = fileMetaResource.getFileResource();
		
		try {
			
			Files.write(resourcePath, fileResource.getFileData());
			
		} catch (FileAlreadyExistsException e){
			throw buildServiceExceptionWriteError(resourcePath, e);
		} catch (DirectoryNotEmptyException e){
			throw buildServiceExceptionWriteError(resourcePath, e);
		} catch (IOException e) {
			throw buildServiceExceptionWriteError(resourcePath, e);
		} catch (SecurityException e) {
			throw buildServiceExceptionWriteError(resourcePath, e);
		}
		
	}
	
	private ServiceException buildServiceExceptionWriteError(Path target, Throwable e){
		
		StringBuffer buf = new StringBuffer();
		String cr = System.getProperty("line.separator");
		buf.append("RFS-VFS sync failure, error writing file => " + target.toString() + cr);
		buf.append("Throwable => " + e.getClass().getName() + cr);
		buf.append("Message => " + e.getMessage() + cr);
		return new ServiceException(buf.toString(), e);
		
	}		
	
	@PreDestroy
	private void cleanup(){
		
		executor.shutdownNow();
		
	}	

}
