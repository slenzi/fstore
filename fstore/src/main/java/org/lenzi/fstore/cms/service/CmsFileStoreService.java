package org.lenzi.fstore.cms.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository;
import org.lenzi.fstore.cms.repository.CmsDirectoryRepository.CmsDirectoryFetch;
import org.lenzi.fstore.cms.repository.CmsFileEntryRepository;
import org.lenzi.fstore.cms.repository.CmsFileEntryRepository.CmsFileEntryFetch;
import org.lenzi.fstore.cms.repository.CmsFileStoreRepository;
import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileEntry;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Main service for working with cms stores, directories, and files.
 * 
 * @author sal
 */
@Service
public class CmsFileStoreService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private CmsFileStoreRepository cmsFileStoreRepository;
	
	@Autowired
	private CmsDirectoryRepository cmsDirectoryRepository;
	
	@Autowired
	private CmsFileEntryRepository cmsFileEntryRepository;
	
	
	public CmsFileStoreService() {
		
	}
	
	/**
	 * Get full path for directory
	 * 
	 * @param nodeId
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(Long dirId) throws CmsServiceException {
		
		Path path = null;
		try {
			path = cmsDirectoryRepository.getAbsoluteDirectoryPath(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching path for directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return path;
		
	}
	
	/**
	 * Get full path for directory
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(CmsFileStore cmsStore, CmsDirectory cmsDirectory) {
		
		return cmsDirectoryRepository.getAbsoluteDirectoryPath(cmsStore, cmsDirectory);
		
	}
	
	/**
	 * Get full path for file
	 * 
	 * @param cmsStore
	 * @param cmsDirectory
	 * @param cmsFileEntry
	 * @return
	 */
	public Path getAbsoluteFilePath(CmsFileStore cmsStore, CmsDirectory cmsDirectory, CmsFileEntry cmsFileEntry){
		
		return cmsFileEntryRepository.getAbsoluteFilePath(cmsStore, cmsDirectory, cmsFileEntry);
		
	}
	
	/**
	 * Create new store.
	 * 
	 * @param storePath
	 * @param name
	 * @param description
	 * @param clearIfExists
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore createFileStore(Path storePath, String name, String description, boolean clearIfExists) throws CmsServiceException {
		
		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.createFileStore(storePath, name, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error creating store. " + e.getMessage(), e);
		}
		return cmsStore;
		
	}

	/**
	 * Get store by id
	 * 
	 * @param storeId
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore getCmsStoreByStoreId(long storeId) throws CmsServiceException {
		
		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsStoreByStoreId(storeId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching store, id => " + storeId + ". " + e.getMessage(), e);
		}
		return cmsStore;
		
	}

	/**
	 * Get store by it's root directory
	 * 
	 * @param rootDirId
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore getCmsStoreByRootDirId(long rootDirId) throws CmsServiceException {

		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsStoreByRootDirId(rootDirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching store by root directory, id => " + rootDirId + ". " + e.getMessage(), e);
		}
		return cmsStore;		
		
	}
	
	/**
	 * Get store by any directory. does not have to be a root directory.
	 * 
	 * @param dirId
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileStore getCmsStoreByDirId(long dirId) throws CmsServiceException {

		CmsFileStore cmsStore = null;
		try {
			cmsStore = cmsFileStoreRepository.getCmsFileStoreByDirId(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching store by directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return cmsStore;		
		
	}

	/**
	 * Fetch directory by id
	 * 
	 * @param dirId
	 * @param fetch
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsDirectory getCmsDirectoryById(long dirId, CmsDirectoryFetch fetch) throws CmsServiceException {
		
		CmsDirectory dir = null;
		try {
			dir = cmsDirectoryRepository.getCmsDirectoryById(dirId, fetch);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching directory, id => " + dirId + ". " + e.getMessage(), e);
		}
		return dir;
		
	}

	/**
	 * Add new directory
	 * 
	 * @param parentDirId
	 * @param dirName
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsDirectory addDirectory(Long parentDirId, String dirName) throws CmsServiceException {

		CmsDirectory dir = null;
		try {
			dir = cmsDirectoryRepository.addDirectory(parentDirId, dirName);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error adding new directory. " + e.getMessage(), e);
		}
		return dir;
		
	}

	/**
	 * Add/replace new file
	 * 
	 * @param sourcePath
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileEntry addFile(Path sourcePath, Long dirId, boolean replaceExisting) throws CmsServiceException {

		CmsFileEntry fileEntry = null;
		try {
			fileEntry = cmsFileEntryRepository.addFile(sourcePath, dirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when adding new file. " + e.getMessage(), e);
		} catch (IOException e) {
			throw new CmsServiceException("I/O error when adding new file. " + e.getMessage(), e);
		}
		return fileEntry;
		
	}
	
	/**
	 * Add files
	 * 
	 * @param filePaths
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws CmsServiceException
	 */
	public List<CmsFileEntry> addFile(List<Path> filePaths, Long dirId, boolean replaceExisting) throws CmsServiceException {
		
		List<CmsFileEntry> entries = null;
		try {
			entries = cmsFileEntryRepository.addFile(filePaths, dirId, replaceExisting);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when adding new files. " + e.getMessage(), e);
		} catch (IOException e) {
			throw new CmsServiceException("I/O error when adding new files. " + e.getMessage(), e);
		}
		return entries;
		
	}

	/**
	 * Get a file entry
	 * 
	 * @param fileId
	 * @param fetch
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileEntry getCmsFileEntryById(Long fileId, CmsFileEntryFetch fetch) throws CmsServiceException {

		CmsFileEntry fileEntry = null;
		try {
			fileEntry = cmsFileEntryRepository.getCmsFileEntryById(fileId, fetch);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching file, id => " + fileId + ". " + e.getMessage(), e);
		}
		return fileEntry;		
		
	}

	/**
	 * Copy file
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileEntry copyFile(Long fileId, Long dirId, boolean replaceExisting) throws CmsServiceException {
		
		CmsFileEntry copy = null;
		try {
			copy = cmsFileEntryRepository.copyFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new CmsServiceException("Error, file already exists. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when copying file. " + e.getMessage(), e);
		}
		return copy;
		
	}

	/**
	 * Moev file
	 * 
	 * @param fileId
	 * @param dirId
	 * @param replaceExisting
	 * @return
	 * @throws CmsServiceException
	 */
	public CmsFileEntry moveFile(Long fileId, Long dirId, boolean replaceExisting) throws CmsServiceException {

		CmsFileEntry move = null;
		try {
			move = cmsFileEntryRepository.moveFile(fileId, dirId, replaceExisting);
		} catch (FileAlreadyExistsException e) {
			throw new CmsServiceException("Error, file already exists. " + e.getMessage(), e);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Database error when moving file. " + e.getMessage(), e);
		}
		return move;		
		
	}	

}
