package org.lenzi.fstore.file2.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

// used before upgrading to hibernate 5. now we use org.springframework.transaction.annotation.Transactional
//import javax.transaction.Transactional;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.FileUtil;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.web.messaging.UploadMessageService;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    
    @Autowired
    private UploadMessageService uploadMessageService;
    
    private String holdingSetupErrorMsg = "";
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
	
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
	
	@PreDestroy
	private void cleanup(){
		
		executor.shutdownNow();
		
	}
	
	/**
	 * Save all files in the file map to the temp upload directory.
	 * 
	 * @param fileMap
	 * @return Path to newly created temp directory where all files are saved.
	 * @throws ServiceException
	 */
	public Path processToTemp(Map<String, MultipartFile> fileMap) throws ServiceException {
		
		String parentTempDir = appProps.getProperty("upload.temp.path");
		
		long epochMilli = System.currentTimeMillis();
		String uuid = UUID.randomUUID().toString();
		
		String tempDir = parentTempDir + File.separator + String.valueOf(epochMilli) + "." + uuid;
		
		Path tempPath = Paths.get(tempDir);
		
		try {
			FileUtil.createDirectory(tempPath, true);
		} catch (IOException e) {
			throw new ServiceException("Failed to create temporary directory for uploaded files, at " + tempDir + ". " + e.getMessage());
		}
		
		// save all files
		fileMap.values().stream().forEach(
			(filePart) -> {
				
				Path filePath = Paths.get(tempDir + File.separator + filePart.getOriginalFilename());
				
				try {
					Files.write(filePath, filePart.getBytes());
				} catch (Exception e) {
					throw new RuntimeException("Error saving file " + filePart.getOriginalFilename() + " to directory " + tempDir + ". " + e.getMessage(), e);
				}
				
			});
		
		return tempPath;
		
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
	 * @param tempDir - temporary directory where uploaded files reside
	 * @param replaceExisting - true to replace existing files
	 * @return reference to the newly created directory in the holding resource store.
	 * @throws ServiceException
	 */
	public FsDirectoryResource processToHolding(Path tempDir, boolean replaceExisting) throws ServiceException {
		
		if(holdingStore == null){
			throw new ServiceException("Error, holding resource store is null. Check application logs for error message. " + holdingSetupErrorMsg);
		}
		
		logger.info("Got holding store");
		
		List<Path> filePaths = null;
		try {
			filePaths = FileUtil.listFilesToDepth(tempDir, 1);
		} catch (IOException e) {
			throw new ServiceException("Error listing files in temporary directory " + tempDir.toString());
		}
		
		LocalDateTime timePoint = LocalDateTime.now();
		
		String dirPostfix = String.format("_%d.%s.%d_%d.%d.%d",
				timePoint.getYear(), timePoint.getMonth(), timePoint.getDayOfMonth(),
				timePoint.getHour(), timePoint.getMinute(), timePoint.getSecond());
		
		String dirName = "Upload" + dirPostfix;
		
		FsDirectoryResource uploadDir = null;
		try {
			uploadDir = fsResourceService.addDirectoryResource(holdingStore.getRootDirectoryResource().getDirId(), dirName);
		} catch (ServiceException e) {
			throw new ServiceException("Error creating new directory for upload in the holding resource store. "
					+ "Directory name = " + dirName + ". " + e.getMessage(),e);
		}
		
		logger.info("Created new directory in holding store for uploaded data: '" + dirName + "'");
		
		final FsDirectoryResource finalDir = uploadDir;
		filePaths.stream().forEach(
			(pathToFile) ->{
			
				executor.execute(() -> {
				
					byte[] fileBytes = null;
	
					String fileName = pathToFile.getFileName().toString();
					
					try {
						
						fileBytes = Files.readAllBytes(pathToFile);
						
						FsFileMetaResource resource = fsResourceService.addFileResource(fileName, fileBytes, finalDir.getDirId(), replaceExisting);
						
						logger.info("Saved file '" + fileName + "' to holding store directory '" + dirName + "'.");
						
						uploadMessageService.sendUploadProcessedMessage(resource.getFileId(), finalDir.getDirId(), resource.getName());
						
					} catch (ServiceException e) {
						throw new RuntimeException("Error saving file '" + fileName + "' to directory '" + dirName + "'.", e);
					} catch (IOException e){
						throw new RuntimeException("IOException thrown when attempting to read file byte data from path. " + e.getMessage(), e);
					}
				
				});
				
			});
		
		return uploadDir;
		
	}
	
	/**
	 * Processes files to existing directory
	 * 
	 * @param tempDir - temporary directory where uploaded files reside
	 * @param parentDirId - id of parent directory node
	 * @param replaceExisting - true to replace existing files
	 * @throws ServiceException
	 */
	public void processToDirectory(Path tempDir, Long parentDirId, boolean replaceExisting) throws ServiceException {
		
		List<Path> filePaths = null;
		try {
			filePaths = FileUtil.listFilesToDepth(tempDir, 1);
		} catch (IOException e) {
			throw new ServiceException("Error listing files in temporary directory " + tempDir.toString());
		}
		
		filePaths.stream().forEach(
			(pathToFile) ->{
			
				executor.execute(() -> {
				
					byte[] fileBytes = null;
	
					String fileName = pathToFile.getFileName().toString();
					
					try {
						
						fileBytes = Files.readAllBytes(pathToFile);
						
						FsFileMetaResource resource = fsResourceService.addFileResource(fileName, fileBytes, parentDirId, replaceExisting);
						
						logger.info("Saved file '" + fileName + "' to directory with id '" + parentDirId + "'.");
						
						uploadMessageService.sendUploadProcessedMessage(resource.getFileId(), parentDirId, resource.getName());
						
					} catch (ServiceException e) {
						throw new RuntimeException("Error saving file '" + fileName + "' to directory with id '" + parentDirId + "'.", e);
					} catch (IOException e){
						throw new RuntimeException("IOException thrown when attempting to read file byte data from path. " + e.getMessage(), e);
					}
				
				});
				
			});
		
	}

}
