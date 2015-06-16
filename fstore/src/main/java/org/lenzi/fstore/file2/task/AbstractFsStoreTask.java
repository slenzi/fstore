package org.lenzi.fstore.file2.task;

import java.io.Serializable;
import java.util.Date;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractFsStoreTask implements FsStoreTask, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 494652534569747606L;
	
	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceService fsResourceService;

	private Date queuedTime = null;
	private Date runStartTime = null;
	private Date runEndTime = null;
	private Long sourceStoreId = null;
	private Long targetStoreId = null;
	
	public AbstractFsStoreTask() {
		
	}

	/**
	 * @return the queuedTime
	 */
	public Date getQueuedTime() {
		return queuedTime;
	}

	/**
	 * @param queuedTime the queuedTime to set
	 */
	public void setQueuedTime(Date queuedTime) {
		this.queuedTime = queuedTime;
	}

	/**
	 * @return the runStartTime
	 */
	public Date getRunStartTime() {
		return runStartTime;
	}

	/**
	 * @param runStartTime the runStartTime to set
	 */
	public void setRunStartTime(Date runStartTime) {
		this.runStartTime = runStartTime;
	}

	/**
	 * @return the runEndTime
	 */
	public Date getRunEndTime() {
		return runEndTime;
	}

	/**
	 * @param runEndTime the runEndTime to set
	 */
	public void setRunEndTime(Date runEndTime) {
		this.runEndTime = runEndTime;
	}
	
	/**
	 * @return the sourceStoreId
	 */
	public Long getSourceStoreId() {
		return sourceStoreId;
	}

	/**
	 * @param sourceStoreId the sourceStoreId to set
	 */
	public void setSourceStoreId(Long sourceStoreId) {
		this.sourceStoreId = sourceStoreId;
	}

	/**
	 * @return the targetStoreId
	 */
	public Long getTargetStoreId() {
		return targetStoreId;
	}

	/**
	 * @param targetStoreId the targetStoreId to set
	 */
	public void setTargetStoreId(Long targetStoreId) {
		this.targetStoreId = targetStoreId;
	}
	
	/**
	 * @return the fsResourceService
	 */
	public FsResourceService getFsResourceService() {
		return fsResourceService;
	}

	public abstract void doWork() throws ServiceException;

}
