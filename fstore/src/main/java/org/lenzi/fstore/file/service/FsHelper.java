package org.lenzi.fstore.file.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lenzi.fstore.core.constants.FsConstants;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
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
	@Qualifier("FsDirectoryTree")
	private TreeRepository<FsDirectory> treeRepository;
	
	public FsHelper() {

	}	
	
	/**
	 * Joins the FsFileStore path and the relative FsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @return
	 */
	public String getAbsoluteDirectoryString(FsFileStore fsStore, FsDirectory fsDirectory){
		
		String dirRelativePath = fsDirectory.getRelativeDirPath();
		if(!dirRelativePath.startsWith(FsConstants.FILE_SEPARATOR)){
			dirRelativePath = FsConstants.FILE_SEPARATOR + dirRelativePath;
		}
		return fsStore.getStorePath() + dirRelativePath;		
		
	}
	
	/**
	 * Joins the FsFileStore path, relative FsDirectory path, and FsFileEntry file name to
	 * get the full/absolute path to the file on the file system.
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @param fsFileEntry
	 * @return
	 */
	public String getAbsoluteFileString(FsFileStore fsStore, FsDirectory fsDirectory, FsFileEntry fsFileEntry){
		
		return getAbsoluteDirectoryString(fsStore, fsDirectory) + FsConstants.FILE_SEPARATOR + fsFileEntry.getFileName();	
		
	}
	
	/**
	 * Joins the FsFileStore path and the relative FsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(FsFileStore fsStore, FsDirectory fsDirectory){
		
		return Paths.get(getAbsoluteDirectoryString(fsStore, fsDirectory));
		
	}
	
	/**
	 * Joins the FsFileStore path, relative FsDirectory path, and FsFileEntry file name to
	 * get the full/absolute path to the file on the file system.
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @param fsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(FsFileStore fsStore, FsDirectory fsDirectory, FsFileEntry fsFileEntry){
		
		return Paths.get(getAbsoluteFileString(fsStore, fsDirectory, fsFileEntry));
		
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
