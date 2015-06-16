package org.lenzi.fstore.file2.concurrent.task;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Abstract class which encapsulates general logic for a file store task/operation
 * 
 * @author sal
 *
 * @param <T>
 */
@Service
public abstract class AbstractFsTask<T> implements FsTask<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 494652534569747606L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;

	private Date queuedTime = null;
	private Date runStartTime = null;
	private Date runEndTime = null;
	
	private CompletableFuture<T> completableFuture;
	
	private Consumer<String> logInfo = this::printInfo;
	
	public AbstractFsTask() {
		
	}

	/**
	 * @return the queuedTime
	 */
	public Date getQueuedTime() {
		return queuedTime;
	}

	/**
	 * @param queuedTime the queuedTime to set
	 */
	public void setQueuedTime(Date queuedTime) {
		this.queuedTime = queuedTime;
	}

	/**
	 * @return the runStartTime
	 */
	public Date getRunStartTime() {
		return runStartTime;
	}

	/**
	 * @param runStartTime the runStartTime to set
	 */
	public void setRunStartTime(Date runStartTime) {
		this.runStartTime = runStartTime;
	}

	/**
	 * @return the runEndTime
	 */
	public Date getRunEndTime() {
		return runEndTime;
	}

	/**
	 * @param runEndTime the runEndTime to set
	 */
	public void setRunEndTime(Date runEndTime) {
		this.runEndTime = runEndTime;
	}
	
	/**
	 * @return the future
	 */
	public CompletableFuture<T> getCompletableFuture() {
		return completableFuture;
	}

	/**
	 * @param completableFuture the completableFuture to set
	 */
	public void setCompletableFuture(CompletableFuture<T> completableFuture) {
		this.completableFuture = completableFuture;
	}

	/**
	 * Calls get on completable future
	 */
	@Override
	public T get() throws ServiceException {
		try {
			return completableFuture.get();
		} catch (InterruptedException e) {
			throw new ServiceException("InterruptedException thrown while adding new file. " + e.getMessage(), e);
		} catch (ExecutionException e) {
			throw new ServiceException("ExecutionException thrown while adding new file. " + e.getMessage(), e);
		}
	}

	/**
	 * @return the fsResourceService
	 */
	public FsResourceService getFsResourceService() {
		return fsResourceService;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		runStartTime = DateUtil.getCurrentTime();
		
		logInfo.accept("Task started.");
		
		T value = null;
		
		try {
			
			value = doWork();
			
		} catch (ServiceException e) {
			
			// pass exception to CompletableFuture.get()
			getCompletableFuture().completeExceptionally(e);
			
		}
		
		runEndTime = DateUtil.getCurrentTime();
		
		logInfo.accept("Task ended.");
		
		// at this point, any potential client thread that's blocking on CompletableFuture.get() will wake up and receive the value
		getCompletableFuture().complete(value);
		
	}
	
	private void printInfo(String s){
		
		logger.info(s);
		
	}

	public abstract T doWork() throws ServiceException;

}
