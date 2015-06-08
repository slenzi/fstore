package org.lenzi.fstore.file.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.service.TreeBuilder;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileEntry;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FsHelper {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("DirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;
	
	//@Autowired
	//private TreeBuilder<CmsDirectory> treeBuilder;	
	
	public FsHelper() {

	}	
	
	/**
	 * Joins the CmsStore path and the relative CmsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 */
	public String getAbsoluteDirectoryString(FsFileStore cmsStore, FsDirectory cmsDirectory){
		
		String dirRelativePath = cmsDirectory.getRelativeDirPath();
		if(!dirRelativePath.startsWith(File.separator)){
			dirRelativePath = File.separator + dirRelativePath;
		}
		return cmsStore.getStorePath() + dirRelativePath;		
		
	}
	
	/**
	 * Joins the CmsStore path, relative CmsDirectory path, and CmsFileEntry file name to
	 * get the full/absolute path to the file on the file system.
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @param cmsFileEntry
	 * @return
	 */
	public String getAbsoluteFileString(FsFileStore cmsStore, FsDirectory cmsDirectory, FsFileEntry cmsFileEntry){
		
		return getAbsoluteDirectoryString(cmsStore, cmsDirectory) + File.separator + cmsFileEntry.getFileName();	
		
	}
	
	/**
	 * Joins the CmsStore path and the relative CmsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(FsFileStore cmsStore, FsDirectory cmsDirectory){
		
		return Paths.get(getAbsoluteDirectoryString(cmsStore, cmsDirectory));
		
	}
	
	/**
	 * Joins the CmsStore path, relative CmsDirectory path, and CmsFileEntry file name to
	 * get the full/absolute path to the file on the file system.
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @param cmsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(FsFileStore cmsStore, FsDirectory cmsDirectory, FsFileEntry cmsFileEntry){
		
		return Paths.get(getAbsoluteFileString(cmsStore, cmsDirectory, cmsFileEntry));
		
	}
	
	/**
	 * Create directory on file system
	 * 
	 * @param path
	 * @param clearIfExists
	 * @throws IOException
	 * @throws SecurityException
	 */
	public void createDirOnFileSystem(Path path, boolean clearIfExists) throws IOException, SecurityException {
		
		FileUtil.createDirectory(path, clearIfExists);

		boolean canReadWrite = Files.isReadable(path) && Files.isWritable(path);
		if(!canReadWrite){
			throw new SecurityException("Cannot read and write to directory " + path.toString());
		}		
		
	}	

}
