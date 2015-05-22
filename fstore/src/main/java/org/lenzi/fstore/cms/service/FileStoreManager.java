package org.lenzi.fstore.cms.service;

import java.io.Serializable;
import java.nio.file.Path;

import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.service.exception.ServiceException;

/**
 * 
 * @author slenzi
 *
 * @deprecated - only kept for reference
 */
public interface FileStoreManager extends Serializable {

	// create file store
	public CmsFileStore createFileStore(Path dirPath, String name, String description) throws ServiceException;
	
	/*
	// get file store
	public CmsFileStore getFileStore(Long storeId);
	
	// add directory
	public CmsDirectory addDirectory(CmsDirectory parentDir, String newDirName);
	
	// remove directory
	public void removeDirectory(CmsDirectory dir);
	
	// move directory
	public CmsDirectory moveDirectory(CmsDirectory dirToMove, CmsDirectory newParent);
	
	// add file
	public void addFile(CmsFile file, CmsDirectory dir);
	
	// remove file
	public void removeFile(CmsFile file);
	
	// move file
	public void moveFile(CmsFile file, CmsDirectory dir);
	*/
	
}
