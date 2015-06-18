package org.lenzi.fstore.file2.concurrent.task;

import java.nio.file.Path;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * Factory for creating new instances of file store tasks. Makes sure that dependencies
 * are autowired in the task beans.
 * 
 * @author sal
 */
@Service
public class FsTaskCreator implements ApplicationContextAware {

	@InjectLogger
	private Logger logger;
	
	private ApplicationContext appContext = null;
	
	public FsTaskCreator() {
		
	}
	
	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		
		this.appContext = appContext;
		
	}
	
	/**
	 * Ensure that dependencies are autowired
	 * 
	 * @param o
	 */
	private void autowire(Object o){
		
		AutowireCapableBeanFactory factory = appContext.getAutowireCapableBeanFactory();
		
		factory.autowireBean(o);
		factory.initializeBean(o,o.getClass().getName());
		
	}
	
	/**
	 * Create new instance of FsAddFileTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsAddFileTask getAddFileTask() {
	
		FsAddFileTask task = new FsAddFileTask();
		autowire(task);
		return task;
		
    }
	
	/**
	 * Create new instance of FsAddFileListTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsAddFileListTask getAddFileListTask() {
	
		FsAddFileListTask task = new FsAddFileListTask();
		autowire(task);
		return task;
		
    }
	
	/**
	 * Create new instance of FsAddDirectoryTask bean

	 * @return
	 */
	@Bean
    public FsAddDirectoryTask getAddDirectoryTask() {

		FsAddDirectoryTask task = new FsAddDirectoryTask();
		autowire(task);
		return task;
		
    }
	
	/**
	 * Create new instance of FsAddStoreTask bean

	 * @return
	 */
	@Bean
    public FsAddStoreTask getAddStoreTask() {

		FsAddStoreTask task = new FsAddStoreTask();
		autowire(task);
		return task;

    }

}
