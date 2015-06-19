package org.lenzi.fstore.file2.concurrent.task;

import java.util.concurrent.CompletableFuture;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Task for fetching a path resource tree
 * 
 * @author sal
 */
@Service
public class FsGetPathResourceTreeTask extends AbstractFsTask<Tree<FsPathResource>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5883529254145781544L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;
	
	private Long dirId = null;

	/**
	 * 
	 */
	public FsGetPathResourceTreeTask() {
		super();
		setCompletableFuture(new CompletableFuture<Tree<FsPathResource>>());
	}

	/**
	 * 
	 * @param dirId
	 */
	public FsGetPathResourceTreeTask(Long dirId) {
		
		super();
		
		setCompletableFuture(new CompletableFuture<Tree<FsPathResource>>());
		
		this.dirId = dirId;
		
	}

	/**
	 * @return the dirId
	 */
	public Long getDirId() {
		return dirId;
	}

	/**
	 * @param dirId the dirId to set
	 */
	public void setDirId(Long dirId) {
		this.dirId = dirId;
	}

	@Override
	public Tree<FsPathResource> doWork() throws ServiceException {
	
		return doGetPathResourceTree();

	}
	
	/**
	 * add file
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private Tree<FsPathResource> doGetPathResourceTree() throws ServiceException {
		
		return fsResourceService.getTree(dirId);
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

}
