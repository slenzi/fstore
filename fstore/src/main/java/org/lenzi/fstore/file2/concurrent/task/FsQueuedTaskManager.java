package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "singleton")
public class FsQueuedTaskManager implements FsTaskExecuter {

	@InjectLogger
	private Logger logger;
	
	private BlockingQueue<FsTask<?>> queue = new LinkedBlockingQueue<FsTask<?>>();
	
	private boolean isFlaggedToStop = false;
	
	private boolean isRunning = false;
	
	public FsQueuedTaskManager() {
		
	}
	
	public synchronized void stopTaskExecuter(){
		isFlaggedToStop = true;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	
	@Override
	public void run() {
		
		isRunning = true;
		
		while(!isFlaggedToStop){
			try {
				
				// wait 5 seconds for next item in queue
				consume(queue.poll(5000, TimeUnit.MILLISECONDS));
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.warn("Interrupt exception thrown while taking next element from task queue. " + e.getMessage(), e);
			}
		}		
		
		isFlaggedToStop = false;
		isRunning = false;
		
	}

	@Override
	public void queueTask(FsTask<?> task) {
	
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
