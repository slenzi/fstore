package org.lenzi.fstore.cms.service;

import java.io.File;
import java.io.Serializable;

import org.lenzi.fstore.cms.model.CmsDirectory;
import org.lenzi.fstore.cms.model.CmsFile;
import org.lenzi.fstore.cms.model.CmsFileStore;

public interface FileStoreManager extends Serializable {

	// create file store
	public CmsFileStore createFileStore(File dirPath, String name, String description);
	
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
