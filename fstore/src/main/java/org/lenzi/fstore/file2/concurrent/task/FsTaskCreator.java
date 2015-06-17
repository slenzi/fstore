package org.lenzi.fstore.file2.concurrent.task;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class FsTaskCreator implements ApplicationContextAware {

	@InjectLogger
	private Logger logger;
	
	private ApplicationContext appContext = null;
	
	public FsTaskCreator() {
		
	}
	
	@Bean
    public FsAddFileTask getAddFileTask() {

		AutowireCapableBeanFactory factory = appContext.getAutowireCapableBeanFactory();
		
		FsAddFileTask task = new FsAddFileTask();
		
		factory.autowireBean(task);
		factory.initializeBean(task, "task");
		
		return new FsAddFileTask();
		
    }
	
	@Bean
    public FsAddDirectoryTask getAddDirectoryTask() {

		return new FsAddDirectoryTask();
    }
	
	@Bean
    public FsAddStoreTask getAddStoreTask() {

		return new FsAddStoreTask();

    }

	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
	}

}
