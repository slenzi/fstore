package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Queues tasks in a priority blocking queue for execution.
 * 
 * prototype scope - new instance each time
 * 
 * @author sal
 */
@Component
@Scope(value = "prototype")
public class FsQueuedTaskManager implements FsTaskManager {

	@InjectLogger
	private Logger logger;
	
	private BlockingQueue<FsQueuedTask<?>> queue = new PriorityBlockingQueue<FsQueuedTask<?>>();
	
	//private boolean isFlaggedToStop = false;
	
	private boolean isRunning = false;
	
	private ExecutorService executorService = null;
	
	private long taskId = 0L;
	
	private String managerName = FsQueuedTaskManager.class.getName();
	
	public FsQueuedTaskManager() {
		
	}
	
	/**
	 * @return the managerName
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * @param managerName the managerName to set
	 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	/**
	 * Starts the task manager by adding it to the executor service.
	 */
	@Override
	public void startTaskManager(ExecutorService executorService) {
		
		this.executorService = executorService;
		
		logger.info("Submitting queued task manager to executor service.");
		
		//this.executorService.submit(this);
		this.executorService.execute(this);
		
	}
	
	/**
	 * Stops the task manager by shutting down the executor service. 
	 */
	@Override
	public void stopTaskManager() {
		
		logger.info("Stop task manager called");

		queue.clear();

		logger.info("Shuttin down executor service...");

		executorService.shutdown(); // Disable new tasks from being submitted to executorService

		logger.info("Call to executor shutdown complete...");

		try {

			logger.info("Awaiting termination for 30 seconds...");

			if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {

				logger.info("Calling shutdownNow() on executor service...");

				executorService.shutdownNow();

				logger.info("Awaiting termination for additional 60 seconds...");

				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					logger.error("Executor service did not terminate");
				}
			}

		} catch (InterruptedException e) {
			// (Re-)Cancel if current thread also interrupted
			executorService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();   
		}

		logger.info("Stop task manager call complete");
		
	}
	
	/**
	 * Get the number of tasks currently in the queue
	 */
	@Override
	public int taskCount() {
		
		return queue.size();
		
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	/**
	 * Run this manager
	 */
	@Override
	public void run() {
		
		isRunning = true;
		
		logger.info(FsQueuedTaskManager.class.getName() + " running!");
		
		while(true){
			
			logger.debug("Polling queued task manager '" + managerName + "'  + ... queue size => " + taskCount());
			
			if(Thread.currentThread().isInterrupted()){
				break;
			}
			
			try {
				
				// wait 5 seconds for next item in queue
				consume(queue.poll(5000, TimeUnit.MILLISECONDS));
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.warn("Interrupt exception thrown while taking next element from task queue.", e);
			}
			
		}		
		
		//isFlaggedToStop = false;
		isRunning = false;
		
		logger.info(FsQueuedTaskManager.class.getName() + " run has ended!");
		
	}

	/**
	 * Add a task to the queue for processing
	 */
	@Override
	public synchronized void addTask(FsQueuedTask<?> task) {
		
		// TODO - check target store for the task, add to blocking queue for that store.
		
		task.setTaskId(getNextTaskId());
		task.setQueuedTime(DateUtil.getCurrentTime());
		
		try {
		
			queue.put(task);
			
			logger.info("Task was queued, id => " + task.getTaskId() + 
					", name => " + task.getClass().getName() + 
					", queued at " + DateUtil.defaultFormat(task.getQueuedTime()));
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}		
			
	}
	
	private synchronized long getNextTaskId(){
		return ++taskId;
	}
	
	/**
	 * Run the task
	 * 
	 * @param task
	 */
	private void consume(FsQueuedTask<?> task){
		
		if(task != null){
			task.run();
		}
		
	}

}
