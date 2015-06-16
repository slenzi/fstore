package org.lenzi.fstore.file2.concurrent.task;


public interface FsTaskExecuter extends Runnable {

	public void queueTask(FsTask<?> task);
	
}
