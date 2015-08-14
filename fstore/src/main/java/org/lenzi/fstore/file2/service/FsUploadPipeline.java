package org.lenzi.fstore.file2.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Processes uploads
 * 
 * @author sal
 */
@Service
@Transactional
public class FsUploadPipeline {

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private ManagedProperties appProps; 	
	
	private FsResourceStore holdingStore = null;
	
    @Autowired
    private FsQueuedResourceService fsResourceService;
    
    private String holdingSetupErrorMsg = "";
	
	public FsUploadPipeline() {
		
	}
	
	@PostConstruct
	private void init(){
		
		logger.info(FsUploadPipeline.class.getName() + " post construct initialization.");
		
		if(holdingStore == null){
			try {
				holdingStore = getHoldingStore();
			} catch (ServiceException e) {
				holdingSetupErrorMsg = e.getMessage();
				logger.error(e.getMessage());
			}
		}
		
	}
	
	/**
	 * Get the holding resource store. This is where uploads are temporarily stored.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	private FsResourceStore getHoldingStore() throws ServiceException {
		
		String holdingStoreName = appProps.getProperty("resource.store.holding.name");
		
		FsResourceStore holdingStore = null;
		try {
			holdingStore = fsResourceService.getResourceStoreByName(holdingStoreName);
		} catch (ServiceException e) {
			throw new ServiceException("Failed to fetch holding resource store from database. " + e.getMessage(), e);
		}
		if(holdingStore == null){
			// create store
			String holdingStorePath = appProps.getProperty("resource.store.holding.path");
			String holdingStoreDesc = appProps.getProperty("resource.store.holding.desc");
			try {
				holdingStore = fsResourceService.addResourceStore(Paths.get(holdingStorePath), holdingStoreName, holdingStoreDesc, true);
			} catch (ServiceException e) {
				throw new ServiceException("Failed to create holding store. " + e.getMessage(), e);
			}
		}
		
		return holdingStore;
		
	}
	
	/**
	 * Creates a new directory in the holding resource store for the uploaded files, then saves all files to the directory.
	 * 
	 * @param fileMap - map of files that were uploaded by a client
	 * @return reference to the newly created directory in the holding resource store.
	 * @throws ServiceException
	 */
	public FsDirectoryResource processToHolding(Map<String, MultipartFile> fileMap) throws ServiceException {
		
		if(holdingStore == null){
			throw new ServiceException("Error, holding resource store is null. Check application logs for error message. " + holdingSetupErrorMsg);
		}
		
		logger.info("Got holding store");
		
		LocalDateTime timePoint = LocalDateTime.now();
		
		String dirPostfix = String.format("_%d.%s.%d_%d.%d.%d",
				timePoint.getYear(), timePoint.getMonth(), timePoint.getDayOfMonth(),
				timePoint.getHour(), timePoint.getMinute(), timePoint.getSecond());
		
		String dirName = "Upload" + dirPostfix;
		
		FsDirectoryResource uploadDir = null;
		try {
			uploadDir = fsResourceService.addDirectoryResource(dirName, holdingStore.getRootDirectoryResource().getDirId());
		} catch (ServiceException e) {
			throw new ServiceException("Error creating new directory for upload in the holding resource store. "
					+ "Directory name = " + dirName + ". " + e.getMessage(),e);
		}
		
		logger.info("Created new directory in holding store for uploaded data: '" + dirName + "'");
		
		final FsDirectoryResource finalDir = uploadDir;
		fileMap.values().stream().forEach(
				(filePart) -> {
					
					try {
						
						fsResourceService.addFileResource(filePart.getOriginalFilename(), filePart.getBytes(), finalDir.getDirId(), true);
						
						logger.info("Saved file '" + filePart.getName() + "' to holding store directory '" + dirName + "'.");
						
					} catch (ServiceException e) {
						throw new RuntimeException("Error saving file '" + filePart.getName() + "' to directory '" + dirName + "' in the holding resource store.", e);
					} catch (IOException e){
						throw new RuntimeException("IOException thrown when attempting to read file byte data from MultipartFile map. " + e.getMessage(), e);
					}
					
				});
		
		return uploadDir;
		
	}
	
	/**
	 * Processes files to existing directory
	 * 
	 * @param fileMap
	 * @param parentDirId
	 * @param replaceExisting
	 * @throws ServiceException
	 */
	public void processToDirectory(Map<String, MultipartFile> fileMap, Long parentDirId, boolean replaceExisting) throws ServiceException {
		
		fileMap.values().stream().forEach(
				(filePart) -> {
					
					try {
						
						fsResourceService.addFileResource(filePart.getOriginalFilename(), filePart.getBytes(), parentDirId, replaceExisting);
						
						logger.info("Saved file '" + filePart.getName() + "' to directory with id '" + parentDirId + "'.");
						
					} catch (ServiceException e) {
						throw new RuntimeException("Error saving file '" + filePart.getName() + "' to directory with id '" + parentDirId + "'.", e);
					} catch (IOException e){
						throw new RuntimeException("IOException thrown when attempting to read file byte data from MultipartFile map. " + e.getMessage(), e);
					}
					
				});
		
	}

}
