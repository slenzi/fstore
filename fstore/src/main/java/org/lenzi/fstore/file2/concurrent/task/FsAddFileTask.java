package org.lenzi.fstore.file2.concurrent.task;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for adding a single file.
 * 
 * @author sal
 */
@Service
public class FsAddFileTask extends AbstractFsTask<FsFileMetaResource> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5883529254145781544L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private Path filePath = null;
	
	private Long dirId = null;
	
	private boolean replaceExisting = false;

	/**
	 * 
	 */
	public FsAddFileTask() {
		super();
		setCompletableFuture(new CompletableFuture<FsFileMetaResource>());
	}

	/**
	 * @param filePath - path to file to be added
	 * @param dirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file, false not to.
	 */
	public FsAddFileTask(Path filePath, Long dirId, boolean replaceExisting) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<FsFileMetaResource>());
		
		this.filePath = filePath;
		this.dirId = dirId;
		this.replaceExisting = replaceExisting;
		
	}
	
	/**
	 * @return the filePath
	 */
	public Path getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(Path filePath) {
		this.filePath = filePath;
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

	/**
	 * @return the replaceExisting
	 */
	public boolean isReplaceExisting() {
		return replaceExisting;
	}

	/**
	 * @param replaceExisting the replaceExisting to set
	 */
	public void setReplaceExisting(boolean replaceExisting) {
		this.replaceExisting = replaceExisting;
	}

	@Override
	public FsFileMetaResource doWork() throws ServiceException {
	
		return doAddFile();

	}
	
	/**
	 * add file
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private FsFileMetaResource doAddFile() throws ServiceException {
		
		return fsResourceService.addFileResource(filePath, dirId, replaceExisting);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
