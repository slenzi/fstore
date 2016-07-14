package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.ExecutorService;


public interface FsTaskManager extends Runnable {

	/**
	 * Add a task to the manager for processing.
	 * 
	 * @param task
	 */
	public void addTask(FsQueuedTask<?> task);
	
	/**
	 * Get the number of tasks that are currently managed.
	 * 
	 * @return
	 */
	public int taskCount();
	
	/**
	 * Adds the task manager to the executor service, in effect starting it up.
	 * 
	 * @param service
	 */
	public void startTaskManager(ExecutorService executorService);
	
	/**
	 * Stop the task manager. Will trigger the executor service to shutdown.
	 * 
	 * @param service
	 */
	public void stopTaskManager();
	
	
}
