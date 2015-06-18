package org.lenzi.fstore.file2.concurrent.service;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.task.FsAddDirectoryTask;
import org.lenzi.fstore.file2.concurrent.task.FsAddFileListTask;
import org.lenzi.fstore.file2.concurrent.task.FsAddFileTask;
import org.lenzi.fstore.file2.concurrent.task.FsAddStoreTask;
import org.lenzi.fstore.file2.concurrent.task.FsQueuedTaskManager;
import org.lenzi.fstore.file2.concurrent.task.FsTaskCreator;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
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
	private FsTaskCreator taskCreator;
	
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
	 * Add file resource
	 * 
	 * @param filePath - path to file to be added
	 * @param dirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file, false not to.
	 * @return reference to the newly added file
	 * @throws ServiceException
	 */
	public FsFileMetaResource addFileResource(Path filePath, Long dirId, boolean replaceExisting) throws ServiceException {
		
		FsFileMetaResource resource = null;
		
		FsAddFileTask task = taskCreator.getAddFileTask();
		task.setFilePath(filePath);
		task.setDirId(dirId);
		task.setReplaceExisting(replaceExisting);
		
		taskManager.addTask(task);
		
		// block and wait for result
		logger.info("Waiting for new " + FsFileMetaResource.class.getName());
		resource = task.get();
		
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
		
		List<FsFileMetaResource> resources = null;
		
		FsAddFileListTask task = taskCreator.getAddFileListTask();
		task.setFilePaths(filePaths);
		task.setDirId(dirId);
		task.setReplaceExisting(replaceExisting);
		
		taskManager.addTask(task);
		
		// block and wait for result
		logger.info("Waiting for new " + FsFileMetaResource.class.getName());
		resources = task.get();
		
		return resources;
		
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
		
		FsDirectoryResource resource = null;
		
		FsAddDirectoryTask task = taskCreator.getAddDirectoryTask();
		task.setName(name);
		task.setParentDirId(parentDirId);
		
		taskManager.addTask(task);
		
		// block and wait for result
		logger.info("Waiting for new " + FsDirectoryResource.class.getName());
		resource = task.get();
		
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
		
		FsResourceStore resource = null;
		
		FsAddStoreTask task = taskCreator.getAddStoreTask();
		task.setStorePath(storePath);
		task.setName(name);
		task.setDescription(description);
		task.setClearIfExists(clearIfExists);
		
		taskManager.addTask(task);
		
		// block and wait for result
		logger.info("Waiting for new " + FsResourceStore.class.getName());
		resource = task.get();
		
		return resource;
		
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
