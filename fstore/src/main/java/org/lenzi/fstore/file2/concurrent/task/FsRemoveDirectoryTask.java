package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for removing a single directory.
 * 
 * @author sal
 */
@Service
public class FsRemoveDirectoryTask extends AbstractFsTask<Void> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3860985105757607992L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private Long dirId = null;

	/**
	 * 
	 */
	public FsRemoveDirectoryTask() {
		super();
		setCompletableFuture(new CompletableFuture<Void>());
	}

	/**
	 * 
	 * @param dirId
	 */
	public FsRemoveDirectoryTask(Long dirId) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<Void>());
		
		this.dirId = dirId;
		
	}

	/**
	 * @return the dirId
	 */
	public Long getDirId() {
		return dirId;
	}

	/**
	 * @param dirId the dirId to set
	 */
	public void setDirId(Long dirId) {
		this.dirId = dirId;
	}

	@Override
	public Void doWork() throws ServiceException {
	
		doRemoveDirectory();
		
		return null;

	}
	
	/**
	 * remove directory
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private void doRemoveDirectory() throws ServiceException {
		
		fsResourceService.removeDirectoryResource(dirId);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
