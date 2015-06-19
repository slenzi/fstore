package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for fetching a file
 * 
 * @author sal
 */
@Service
public class FsGetFileTask extends AbstractFsTask<FsFileMetaResource> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5883529254145781544L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private Long fileId = null;
	private FsFileResourceFetch fetch = null;

	/**
	 * 
	 */
	public FsGetFileTask() {
		super();
		setCompletableFuture(new CompletableFuture<FsFileMetaResource>());
	}

	/**
	 * 
	 * @param dirId
	 */
	public FsGetFileTask(Long fileId, FsFileResourceFetch fetch) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<FsFileMetaResource>());
		
		this.fileId = fileId;
		this.fetch = fetch;
		
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

	/**
	 * @return the fetch
	 */
	public FsFileResourceFetch getFetch() {
		return fetch;
	}

	/**
	 * @param fetch the fetch to set
	 */
	public void setFetch(FsFileResourceFetch fetch) {
		this.fetch = fetch;
	}

	@Override
	public FsFileMetaResource doWork() throws ServiceException {
	
		return doGetFileResource();

	}
	
	/**
	 * add file
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private FsFileMetaResource doGetFileResource() throws ServiceException {
		
		return fsResourceService.getFileResource(fileId, fetch);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
