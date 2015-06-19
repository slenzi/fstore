package org.lenzi.fstore.file2.concurrent.task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for removing a list of files
 * 
 * @author sal
 */
@Service
public class FsRemoveFileListTask extends AbstractFsTask<Void> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3860985105757607992L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private List<Long> fileIdList = null;

	/**
	 * 
	 */
	public FsRemoveFileListTask() {
		super();
		setCompletableFuture(new CompletableFuture<Void>());
	}

	/**
	 * 
	 * @param fileId
	 */
	public FsRemoveFileListTask(List<Long> fileIdList) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<Void>());
		
		this.fileIdList = fileIdList;
		
	}

	/**
	 * @return the fileIdList
	 */
	public List<Long> getFileIdList() {
		return fileIdList;
	}

	/**
	 * @param fileIdList the fileIdList to set
	 */
	public void setFileIdList(List<Long> fileIdList) {
		this.fileIdList = fileIdList;
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
		
		for(Long fileId : fileIdList){
			fsResourceService.removeFileResource(fileId);
		}
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
