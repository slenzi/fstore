package org.lenzi.fstore.file2.task;

import java.io.Serializable;
import java.util.Date;

import org.lenzi.fstore.core.service.exception.ServiceException;

public interface FsStoreTask extends Serializable {

	public Date getQueuedTime();
	
	public Date getRunStartTime();
	
	public Date getRunEndTime();
	
	public Long getSourceStoreId();
	
	public Long getTargetStoreId();
	
	public void doWork() throws ServiceException;
	
}
