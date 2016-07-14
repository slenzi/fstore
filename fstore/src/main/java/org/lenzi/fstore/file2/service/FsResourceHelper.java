package org.lenzi.fstore.file2.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lenzi.fstore.core.constants.FsConstants;
import org.lenzi.fstore.core.repository.tree.TreeRepository;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
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
	 * Joins the FsResourceStore path and the relative FsDirectoryResource path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @return
	 */
	public String getAbsoluteDirectoryString(FsResourceStore fsStore, FsDirectoryResource fsDirectory){
		
		String relPath = fsDirectory.getRelativePath();
		if(!relPath.startsWith(FsConstants.FILE_SEPARATOR)){
			relPath = FsConstants.FILE_SEPARATOR + relPath;
		}
		return fsStore.getStorePath() + relPath;		
		
	}
	
	/**
	 * Joins the FsResourceStore path and the relative FsFileMetaResource path to get the full/absolute path
	 * to the file on the file system
	 * 
	 * @param fsStore
	 * @param fsFileEntry
	 * @return
	 */
	public String getAbsoluteFileString(FsResourceStore fsStore, FsFileMetaResource fsFileEntry){
		
		String relPath = fsFileEntry.getRelativePath();
		if(!relPath.startsWith(FsConstants.FILE_SEPARATOR)){
			relPath = FsConstants.FILE_SEPARATOR + relPath;
		}
		return fsStore.getStorePath() + relPath;		
		
	}
	
	/**
	 * Joins the FsResourceStore path, relative FsDirectoryResource path, and FsFileMetaResource file name to
	 * get the full/absolute path to the file on the file system.
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @param fsFileEntry
	 * @return
	 */
	public String getAbsoluteFileString(FsResourceStore fsStore, FsDirectoryResource fsDirectory, FsFileMetaResource fsFileEntry){
		
		return getAbsoluteDirectoryString(fsStore, fsDirectory) + FsConstants.FILE_SEPARATOR + fsFileEntry.getName();
		
	}
	
	/**
	 * Joins the FsResourceStore path and the relative FsDirectoryResource path to get the full/absolute path
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
	 * Joins the FsResourceStore path, relative FsDirectoryResource path, and FsFileMetaResource file name to
	 * get the full/absolute path to the file on the file system.
	 * 
	 * @param fsStore
	 * @param fsDirectory
	 * @param fsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(FsResourceStore fsStore, FsDirectoryResource fsDirectory, FsFileMetaResource fsFileEntry){
		
		return Paths.get(getAbsoluteFileString(fsStore, fsDirectory, fsFileEntry));
		
	}
	
	/**
	 * Joins the FsResourceStore path and the relative FsFileMetaResource path to get the full/absolute path
	 * to the file on the file system
	 * 
	 * @param fsStore
	 * @param fsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(FsResourceStore fsStore, FsFileMetaResource fsFileEntry){
		
		return Paths.get(getAbsoluteFileString(fsStore, fsFileEntry));
		
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
	
	/**
	 * Build absolute path for resource that will be placed under 'fsDirectory'.
	 * 
	 * @param fsStore - the resource store for the directory
	 * @param fsDirectory - the directory where the new path resource will be placed
	 * @param pathName -  the name of the new path resource (sub dir name or file name)
	 * @return the absolute path for the new path resource
	 */
	public Path getAbsolutePath(FsResourceStore fsStore, FsDirectoryResource fsDirectory, String pathName) {
		
		return Paths.get(fsStore.getStorePath() + fsDirectory.getRelativePath() + FsConstants.FILE_SEPARATOR + pathName);
		
	}
	
	/**
	 * Build relative path for resource that will be placed under 'fsDirectory'.
	 * 
	 * @param fsStore - the resource store for the directory
	 * @param fsDirectory - the directory where the new path resource will be placed
	 * @param pathName -  the name of the new path resource (sub dir name or file name)
	 * @return the relative path for the new path resource
	 */
	public String getRelativePath(FsResourceStore fsStore, FsDirectoryResource fsDirectory, String pathName){
		
		Path storePath = Paths.get(fsStore.getStorePath());
		Path childPath =  Paths.get(fsStore.getStorePath() + fsDirectory.getRelativePath() + FsConstants.FILE_SEPARATOR + pathName);
		
		Path childRelativePath = storePath.relativize(childPath);
		
		String sChildRelativePath = childRelativePath.toString();
		
		// all paths in database use forward slash
		sChildRelativePath = sChildRelativePath.replace("\\", FsConstants.FILE_SEPARATOR);
		
		if(!sChildRelativePath.startsWith(FsConstants.FILE_SEPARATOR)){
			sChildRelativePath = FsConstants.FILE_SEPARATOR + sChildRelativePath;
		}
		
		return sChildRelativePath;
		
	}

}
