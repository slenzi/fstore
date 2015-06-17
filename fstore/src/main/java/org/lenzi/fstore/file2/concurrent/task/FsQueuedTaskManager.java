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
 * @author sal
 */
@Component
@Scope(value = "singleton")
public class FsQueuedTaskManager implements FsTaskManager {

	@InjectLogger
	private Logger logger;
	
	private BlockingQueue<FsTask<?>> queue = new PriorityBlockingQueue<FsTask<?>>();
	
	//private boolean isFlaggedToStop = false;
	
	private boolean isRunning = false;
	
	public FsQueuedTaskManager() {
		
	}
	
	/**
	 * Starts the task manager by adding it to the executor service.
	 */
	@Override
	public void startTaskManager(ExecutorService service) {
		
		service.submit(this);
		
	}
	
	/**
	 * Stops the task manager by shutting down the executor service. 
	 */
	@Override
	public void stopTaskManager(ExecutorService service) {
		
		service.shutdown(); // Disable new tasks from being submitted
		
		try {
			if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
				
				service.shutdownNow();
				
				if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
					logger.error("Executor service did not terminate");
				}
			}
	   } catch (InterruptedException ie) {
		   // (Re-)Cancel if current thread also interrupted
		   service.shutdownNow();
		   // Preserve interrupt status
		   Thread.currentThread().interrupt();   
	   }		
		
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
	
	@Override
	public void run() {
		
		isRunning = true;
		
		while(true){
			
			if(Thread.currentThread().isInterrupted()){
				break;
			}
			
			try {
				
				// wait 5 seconds for next item in queue
				consume(queue.poll(5000, TimeUnit.MILLISECONDS));
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.warn("Interrupt exception thrown while taking next element from task queue. " + e.getMessage(), e);
			}
		}		
		
		//isFlaggedToStop = false;
		isRunning = false;
		
	}

	/**
	 * Add a task to the queue for processing
	 */
	@Override
	public synchronized void addTask(FsTask<?> task) {
		
		// TODO - check target store for the task, add to blocking queue for that store.
		
		task.setQueuedTime(DateUtil.getCurrentTime());
		
		try {
			
			queue.put(task);
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}		
			
	}
	
	private void consume(FsTask<?> task){
		if(task != null){
			task.run();
		}
	}

}
