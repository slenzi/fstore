package org.lenzi.fstore.file2.concurrent.task;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.util.DateUtil;
import org.slf4j.Logger;

/**
 * Abstract class which encapsulates general logic for a file store task/operation
 * 
 * @author sal
 *
 * @param <T>
 */
public abstract class AbstractFsTask<T> implements FsQueuedTask<T>, Comparable<FsQueuedTask<T>>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 494652534569747606L;

	private long taskId = 0L;
	
	private Date queuedTime = null;
	private Date runStartTime = null;
	private Date runEndTime = null;
	
	private CompletableFuture<T> completableFuture;
	
	//private Consumer<String> logInfo = this::printInfo;
	
	public AbstractFsTask() {
		
	}

	/**
	 * @return the taskId
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	/**
	 * Compares tasks on the date and time they were queued for execution in the task manager. 
	 */
	@Override
	public int compareTo(FsQueuedTask<T> otherTask) {
		
		if(otherTask == null){
			
			// run this task first
			return -1;
			
		}else{
			
			if(queuedTime == null && otherTask.getQueuedTime() == null){
				
				// TODO - In the future we can allow two tasks to run at the same time if we know which file stores
				// the tasks are operating on.
				
				return Long.valueOf(taskId).compareTo(Long.valueOf(otherTask.getTaskId()));
				
			}else if(queuedTime != null && otherTask.getQueuedTime() == null){
				
				// run this task first
				return -1;
				
			}else if(queuedTime == null && otherTask.getQueuedTime() != null){
				
				// run other task first
				return 1;
				
			}else{
				
				// run whichever task was queued first
				
				// TODO - In the future we can allow two tasks to run at the same time if we know which file stores
				// the tasks are operating on.
				
				if(queuedTime.equals(otherTask.getQueuedTime())){
					
					return Long.valueOf(taskId).compareTo(Long.valueOf(otherTask.getTaskId()));
					
				}else{
					
					return queuedTime.compareTo(otherTask.getQueuedTime());
					
				}
				
			}
			
		}
		
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
	 * same as calling get, but for tasks with futures that return void
	 */
	@Override
	public void waitComplete() throws ServiceException {
		
		get();
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		runStartTime = DateUtil.getCurrentTime();
		
		getLogger().info("Task is running, id => " + getTaskId() + 
				", name => " + getClass().getName() + 
				", queued at => " + DateUtil.defaultFormat(this.getQueuedTime()));
		
		T value = null;
		
		try {
			
			value = doWork();
			
		} catch (ServiceException e) {
			
			// pass exception to CompletableFuture.get()
			getCompletableFuture().completeExceptionally(e);
			
		}
		
		runEndTime = DateUtil.getCurrentTime();
		
		getLogger().info("Task ended.");
		
		// at this point, any potential client thread that's blocking on CompletableFuture.get() will wake up and receive the value
		getCompletableFuture().complete(value);
		
	}

	public abstract T doWork() throws ServiceException;
	
	public abstract Logger getLogger();

}
