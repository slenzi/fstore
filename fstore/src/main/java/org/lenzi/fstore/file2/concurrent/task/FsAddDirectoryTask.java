package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for adding a directory
 * 
 * @author sal
 */
@Service
public class FsAddDirectoryTask extends AbstractFsTask<FsDirectoryResource> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5883529254145781544L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private String name = null;
	
	private Long parentDirId = null;
	
	/**
	 * 
	 */
	public FsAddDirectoryTask() {
		super();
		setCompletableFuture(new CompletableFuture<FsDirectoryResource>());
	}

	/**
	 * @param filePath - path to file to be added
	 * @param dirId - id of directory where file will be added
	 * @param replaceExisting - true to replace existing file, false not to.
	 */
	public FsAddDirectoryTask(String name, Long parentDirId) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<FsDirectoryResource>());
		
		this.name = name;
		this.parentDirId = parentDirId;
		
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parentDirId
	 */
	public Long getParentDirId() {
		return parentDirId;
	}

	/**
	 * @param parentDirId the parentDirId to set
	 */
	public void setParentDirId(Long parentDirId) {
		this.parentDirId = parentDirId;
	}

	@Override
	public FsDirectoryResource doWork() throws ServiceException {
	
		return doAddDirectory();

	}
	
	/**
	 * add directory
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private FsDirectoryResource doAddDirectory() throws ServiceException {
		
		return fsResourceService.addDirectoryResource(parentDirId, name);
		
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
