package org.lenzi.fstore.file2.concurrent.task;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for creating a file store
 * 
 * @author sal
 */
@Service
public class FsAddStoreTask extends AbstractFsTask<FsResourceStore> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8916601482908768995L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private Path storePath = null;
	private String name = null;
	private String description = null;
	private boolean clearIfExists = false;
	

	/**
	 * 
	 */
	public FsAddStoreTask() {
		super();
		setCompletableFuture(new CompletableFuture<FsResourceStore>());
	}

	/**
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 */
	public FsAddStoreTask(Path storePath, String name, String description,boolean clearIfExists) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<FsResourceStore>());
		
		this.storePath = storePath;
		this.name = name;
		this.description = description;
		this.clearIfExists = clearIfExists;
	}
	
	/**
	 * @return the storePath
	 */
	public Path getStorePath() {
		return storePath;
	}

	/**
	 * @param storePath the storePath to set
	 */
	public void setStorePath(Path storePath) {
		this.storePath = storePath;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the clearIfExists
	 */
	public boolean isClearIfExists() {
		return clearIfExists;
	}

	/**
	 * @param clearIfExists the clearIfExists to set
	 */
	public void setClearIfExists(boolean clearIfExists) {
		this.clearIfExists = clearIfExists;
	}

	@Override
	public FsResourceStore doWork() throws ServiceException {
		
		return doAddStore();
		
	}
	
	/**
	 * create store
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private FsResourceStore doAddStore() throws ServiceException {
		
		return fsResourceService.createResourceStore(storePath, name, description, clearIfExists);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
