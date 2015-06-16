package org.lenzi.fstore.file2.service;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resource service which queues operations.
 * 
 * 
 * @author slenzi
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsQueuedResourceService {

	private ExecutorService executorService = Executors.newFixedThreadPool(20);
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private BiConsumer<FsFileMetaResource, Throwable> fsFileMetaResourceConsumer = (resource, throwable) -> {
        System.out.println( resource != null ? resource.toString() : "no resource");
        System.out.println( throwable != null ? throwable.getMessage() + " => " + throwable.getCause().getMessage() : "no throwable");
    };
	
	public FsQueuedResourceService() {
		
	}
	
	public FsFileMetaResource addFileResource(Path filePath, Long dirId, boolean replaceExisting) throws ServiceException {
		
		FsFileMetaResource resource = null;
		
		CompletableFuture<FsFileMetaResource> addFileFuture = CompletableFuture
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
		
		return resource;
		
	}
	
	private FsFileMetaResource supplyNewFile(Path filePath, Long dirId, boolean replaceExisting){
		
		// TODO - queue the file to be added and block until done,
		
		try {
			return fsResourceService.addFileResource(filePath, dirId, replaceExisting);
		} catch (ServiceException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}

}
