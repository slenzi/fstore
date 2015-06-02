package org.lenzi.fstore.cms.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.repository.tree.TreeRepository;
import org.lenzi.fstore.service.TreeBuilder;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.lenzi.fstore.tree.Tree;
import org.lenzi.fstore.util.FileUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FileStoreHelper {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	@Qualifier("cmsDirectoryTree")
	private TreeRepository<CmsDirectory> treeRepository;
	
	@Autowired
	private TreeBuilder<CmsDirectory> treeBuilder;	
	
	public FileStoreHelper() {

	}
	
	/**
	 * Get printable tree
	 * 
	 * @deprecated - remove
	 * 
	 * @param dirId - id of node
	 * @return
	 * @throws DatabaseException
	 */
	public String printTree(Long dirId) throws DatabaseException {
		
		Tree<CmsDirectory> sourceTree = getTree(dirId);
		
		return sourceTree.printTree();
		
	}
	
	/**
	 * Get a tree for the cms directory.
	 * 
	 * @deprecated - remove
	 * 
	 * @param dirId - id of node
	 * @return
	 * @throws DatabaseException
	 */
	public Tree<CmsDirectory> getTree(Long dirId) throws DatabaseException {
		
		// TODO - allow for specific fetch options (with file meta and file data if needed.)
		
		CmsDirectory cmsDir = treeRepository.getNodeWithChild(new CmsDirectory(dirId));
		Tree<CmsDirectory> tree = null;
		try {
			tree = treeBuilder.buildTree(cmsDir);
		} catch (ServiceException e) {
			throw new DatabaseException("Failed to build tree from CmsDirectory node, id => " + dirId, e);
		}
		return tree;
		
	}	
	
	/**
	 * Joins the CmsStore path and the relative CmsDirectory path to get the full/absolute path
	 * to the directory on the file system
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 */
	public String getAbsoluteDirectoryString(CmsFileStore cmsStore, CmsDirectory cmsDirectory){
		
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
	public String getAbsoluteFileString(CmsFileStore cmsStore, CmsDirectory cmsDirectory, CmsFileEntry cmsFileEntry){
		
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
	public Path getAbsoluteDirectoryPath(CmsFileStore cmsStore, CmsDirectory cmsDirectory){
		
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
	public Path getAbsoluteFilePath(CmsFileStore cmsStore, CmsDirectory cmsDirectory, CmsFileEntry cmsFileEntry){
		
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
