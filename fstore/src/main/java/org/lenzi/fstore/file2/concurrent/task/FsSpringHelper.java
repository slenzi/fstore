package org.lenzi.fstore.file2.concurrent.task;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Use this class as a factor for manually creating beans and autowiring their dependencies
 * 
 * @author sal
 */
@Service
public class FsSpringHelper implements ApplicationContextAware {

	@InjectLogger
	private Logger logger;
	
	private ApplicationContext appContext = null;
	
	public FsSpringHelper() {
		
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

}
