package org.lenzi.fstore.file2.concurrent.task;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;

/**
 * Defines a basic task with a completable future
 * 
 * @author sal
 *
 * @param <T>
 */
public interface FsTask<T> extends Runnable, Serializable {

	public Date getQueuedTime();
	
	public Date getRunStartTime();
	
	public Date getRunEndTime();
	
	public CompletableFuture<T> getCompletableFuture();
	
	public T get() throws ServiceException;
	
	public T doWork() throws ServiceException;
	
}
