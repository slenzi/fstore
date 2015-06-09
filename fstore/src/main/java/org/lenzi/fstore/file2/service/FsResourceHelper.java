package org.lenzi.fstore.file2.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 
 * @author sal
 *
 */
@Service
public class FsResourceHelper {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("FsPathResourceTree")
	private TreeRepository<FsPathResource> treeRepository;
	
	public FsResourceHelper() {

	}	
	
	/**
	 * Joins the FsFileStore path and the relative FsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @return
	 */
	public String getAbsoluteDirectoryString(FsResourceStore fsStore, FsDirectoryResource fsDirectory){
		
		String dirRelativePath = fsDirectory.getRelativePath();
		if(!dirRelativePath.startsWith(File.separator)){
			dirRelativePath = File.separator + dirRelativePath;
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
	/*
	public String getAbsoluteFileString(FsResourceStore fsStore, FsDirectoryResource fsDirectory, FsFileMetaResource fsFileEntry){
		
		return getAbsoluteDirectoryString(fsStore, fsDirectory) + File.separator + fsFileEntry.getFileName();	
		
	}
	*/
	
	/**
	 * Joins the FsFileStore path and the relative FsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(FsResourceStore fsStore, FsDirectoryResource fsDirectory){
		
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
	/*
	public Path getAbsoluteFilePath(FsResourceStore fsStore, FsDirectoryResource fsDirectory, FsFileMetaResource fsFileEntry){
		
		return Paths.get(getAbsoluteFileString(fsStore, fsDirectory, fsFileEntry));
		
	}
	*/
	
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
