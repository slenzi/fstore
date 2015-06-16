package org.lenzi.fstore.file2.concurrent.task;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Task for adding a single file.
 * 
 * @author sal
 */
@Service
public class FsAddFileTask extends AbstractFsTask<FsFileMetaResource> {

	@InjectLogger
	private Logger logger;
	
	private Path filePath = null;
	
	private Long dirId = null;
	
	private boolean replaceExisting = false;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5883529254145781544L;

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

	@Override
	public FsFileMetaResource doWork() throws ServiceException {
	
		FsFileMetaResource resource = null;
		
		resource = doAddFile();
		
		return resource;

	}
	
	private FsFileMetaResource doAddFile() throws ServiceException {
		
		return getFsResourceService().addFileResource(filePath, dirId, replaceExisting);
		
	}
	


}
