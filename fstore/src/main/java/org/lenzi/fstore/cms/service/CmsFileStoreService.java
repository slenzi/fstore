package org.lenzi.fstore.cms.service;

import java.nio.file.Path;

import org.lenzi.fstore.cms.repository.CmsDirectoryRepository;
import org.lenzi.fstore.cms.repository.CmsFileEntryRepository;
import org.lenzi.fstore.cms.repository.CmsFileStoreRepository;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	 * 
	 * @param nodeId
	 * @return
	 */
	public Path getAbsoluteDirectoryPath(Long dirId) throws CmsServiceException {
		
		Path path = null;
		try {
			path = cmsDirectoryRepository.getAbsoluteDirectoryPath(dirId);
		} catch (DatabaseException e) {
			throw new CmsServiceException("Error fetching path for directory " + dirId + ". " + e.getMessage(), e);
		}
		return path;
		
	}

}
