/**
 * 
 */
package org.lenzi.fstore.file2.concurrent.task;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;

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
	
	private List<Path> filePaths = null;
	
	private Long dirId = null;
	
	private boolean replaceExisting = false;

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

	@Override
	public List<FsFileMetaResource> doWork() throws ServiceException {

		List<FsFileMetaResource> resources = null;
		
		resources = doAddFiles();
		
		return resources;		
		
	}
	
	private List<FsFileMetaResource> doAddFiles() throws ServiceException {
		
		return getFsResourceService().addFileResource(filePaths, dirId, replaceExisting);
		
	}

}
