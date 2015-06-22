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
			logger.error("Executor service not shutdow...");
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
	 * Get a file resource
	 * 
	 * @param fileId
	 * @param fetch
	 * @return
	 * @throws ServiceException
	 */
	public FsFileMetaResource getFileResource(Long fileId, FsFileResourceFetch fetch) throws ServiceException {
		
		class Task extends AbstractFsTask<FsFileMetaResource> {

			@Override
			public FsFileMetaResource doWork() throws ServiceException {
				return fsResourceService.getFileResource(fileId, fetch);
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
				return fsResourceService.getStoreById(storeId);
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
	 * Fetch the resource store for a specific path resource, e.g. for any directory resource or file
	 * meta resource.
	 * 
	 * @param resourceId - the id of the resource, e.g. id of a FsDirectoryResource, or FsFileMetaResource,
	 * 	or some other resource in the tree which extends from FsPathResource.
	 * @return
	 * @throws ServiceException
	 */
	public FsResourceStore getResourceStoreByPathResource(final Long resourceId) throws ServiceException {
		
		class Task extends AbstractFsTask<FsResourceStore> {

			@Override
			public FsResourceStore doWork() throws ServiceException {
				return fsResourceService.getStoreByPathResourceId(resourceId);
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
	 * Add directory resource
	 * 
	 * @param name
	 * @param parentDirId
	 * @return
	 * @throws ServiceException
	 */
	public FsDirectoryResource addDirectoryResource(String name, Long parentDirId) throws ServiceException {
		
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
