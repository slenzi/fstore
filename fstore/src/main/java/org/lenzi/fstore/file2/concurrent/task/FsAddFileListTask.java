/**
 * 
 */
package org.lenzi.fstore.file2.concurrent.task;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Task for adding a collection of file.
 * 
 * @author sal
 */
public class FsAddFileListTask extends AbstractFsTask<List<FsFileMetaResource>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3064322921813354789L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private List<Path> filePaths = null;
	
	private Long dirId = null;
	
	private boolean replaceExisting = false;

	/**
	 * 
	 */
	public FsAddFileListTask() {
		super();
		setCompletableFuture(new CompletableFuture<List<FsFileMetaResource>>());
	}

	/**
	 * @param filePath - list of paths of files to be added
	 * @param dirId - id of directory where files will be added
	 * @param replaceExisting - true to replace existing files, false not to.
	 */
	public FsAddFileListTask(List<Path> filePaths, Long dirId, boolean replaceExisting) {
	
		super();
	
		setCompletableFuture(new CompletableFuture<List<FsFileMetaResource>>());
		
		this.filePaths = filePaths;
		this.dirId = dirId;
		this.replaceExisting = replaceExisting;
		
	}
	
	/**
	 * @return the filePaths
	 */
	public List<Path> getFilePaths() {
		return filePaths;
	}

	/**
	 * @param filePaths the filePaths to set
	 */
	public void setFilePaths(List<Path> filePaths) {
		this.filePaths = filePaths;
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
	public List<FsFileMetaResource> doWork() throws ServiceException {

		return doAddFiles();	
		
	}
	
	/**
	 * Add files
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private List<FsFileMetaResource> doAddFiles() throws ServiceException {
		
		return fsResourceService.addFileResource(filePaths, dirId, replaceExisting);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
