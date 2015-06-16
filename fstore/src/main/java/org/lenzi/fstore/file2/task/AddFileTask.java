package org.lenzi.fstore.file2.task;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Task for adding a single file.
 * 
 * @author sal
 */
@Service
public class AddFileTask extends AbstractFsStoreTask {

	@InjectLogger
	private Logger logger;
	
	private Path filePath = null;
	
	private Long dirId = null;
	
	private boolean replaceExisting = false;
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5883529254145781544L;

	/**
	 * @param filePath - path to file to be added
	 * @param dirId - id of directory where fill will be added
	 * @param replaceExisting - true to replace existing file, false not to.
	 */
	public AddFileTask(Path filePath, Long dirId, boolean replaceExisting) {
		super();
		this.filePath = filePath;
		this.dirId = dirId;
		this.replaceExisting = replaceExisting;
	}

	@Override
	public void doWork() throws ServiceException {
		
		Consumer<String> logInfo = this::printInfo;
		
		logInfo.accept("Add file task started.");
		
		for(int i=1; i<=10; i++){
			
			logInfo.accept("Add file task => " + i + "...");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		logInfo.accept("Add file task ended.");

	}
	
	private FsFileMetaResource doAddFile() throws ServiceException {
		
		return getFsResourceService().addFileResource(filePath, dirId, replaceExisting);
		
	}
	
	private void printInfo(String s){
		logger.info(s);
	}

}
