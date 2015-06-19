/**
 * 
 */
package org.lenzi.fstore.test.file2.concurrent;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.test.AbstractTreeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.Rollback;

/**
 * @author sal
 */
public abstract class AbstractConcurrentAdd extends AbstractTreeTest {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private FsQueuedResourceService resourceService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	
	public AbstractConcurrentAdd() {
		
	}
	
	/**
	 * Simulate a user creating a new directory and adding files.
	 * 
	 * @author sal
	 */
	private class AddFileActor implements Callable<AddFileActor> {
		
		private String actorName = null;
		private String dirName = null;
		private List<Path> filePaths = null;
		private FsResourceStore store = null;
		
		private FsDirectoryResource dirResource = null;
		private List<FsFileMetaResource> fileResourceList = null;
		
		public AddFileActor(String actorName, String dirName, List<Path> filePaths, FsResourceStore store){
			this.actorName = actorName;
			this.dirName = dirName;
			this.filePaths = filePaths;
			this.store = store;
		}
		
		public String getActorName(){
			return actorName;
		}

		public List<Path> getFilePaths() {
			return filePaths;
		}
		
		public List<FsFileMetaResource> getFileResources(){
			return fileResourceList;
		}

		public FsDirectoryResource getDirResource() {
			return dirResource;
		}

		@Override
		public AddFileActor call() throws Exception {
			
			logger.info("Actor " + actorName + " is running.");
			
			// create sub directory
			try {
				dirResource = resourceService.addDirectoryResource(dirName, store.getRootDirectoryResource().getDirId());
			} catch (ServiceException e) {
				logger.error("Failed to add child directory to dir => " + store.getRootDirectoryResource().getDirId() + 
						". " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			// add files
			try {
				fileResourceList = resourceService.addFileResource(filePaths, dirResource.getDirId(), true);
			} catch (ServiceException e) {
				logger.error("Failed to add files to sub directory, id => " + dirResource.getDirId());
				e.printStackTrace();
				return null;
			}
			
			return this;
			
		}
		
	};
	
	@Test
	@Rollback(false)
	public void concurrentAddTest() {
		
		logTestTitle("Concurrent add test");
		
		
		assertNotNull(resourceLoader);
		
		//
		// get resource path to sample images for testing
		//
		Resource sourceDir = resourceLoader.getResource("classpath:image/");
		Path sourceDirPath = null;
		try {
			sourceDirPath = Paths.get(sourceDir.getFile().getAbsolutePath());
		} catch (IOException e) {
			logger.error("Failed to get source dir resource." + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//
		// get all files to add
		//
		List<Path> filePaths = null;
		try {
			filePaths = FileUtil.listFilesToDepth(sourceDirPath, 1);
		} catch (IOException e) {
			logger.error("Failed to get source dir resource." + e.getMessage());
			e.printStackTrace();
			return;
		}		
		
		//
		// create file store
		//
		FsResourceStore store = null;
		Path storePath = Paths.get(getTestStorePath());
		try {
			store = resourceService.addResourceStore(storePath, "Sample Resource Store", "Sample resource store description", true);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return;
		}
		
		assertNotNull(store);
		assertNotNull(store.getRootDirectoryResource());
		
		logger.info("Resource Store => " + store);
		
		//
		// Simulate web actors, adding directories and files
		//
		
		logger.info("Simulating multiple web actors...");
		
		final List<Path> filePathList = filePaths;
		final FsResourceStore fStore = store;
		List<AddFileActor> actorList = new ArrayList<AddFileActor>(){{
			add(new AddFileActor("Actor 1", "actor1", filePathList, fStore));
			add(new AddFileActor("Actor 2", "actor2", filePathList, fStore));
			add(new AddFileActor("Actor 3", "actor3", filePathList, fStore));
			add(new AddFileActor("Actor 4", "actor4", filePathList, fStore));
			add(new AddFileActor("Actor 5", "actor5", filePathList, fStore));
		}};
		
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CompletionService<AddFileActor> completionService = new ExecutorCompletionService<AddFileActor>(executorService); 
		
		for(final AddFileActor actor : actorList){
			completionService.submit(actor);
		}
		
		try {
			int finishedActorCount = 0;
			while(finishedActorCount < actorList.size()){
				Future<AddFileActor> f = completionService.take();
				AddFileActor a = f.get();
				logger.info(a.getActorName() + " has completed, " + a.getFileResources().size() + " files added.");
				finishedActorCount++;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			Thread.currentThread().interrupt();
		} finally {
			if (executorService != null) {
				executorService.shutdownNow();
			}
		}
		
		//executorService.shutdown();
		
		// Wait until all threads are finish
		while (!executorService.isTerminated()) {
			
		}
		
		logger.info("Done");
		
	}
	
	public abstract String getTestStorePath();

}
