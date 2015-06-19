package org.lenzi.fstore.file2.concurrent.task;

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
	 * Create new instance of FsGetFileTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsGetFileTask getFsGetFileTask() {
	
		FsGetFileTask task = new FsGetFileTask();
		autowire(task);
		return task;
		
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
	 *
	 * @return
	 */
	@Bean
    public FsAddStoreTask getAddStoreTask() {

		FsAddStoreTask task = new FsAddStoreTask();
		autowire(task);
		return task;

    }
	
	/**
	 * Create new instance of FsRemoveFileTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsRemoveFileTask getRemoveFileTask() {

		FsRemoveFileTask task = new FsRemoveFileTask();
		autowire(task);
		return task;

    }
	
	/**
	 * Create new instance of FsRemoveFileListTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsRemoveFileListTask getRemoveFileListTask() {

		FsRemoveFileListTask task = new FsRemoveFileListTask();
		autowire(task);
		return task;

    }
	
	/**
	 * Create new instance of FsRemoveDirectoryTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsRemoveDirectoryTask getRemoveDirectoryTask() {

		FsRemoveDirectoryTask task = new FsRemoveDirectoryTask();
		autowire(task);
		return task;

    }
	
	/**
	 * Create new instance of FsRemoveStoreTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsRemoveStoreTask getRemoveStoreTask() {

		FsRemoveStoreTask task = new FsRemoveStoreTask();
		autowire(task);
		return task;

    }
	
	/**
	 * Create new instance of FsGetPathResourceTreeTask bean
	 * 
	 * @return
	 */
	@Bean
    public FsGetPathResourceTreeTask getGetPathResourceTreeTask() {

		FsGetPathResourceTreeTask task = new FsGetPathResourceTreeTask();
		autowire(task);
		return task;

    }

}
