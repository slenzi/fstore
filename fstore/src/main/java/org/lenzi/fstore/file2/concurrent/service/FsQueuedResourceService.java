package org.lenzi.fstore.file2.concurrent.service;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.file2.concurrent.task.AbstractFsTask;
import org.lenzi.fstore.file2.concurrent.task.FsQueuedTaskManager;
import org.lenzi.fstore.file2.concurrent.task.FsSpringHelper;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resource service which queues operations in a blocking queue.
 * 
 * @author slenzi
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsQueuedResourceService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsQueuedTaskManager taskManager;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	@Autowired
	private FsSpringHelper taskCreator;
	
	private ExecutorService taskExecutorService = null;
	
	public FsQueuedResourceService() {
		
	}
	
	/**
	 * Start the task manager
	 */
	@PostConstruct
	private void init(){
		
		logger.info("Starting queued task manager...");
		
		taskExecutorService = Executors.newSingleThreadExecutor();
		
		taskManager.setManagerName("Queued Resource Service");
		
		taskManager.startTaskManager(taskExecutorService);
		
		logger.info("Startup complete.");
		
	}
	
	/**
	 * Shutdown the task manager
	 */
	@PreDestroy
	private void cleanup(){
		
		logger.info("Shutting down queued task manager...");
		
		taskManager.stopTaskManager();
		
		if(!taskExecutorService.isShutdown()){
			logger.error("Executor service not shutdown...");
		}
		
		taskExecutorService = null;
		
		logger.info("Shutdown complete.");
		
	}
	
	/**
	 * Get path resource tree for directory.
	 * 
	 * @param dirId
	 * @return
	 * @throws ServiceException
	 */
	public Tree<FsPathResource> getPathResourceTree(Long dirId) throws ServiceException {
		
		class Task extends AbstractFsTask<Tree<FsPathResource>> {

			@Override
			public Tree<FsPathResource> doWork() throws ServiceException {
				return fsResourceService.getTree(dirId);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		Tree<FsPathResource> resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Get path resource tree for directory, up to the max depth
	 * 
	 * @param dirId
	 * @param maxDepth
	 * @return
	 * @throws ServiceException
	 */
	public Tree<FsPathResource> getPathResourceTree(Long dirId, int maxDepth) throws ServiceException {
		
		class Task extends AbstractFsTask<Tree<FsPathResource>> {

			@Override
			public Tree<FsPathResource> doWork() throws ServiceException {
				return fsResourceService.getPathResourceTree(dirId, maxDepth);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		Tree<FsPathResource> resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Get tree for node parent data
	 * 
	 * @param dirId
	 * @return
	 * @throws ServiceException
	 */
	public Tree<FsPathResource> getParentPathResourceTree(Long dirId) throws ServiceException {
		
		class Task extends AbstractFsTask<Tree<FsPathResource>> {

			@Override
			public Tree<FsPathResource> doWork() throws ServiceException {
				return fsResourceService.getParentPathResourceTree(dirId);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		Tree<FsPathResource> resource = t.get(); // block until complete
		
		return resource;		
		
	}
	
	/**
	 * Get a file resource by id
	 * 
	 * @param fileId - id of the file resource
	 * @param fetch - specify file data to fetch
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource getFileResourceById(Long fileId, FsFileResourceFetch fetch) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.getFileResourceById(fileId, fetch);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Get a file resource by path
	 * 
	 * @param path - resource store root dir name + file relative path
	 * @param fetch - specify file data to fetch
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource getFileResourceByPath(String path, FsFileResourceFetch fetch) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.getFileResourceByPath(path, fetch);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Get resource store by store id
	 * 
	 * @param storeId - the ID of the store
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getResourceStoreById(final Long storeId) throws ServiceException {
		
		class Task extends AbstractFsTask<FsResourceStore> {

			@Override
			public FsResourceStore doWork() throws ServiceException {
				return fsResourceService.getResourceStoreById(storeId);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsResourceStore resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Get resource store by store name. Store names should be unique, so only
	 * one store object is returned (if a store with the provided name exists.)
	 * 
	 * @param storeName
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getResourceStoreByName(final String storeName) throws ServiceException {
		
		class Task extends AbstractFsTask<FsResourceStore> {

			@Override
			public FsResourceStore doWork() throws ServiceException {
				return fsResourceService.getStoreByName(storeName);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsResourceStore resource = t.get(); // block until complete
		
		return resource;
		
	}	
	
	/**
	 * Get all resource stores
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public List<FsResourceStore> getAllStores() throws ServiceException {
		
		class Task extends AbstractFsTask<List<FsResourceStore>> {

			@Override
			public List<FsResourceStore> doWork() throws ServiceException {
				return fsResourceService.getAllStores();
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		List<FsResourceStore> resource = t.get(); // block until complete
		
		return resource;		
		
	}
	
	/**
	 * Fetch the resource store for a specific path resource, e.g. for any directory resource or file
	 * meta resource.
	 * 
	 * @param resourceId - the id of the resource, e.g. id of a FsDirectoryResource, or FsFileMetaResource,
	 * 	or some other resource in the tree which extends from FsPathResource.
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getResourceStoreByPathResourceId(final Long resourceId) throws ServiceException {
		
		class Task extends AbstractFsTask<FsResourceStore> {

			@Override
			public FsResourceStore doWork() throws ServiceException {
				return fsResourceService.getResourceStoreByPathResourceId(resourceId);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsResourceStore resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Add file resource
	 * 
	 * @param filePath - path to file to be added
	 * @param dirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file, false not to.
	 * @return reference to the newly added file
	 * @throws ServiceException
	 */
	public FsFileMetaResource addFileResource(Path filePath, Long dirId, boolean replaceExisting) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.addFileResource(filePath, dirId, replaceExisting);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Add file resource
	 * 
	 * @param fileName - name of new file
	 * @param fileBytes - file data bytes
	 * @param dirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file, false not to.
	 * @param storeInDatabase - true to store file binary data in database AND on file system, false to only store file on file system.
	 * @return reference to the newly added file
	 * @throws ServiceException
	 */
	public FsFileMetaResource addFileResource(String fileName, byte[] fileBytes, Long dirId, boolean replaceExisting, boolean storeInDatabase) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.addFileResource(fileName, fileBytes, dirId, replaceExisting, storeInDatabase);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource resource = t.get(); // block until complete
		
		return resource;
		
	}	
	
	/**
	 * Add list of files
	 * 
	 * @param filePaths
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public List<FsFileMetaResource> addFileResource(List<Path> filePaths, Long dirId, boolean replaceExisting) throws ServiceException {
		
		class Task extends AbstractFsTask<List<FsFileMetaResource>> {

			@Override
			public List<FsFileMetaResource> doWork() throws ServiceException {
				return fsResourceService.addFileResource(filePaths, dirId, replaceExisting);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		List<FsFileMetaResource> resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Add / replace file resource meta. Binary data is not store in teh database. A 1-byte placeholder will be added
	 * in place of the binary data.
	 * 
	 * @param fileToAdd
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource addFileResourceMeta(Path fileToAdd, Long dirId, boolean replaceExisting) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.addFileResourceMeta(fileToAdd, dirId, replaceExisting);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Reads the file data from disk and re-writes it to the database for the existing file entry.
	 * 
	 * @param fileId
	 * @param store
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource syncDatabaseBinary(Long fileId, FsResourceStore store) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.syncDatabaseBinary(fileId, store);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource updatedResource = t.get(); // block until complete
		
		return updatedResource;
		
	}
	
	/**
	 * Add directory resource
	 * 
	 * @param name
	 * @param parentDirId
	 * @return
	 * @throws ServiceException
	 */
	public FsDirectoryResource addDirectoryResource(Long parentDirId, String name) throws ServiceException {
		
		class Task extends AbstractFsTask<FsDirectoryResource> {

			@Override
			public FsDirectoryResource doWork() throws ServiceException {
				return fsResourceService.addDirectoryResource(parentDirId, name);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsDirectoryResource resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Add resource store
	 * 
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExist
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore addResourceStore(Path storePath, String name, String description, boolean clearIfExists) throws ServiceException {
		
		class Task extends AbstractFsTask<FsResourceStore> {

			@Override
			public FsResourceStore doWork() throws ServiceException {
				return fsResourceService.createResourceStore(storePath, name, description, clearIfExists);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsResourceStore resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * remove file
	 * 
	 * @param fileId
	 * @throws ServiceException
	 */
	public void removeFileResource(Long fileId) throws ServiceException {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
				fsResourceService.removeFileResource(fileId);
				return null;
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		t.waitComplete(); // block until complete
		
	}
	
	/**
	 * remove list of files
	 * 
	 * @param fileIdList
	 * @throws ServiceException
	 */
	public void removeFileResourceList(List<Long> fileIdList) throws ServiceException {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
				for(Long fileId : fileIdList){
					fsResourceService.removeFileResource(fileId);
				}
				return null;
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		t.waitComplete(); // block until complete
		
	}
	
	/**
	 * remove directory
	 * 
	 * @param dirId
	 * @throws ServiceException
	 */
	public void removeDirectoryResource(Long dirId) throws ServiceException {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
				fsResourceService.removeDirectoryResource(dirId);
				return null;
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		t.waitComplete(); // block until complete
		
	}
	
	/**
	 * Remove list of directories
	 * 
	 * @param dirIdList
	 * @throws ServiceException
	 */
	public void removeDirectoryResourceList(List<Long> dirIdList) throws ServiceException {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
				for(Long dirId : dirIdList){
					fsResourceService.removeDirectoryResource(dirId);
				}
				return null;
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		t.waitComplete(); // block until complete
		
	}	
	
	/**
	 * Remove resource store 
	 * 
	 * @param storeId
	 * @throws ServiceException
	 */
	public void removeResourceStore(Long storeId) throws ServiceException {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
				fsResourceService.removeResourceStore(storeId);
				return null;
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		t.waitComplete(); // block until complete
		
	}
	
	/**
	 * Copy file resource
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource copyFileResource(Long fileId, Long dirId, boolean replaceExisting) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.copyFileResource(fileId, dirId, replaceExisting);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * move file resource
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource moveFileResource(Long fileId, Long dirId, boolean replaceExisting) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.moveFileResource(fileId, dirId, replaceExisting);
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		FsFileMetaResource resource = t.get(); // block until complete
		
		return resource;
		
	}
	
	/**
	 * Copy directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 * @throws ServiceException
	 */
	public void copyDirectoryResource(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws ServiceException {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
				fsResourceService.copyDirectoryResource(sourceDirId, targetDirId, replaceExisting);
				return null;
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		t.waitComplete(); // block until complete		
		
	}
	
	/**
	 * Move directory
	 * 
	 * @param sourceDirId
	 * @param targetDirId
	 * @param replaceExisting
	 * @throws ServiceException
	 */
	public void moveDirectoryResource(Long sourceDirId, Long targetDirId, boolean replaceExisting) throws ServiceException {
		
		class Task extends AbstractFsTask<Void> {

			@Override
			public Void doWork() throws ServiceException {
				fsResourceService.moveDirectoryResource(sourceDirId, targetDirId, replaceExisting);
				return null;
			}

			@Override
			public Logger getLogger() {
				return logger;
			}
			
		};
		Task t = new Task();
		taskManager.addTask(t);
		
		t.waitComplete(); // block until complete		
		
	}	
	
	//private ExecutorService executorService = Executors.newFixedThreadPool(20);
	
	/*
	private BiConsumer<FsFileMetaResource, Throwable> fsFileMetaResourceConsumer = (resource, throwable) -> {
		logger.info( resource != null ? resource.toString() : "No resource to consume");
		logger.info( throwable != null ? throwable.getMessage() + ", Cause =>" + throwable.getCause().getMessage() : "No throwable to consume");
    };
    */
	
	/*
	CompletableFuture<FsFileMetaResource> addFileFuture = CompletableFuture
			.supplyAsync(
					() -> supplyNewFile(filePath, dirId, replaceExisting),
					executorService)
			.whenComplete(fsFileMetaResourceConsumer);
	
	CompletableFuture<FsFileMetaResource> addFileFuture2 = CompletableFuture
			.supplyAsync(
					() -> supplyNewFile(filePath, dirId, replaceExisting),
					executorService)
			.whenComplete(fsFileMetaResourceConsumer);
	
	try {
		resource = addFileFuture.get();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/
	
	/*
	private FsFileMetaResource supplyNewFile(Path filePath, Long dirId, boolean replaceExisting){
		
		// TODO - queue the file to be added and block until done,
		
		try {
			return fsResourceService.addFileResource(filePath, dirId, replaceExisting);
		} catch (ServiceException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}
	*/

}
