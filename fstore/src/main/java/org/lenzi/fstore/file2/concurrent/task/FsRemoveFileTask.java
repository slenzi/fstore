package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for removing a single file.
 * 
 * @author sal
 */
@Service
public class FsRemoveFileTask extends AbstractFsTask<Void> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3860985105757607992L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private Long fileId = null;

	/**
	 * 
	 */
	public FsRemoveFileTask() {
		super();
		setCompletableFuture(new CompletableFuture<Void>());
	}

	/**
	 * 
	 * @param fileId
	 */
	public FsRemoveFileTask(Long fileId) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<Void>());
		
		this.fileId = fileId;
		
	}

	/**
	 * @return the fileId
	 */
	public Long getFileId() {
		return fileId;
	}

	/**
	 * @param fileId the fileId to set
	 */
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	@Override
	public Void doWork() throws ServiceException {
	
		doRemoveFile();
		
		return null;

	}
	
	/**
	 * remove file
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private void doRemoveFile() throws ServiceException {
		
		fsResourceService.removeFileResource(fileId);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
