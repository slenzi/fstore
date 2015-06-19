package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for removing a resource store
 * 
 * @author sal
 */
@Service
public class FsRemoveStoreTask extends AbstractFsTask<Void> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3860985105757607992L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private Long storeId = null;

	/**
	 * 
	 */
	public FsRemoveStoreTask() {
		super();
		setCompletableFuture(new CompletableFuture<Void>());
	}

	/**
	 * 
	 * @param storeId
	 */
	public FsRemoveStoreTask(Long storeId) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<Void>());
		
		this.storeId = storeId;
		
	}

	/**
	 * @return the storeId
	 */
	public Long getStoreId() {
		return storeId;
	}

	/**
	 * @param storeId the storeId to set
	 */
	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	@Override
	public Void doWork() throws ServiceException {
	
		doRemoveStore();
		
		return null;

	}
	
	/**
	 * remove store
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private void doRemoveStore() throws ServiceException {
		
		fsResourceService.removeResourceStore(storeId);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
