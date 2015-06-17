package org.lenzi.fstore.file2.concurrent.service;

import java.nio.file.Path;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.task.FsAddFileTask;
import org.lenzi.fstore.file2.concurrent.task.FsQueuedTaskManager;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
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
	
	public FsQueuedResourceService() {
		
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
		
		FsAddFileTask addFileTask = new FsAddFileTask(filePath, dirId, replaceExisting);
		
		taskManager.addTask(addFileTask);
		
		// block and wait for result
		resource = addFileTask.get();
		
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
